package org.dami.common.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;


import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;

/**
 * Vectors reading for algorithms
 * @author lgn
 *
 */
public abstract class VectorStorage implements DataReader<Vector>{
	protected Properties dataStatus;
	protected FileVectorReader reader ;
	
	public interface RandomAccess{
		public void getVectorById(Vector sample, int id) throws IOException;
	}
	
	protected VectorStorage(FileVectorReader reader) throws IOException{
		if (reader == null)
			throw new RuntimeException("File Vector Reader not initialized yet!");
		
		File stat = new File(reader.getFilePath() + Constants.STAT_SUFFIX);
		if (!stat.exists() || !stat.isFile()){
			throw new IOException("Statistic file NOT FOUND! : " + stat.getAbsolutePath());
		}
		Properties p = new Properties();
		Reader statReader = new FileReader(stat);
		p.load(statReader);
		statReader.close();
		
		this.reader = reader;		
		dataStatus = p;

	}
	
	public void reOpenData() throws IOException{
		this.close();
		this.open();
	}

	public Properties getDataSetInfo() {
		return this.dataStatus;
	}
	
	public static class FileStorage extends VectorStorage{

		public FileStorage(FileVectorReader reader) throws IOException {
			super(reader);
			this.open();
		}

		@Override
		public void open() throws IOException {
			reader.open();
		}

		@Override
		public void close() throws IOException {
			reader.close();
		}

		@Override
		public synchronized void next(Vector sample) throws IOException {
			reader.next(sample);
		}
		
	}
	
	/**
	 * do not support random access
	 * @author lgn
	 *
	 */
	public static class RAMCompactStorage extends VectorStorage{

		int[] xlabels = null; // label 
		int[][] x = null;  // features
		float[][] xweights = null;  //weights
		int[] xcounts = null;  //count
		int[] xids = null;     //id
		int currentIdx = 0;  //current index of array
		
		Vector.Status vs = null;
		public RAMCompactStorage(FileVectorReader reader) throws IOException {
			super(reader);
			
			vs = this.reader.getVectorParameter();
			int samples = Utilities.getIntFromProperties(dataStatus, Constants.NUMBER_SAMPLES);
			Vector current = new Vector();
			
			x = new int[samples][];
			if (vs.hasLabel)
				xlabels = new int[samples];
			if (vs.hasWeight)
				xweights = new float[samples][];
			if (vs.hasCount)
				xcounts = new int[samples];
			if (vs.hasId)
				xids = new int[samples];
			
			reader.open();
			for(reader.next(current); current.featureSize >= 0; reader.next(current)){
				x[currentIdx] = new int[current.featureSize];
				if (vs.hasWeight){
					xweights[currentIdx] = new float[current.featureSize];
					for(int i = 0 ; i < current.featureSize; i++){
						x[currentIdx][i] = current.features[i];
						xweights[currentIdx][i] = current.weights[i];
					}
				}
				else
					for(int i = 0 ; i < current.featureSize; i++){
						x[currentIdx][i] = current.features[i];
					}
				if (vs.hasLabel)
					xlabels[currentIdx] = current.label;
				if (vs.hasCount)
					xcounts[currentIdx] = current.count;
				if (vs.hasId)
					xids[currentIdx] = current.id;
				currentIdx += 1;
			}
//			current = reader.sample;
//			System.out.println(currentIdx);
			reader.close();
		}

		public void open() throws IOException {
			currentIdx= 0;
		}

		public void close() throws IOException {
			currentIdx = this.xlabels.length;
		}

		@Override
		public synchronized void next(Vector sample) throws IOException {
			if (currentIdx >= this.xlabels.length){
				sample.featureSize = -1;
				return ;
			}
			else{
//				try{
				if (vs.hasLabel)
					sample.label = xlabels[currentIdx];
				if (vs.hasCount)
					sample.count = xcounts[currentIdx];
				if (vs.hasId)
					sample.id = xids[currentIdx];
				sample.featureSize = x[currentIdx].length; //always >= 0 
				if (vs.hasWeight)
					for(int i = 0 ; i < x[currentIdx].length; i++){
						sample.features[i] = x[currentIdx][i];
						sample.weights[i] =  xweights[currentIdx][i];
					}
				else{
					System.arraycopy(x[currentIdx], 0, sample.features, 0, x[currentIdx].length);
//					for(int i = 0 ; i < x[currentIdx].length; i++){
//						sample.features[i] = x[currentIdx][i];
//					}
				}
//				}catch (NullPointerException e){
//					System.out.println(currentIdx);
//				}
			}
			currentIdx += 1;
//			return current;
		}
		
	}
	
	
}
