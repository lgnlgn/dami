package org.dami.common;

import java.io.IOException;
import java.util.ArrayList;

import org.dami.common.io.FileVectorConverter;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;
import org.dami.common.io.VectorStorage;

public class TestVectorPool {

	static class VectorGetter implements Runnable{
		
		
		VectorPool vp;
		
		public VectorGetter(VectorPool vp) throws IOException{
			this.vp = vp;
			vp.open();
		}
		
		@Override
		public void run() {
			for(Vector c = vp.get(); c != null; c = vp.get()){
				System.out.println(c);
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				vp.takeBack();
			}
			
		}
		
		public void close() throws IOException{
			if (vp != null)
				vp.close();
		}
		
	}
	
	
	static void convert() throws IOException{
		FileVectorReader reader = new FileVectorReader.LineReader("e:/data/vp.txt", " ", ":", new Vector.Status(9));
		FileVectorWriter writer = new FileVectorWriter.BytesWriter("d:/vp", new Vector.Status(9));
		
		FileVectorConverter converter = new FileVectorConverter(reader, writer, new DataStatistic.CommonStatistic());
		converter.convert();
		
	}
	
	static void testPoolSlowReader() throws IOException, InterruptedException{
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta("d:/vp");
		VectorStorage vs = new VectorStorage.FileStorage(reader);
		
		VectorPool vp = new VectorPool(vs);
		vp.open();
		for(Vector c = vp.get(); c != null; c = vp.get()){
			System.out.println(c);
//			Thread.sleep(100);
			vp.takeBack();
		}
		vp.close();
	}
	
	
	static void testMultiOpen() throws IOException{
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta("d:/vp");
		VectorStorage vs = new VectorStorage.FileStorage(reader);
		VectorPool vp = new VectorPool(vs);
		vp.open();
		for(Vector c = vp.get(); c != null; c = vp.get()){
			System.out.println(c);
//			Thread.sleep(100);
			vp.takeBack();
		}
		vp.close();
		System.out.println("----------------------------");
		vp.open();
		for(Vector c = vp.get(); c != null; c = vp.get()){
			System.out.println(c);
//			Thread.sleep(100);
			vp.takeBack();
		}
		vp.close();
	}
	
	static void testMultiReader()throws IOException, InterruptedException{
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta("d:/vp");
		VectorStorage vs = new VectorStorage.FileStorage(reader);
		
		VectorPool vp = new VectorPool(vs);
		VectorGetter vg = new VectorGetter(vp);
		
		ArrayList<Thread> tlist = new ArrayList<Thread>();
		for(int i = 0 ; i < 4; i++){
			tlist.add(new Thread(vg));
		}
		for(Thread t : tlist){
			t.start();
		}
		for(Thread t : tlist){
			t.join();
		}
		vg.close();
	}
	
	static void testDuration() throws IOException{
		String db = "d:/real-sim";
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(db);
		
		VectorStorage vs = new VectorStorage.FileStorage(fvr);
		VectorPool vp = new VectorPool(vs);
		for(int i = 0 ; i < 1000; i++){
			long t = System.currentTimeMillis();
//			System.out.print("1");
			vp.open();
			for(Vector s = vp.get();s!= null; s=vp.get()){
				vp.takeBack();
			}
//			vp.takeBack(null);
//			System.out.println("2");
			vp.close();
			System.out.println("\ttime:" + (System.currentTimeMillis() -t));
		}
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
//		convert();
//		testPoolSlowReader();
//		testMultiReader();
//		testMultiOpen();
		testDuration();
	}

}
