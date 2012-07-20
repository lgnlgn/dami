package org.dami.common;

import java.io.IOException;


import org.dami.common.io.VectorStorage;

/**
 * an asynchronous buffer for vector IO
 * to use pool 
 * 1. initial VectorPool with a VectorStorage
 * 2. for each iteration of algorithm:
 *        call pool.open() to get start
 *        fetch Vectors by pool.get() and return back to pool as below: 
 *        //for(Vectors sample = pool.get(); sample != null; sample = pool.get()){
 *        //	do computations ...
 *        //    pool.takeBack();
 *        }
 *        call pool.close()
 * 
 * @author lgn
 *
 */
public class VectorPool implements Runnable{
	final static int DEFAUlTBUFFERSIZE = 20;
	final static int VECTORSIZE = 20000;
	VectorStorage source;
	Vector.Status vs;
	Vector[] readingBuffer = null;
	Vector[] writingBuffer = null;

	volatile boolean reading;  //control by reader, writer need to monitor it
	volatile boolean writingfinished; // 
	volatile boolean finished; // 
	boolean written ;

	volatile int readingIdx;
	volatile int readingBufferSize ; // for readers 
	volatile int reads ;				// for 
	
	int writingBufferSize ;
	int commonFeatureSize ;

	volatile int backs;
	Thread writer = null;

	public VectorPool(VectorStorage dbSource, int bufferSize){
		this.source = dbSource;
		vs = dbSource.getVectorStatus();
		readingBuffer = new Vector[bufferSize];
		writingBuffer = new Vector[bufferSize];
		int maxfs = Utilities.getIntFromProperties(source.getDataSetInfo(), Constants.MAXFEATURESIZE);
		if (maxfs > bufferSize){
			for(int i = 1; i < bufferSize; i++){
				readingBuffer[i] = new Vector(bufferSize);
				writingBuffer[i] = new Vector(bufferSize);
			}
			commonFeatureSize = bufferSize;
			readingBuffer[0] = new Vector(maxfs + 1);
			writingBuffer[0] = new Vector(maxfs + 1);
		}else{
			for(int i = 0; i < bufferSize; i++){
				readingBuffer[i] = new Vector(maxfs);
				writingBuffer[i] = new Vector(maxfs);
			}
			commonFeatureSize = maxfs;
		}	
	}
	
	
	public VectorPool(VectorStorage dbSource ){
		this(dbSource, DEFAUlTBUFFERSIZE);
	}

	

	/**
	 * @throws IOException 
	 * 
	 */
	public void open() throws IOException{
		finished = false;
		written = false;
		writingfinished = false;
		source.open();
		fillBuffer();
		switchBuffer();
		writer = new Thread(this);
		writer.setDaemon(true);
		writer.start();
	}

	public void close() throws IOException{
		writingfinished = true;
		finished = true;
		source.close();
		try {
			writer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		writer = null;
	}


	private synchronized int checkAndGet(){
		if (readingIdx < readingBufferSize){ //
			readingIdx += 1;
			return readingIdx -1;
		}else{ // exceed the size , a large number equals  is enough
			return commonFeatureSize;
		}
	}

	/**
	 * null for end
	 * @return
	 */
	public Vector get(){
		while(true){ //
			int currentIdx = checkAndGet(); // threads synchronization here
			if (currentIdx < readingBufferSize){  // OK, fetch one
				return readingBuffer[currentIdx];
			}
			else{
				while(true){ //No, waiting for command
					if (readingIdx >= readingBufferSize){ // current buffer is not switched yet,
						if (finished){ // the switch phrase said finished , 
							return null;
						}else{
							; //
						}							
					}else{  
						// the other buffer is filled and switched ready.
						// that is : readingIdx assigned to 0 by the writer
						break;
					}
					
				}
			}
		}
	}

	/**
	 * no need to check 
	 * 
	 */
	private void switchBuffer(){
		readingIdx = readingBuffer.length + 1; // set to max, forbid reading
		if (writingBufferSize == 0){ // no need to switch
			finished = true;
			return;
		}

		Vector[] bufferSwap = readingBuffer;
		readingBuffer = writingBuffer;
		writingBuffer = bufferSwap;

		readingBufferSize = writingBufferSize;  //parameters ready for readers
		writingBufferSize = 0;
		reading = true;      // close for the writer
		backs = 0;
		written = false;

		readingIdx = 0;      // set to 0, readers could found and then start to read
		
		if (writingfinished){
			finished = true;
		}
		
	}



	public synchronized void takeBack(){
		backs += 1;
		if (backs >= readingBufferSize){ //all vectors are returned back
			readingBufferSize = 0;
			reading = false;  //tell the writer reading phrase finished
		}
	}

	private void fillVector(int idxOfBuffer, Vector tmp){
		writingBuffer[idxOfBuffer].count = tmp.count;
		writingBuffer[idxOfBuffer].id = tmp.id;
		writingBuffer[idxOfBuffer].label = tmp.label;
		writingBuffer[idxOfBuffer].featureSize = tmp.featureSize;
		if (vs.hasWeight){
			for(int j = 0 ; j < tmp.featureSize; j++){
				writingBuffer[idxOfBuffer].features[j] = tmp.features[j];
				writingBuffer[idxOfBuffer].weights[j] = tmp.weights[j];
			}
		}else{
			for(int j = 0 ; j < tmp.featureSize; j++){
				writingBuffer[idxOfBuffer].features[j] = tmp.features[j];
			}
		}
	}
	
	private void swapVectorInBuffer(int i, int j){
		Vector s = writingBuffer[i];
		writingBuffer[i] = writingBuffer[j];
		writingBuffer[j] = s;
	}
	
	
	private void fillBuffer() throws IOException{
		Vector tmp = writingBuffer[0];  //largest vector pointer
		int i = 1;
		for(; i < writingBuffer.length; i++){
			source.next(tmp);
			if (tmp.featureSize == -1){ // meets the end while not fulfilled yet
				i -= 1;
				swapVectorInBuffer(0, i);
				writingfinished = true;
				break;
			}
			if (tmp.featureSize <= commonFeatureSize){
				fillVector(i, tmp);
			}else{ // first meet 
				break;
			}
		}
		if (i == writingBuffer.length){ 
			source.next(tmp);
			if (tmp.featureSize == -1){ 
				i -= 1;
				swapVectorInBuffer(0, i);
				writingfinished = true;
			}else{
				;
			}
		}
		writingBufferSize = i; //index upper bound
		written = true;
	}

	@Override
	public void run() {
		while(!finished){
			if (reading && written){ // need to waiting for reader
				;
			}else if (!reading && written){
				this.switchBuffer();
			}else{
				// read some vectors from file to ram each loop  
				try{
					this.fillBuffer();  
				}catch (IOException e) {
					throw new RuntimeException("IO exception!!!!!!!");
				}
			}
		}
	}

}
