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
	Properties dataStatus;
	FileVectorReader reader ;
	Vector current;
	
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
		current = new Vector();
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
		public Vector next() throws IOException {
			return reader.next();
		}
		
	}
	
	public static class RAMStorage extends VectorStorage{

		int[] xlabels = null; // label 
		int[][] x = null;  // features
		double[][] xweights = null;  //weights
		int[] xcounts = null;  //count
		int[] xids = null;     //id
		int currentIdx = 0;  //current index of array
		
		public RAMStorage(FileVectorReader reader) throws IOException {
			super(reader);
			int samples = Utilities.getIntFromProperties(dataStatus, Constants.NUMBER_SAMPLES);
			xlabels = new int[samples];
			x = new int[samples][];
			xweights = new double[samples][];
			xcounts = new int[samples];
			xids = new int[samples];
			reader.open();
			
			for(current = reader.next(); current != null; current = reader.next()){
				x[currentIdx] = new int[current.featureSize];
				xweights[currentIdx] = new double[current.featureSize];
				for(int i = 0 ; i < current.featureSize; i++){
					x[currentIdx][i] = current.features[i];
					xweights[currentIdx][i] = current.weights[i];
				}
				xlabels[currentIdx] = current.label;
				xcounts[currentIdx] = current.count;
				xids[currentIdx] = current.id;
				currentIdx += 1;
			}
			current = reader.sample;
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
		public Vector next() throws IOException {
			if (currentIdx >= this.xlabels.length){
				return null;
			}
			else{
//				try{
				current.label = xlabels[currentIdx];
				current.count = xcounts[currentIdx];
				current.id = xids[currentIdx];
				current.featureSize = x[currentIdx].length; //always >= 0 
				for(int i = 0 ; i < x[currentIdx].length; i++){
					current.features[i] = x[currentIdx][i];
					current.weights[i] = (float)xweights[currentIdx][i];
				}
//				}catch (NullPointerException e){
//					System.out.println(currentIdx);
//				}
			}
			currentIdx += 1;
			return current;
		}
		
	}
}
