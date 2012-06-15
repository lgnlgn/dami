package org.dami.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * deserialization embeded here
 * @author lgn
 *
 */
public abstract class DataStorage {
	
	Properties dataStatus = null;
	BufferedInputStream bis = null;
	FileInputStream fis = null;
	Sample current = null;
	String dbprefix = null;
	byte[] tmp4bytes = new byte[4];
	ByteBuffer buff;
	
	protected DataStorage(String dbprefix) throws IOException{
		this.dbprefix = dbprefix;
		Properties p = new Properties();
		Reader reader = new FileReader(this.dbprefix + ".sta");
		p.load(reader);
		reader.close();
		dataStatus = p;
		current = new Sample(Utilities.getIntFromProperties(dataStatus, Constants.MAXFEATUREID));

	}
	
	protected abstract void open() throws IOException;
	
	public abstract void close() throws IOException;
	public abstract Sample next() throws IOException;
	
	public void reOpenData() throws IOException{
		this.close();
		this.open();
	}

	public Properties getDataSetInfo() {
		return this.dataStatus;
	}

	public static class LabelFeatureWeightFileStorage extends DataStorage{
		final static Double denominator = 255.0;
		protected byte[] tmp5bytes = new byte[5];

		public LabelFeatureWeightFileStorage(String dbprefix) throws IOException{
			super(dbprefix);
			this.open();
		}
		

		public Sample next() throws IOException {
			// TODO Auto-generated method stub
			int bytes = bis.read(tmp4bytes);
			if (bytes == -1){
				this.close();
				return null;
			}else{
				buff = ByteBuffer.wrap(tmp4bytes); //sample_count
				int sampleCount = buff.getInt();
				current.count = sampleCount;
				bis.read(tmp4bytes);
				buff = ByteBuffer.wrap(tmp4bytes);//label & #features
				int sampleInfo = buff.getInt(); 
				int featureSize = sampleInfo & 0xffffff;
				int label = (sampleInfo & 0xff000000) >>> 24;
				current.label = label;
				current.featureSize = featureSize;
				byte weight = 0;
				int feature= 0;
				for(int i = 0 ; i < featureSize; i++){
					bis.read(tmp5bytes);
					buff = ByteBuffer.wrap(tmp5bytes);
					
					feature = buff.getInt();
					weight = buff.get(4);
					current.features[i] = feature;
					current.weights[i]= ((int)(weight) & 0xff) / denominator;
				}
				return current;
			}
		}

		protected void open() throws IOException {
			fis = new FileInputStream(this.dbprefix + ".ser");
			bis = new BufferedInputStream(fis);
		}

		public void close() throws IOException {
			bis.close();
			fis.close();
		}	
	}

	public static class LabelFeatureWeightRAMStorage extends DataStorage{
		final static Double denominator = 255.0;
		
		int[] y = null; //include label 
		int[][] x = null;
		byte[][] xweights = null;
		int[] xcounts = null;
		int currentIdx = -1;
		
		protected byte[] tmp5bytes = new byte[5];
		
		public LabelFeatureWeightRAMStorage(String dbprefix) throws IOException{
			super(dbprefix);// open DB
			int samples = Utilities.getIntFromProperties(dataStatus, Constants.NUMBERS_SAMPLE);
			y = new int[samples];
			x = new int[samples][];
			xweights = new byte[samples][];
			xcounts = new int[samples];
			
			int idx = 0;
			int sampleInfo;
			int sampleCount;
			int featureSize;
			int label;
			byte weight = 0;
			int feature= 0;
			
			fis = new FileInputStream(this.dbprefix + ".ser");
			bis = new BufferedInputStream(fis);
			for(int bytes = bis.read(tmp4bytes) ; bytes != -1; bytes = bis.read(tmp4bytes)){
				buff = ByteBuffer.wrap(tmp4bytes); //sample_count
				sampleCount = buff.getInt();
				bis.read(tmp4bytes);
				buff = ByteBuffer.wrap(tmp4bytes); //label & #features
				sampleInfo = buff.getInt();
				featureSize = sampleInfo & 0xffffff;
				label = (sampleInfo & 0xff000000) >>> 24;
				y[idx] = label;
				x[idx] = new int[featureSize];
				xweights[idx] = new byte[featureSize];
				for(int i = 0 ; i < featureSize; i++){
					bis.read(tmp5bytes);
					buff = ByteBuffer.wrap(tmp5bytes);
					
					feature = buff.getInt();
					weight = buff.get(4);
					
					x[idx][i] = feature;
					xweights[idx][i] = weight;
				}
				idx += 1;
			}
			bis.close();
			fis.close();
			currentIdx = 0;
		}

		public Sample next() throws IOException {
			// TODO Auto-generated method stub
			if (currentIdx >= this.y.length){
				this.close();
				return null;
			}
			current.label = y[currentIdx];
			current.featureSize = x[currentIdx].length;
			for(int i = 0 ; i < current.featureSize; i++){
				current.features[i] = x[currentIdx][i] ;
				current.weights[i]= ((int)(xweights[currentIdx][i] ) & 0xff) / denominator;		
			}
			currentIdx += 1;
			return current;
		}


		protected void open() throws IOException {
			currentIdx = 0;
		}


		public void close() throws IOException {
			bis.close();
			fis.close();
			currentIdx =  this.y.length;
		}
	}

	/**
	 * decoder for frequent itemset mining data
	 * @author lgn
	 *
	 */
	public static class FeatureOnlyFileStorage extends DataStorage{

		protected FeatureOnlyFileStorage(String dbprefix) throws IOException {
			super(dbprefix);
		}

		protected void open() throws IOException {
			fis = new FileInputStream(this.dbprefix + ".ser");
			bis = new BufferedInputStream(fis);
		}

		public void close() throws IOException {
			bis.close();
			fis.close();
		}	

		public Sample next() throws IOException {
			int bytes = bis.read(tmp4bytes);
			if (bytes == -1){
				this.close();
				return null;
			}else{
				buff = ByteBuffer.wrap(tmp4bytes);
				current.featureSize = buff.getInt();
				int feature= 0;
				for(int i = 0 ; i < current.featureSize; i++){
					bis.read(tmp4bytes);
					buff = ByteBuffer.wrap(tmp4bytes);
					current.features[i] = feature;
				}
				return current;
			}
		}
	}
	
	public static class FeatureOnlyRAMStorage extends DataStorage{
		
		int currentIdx = -1;
		int[][] x = null;
		
		protected FeatureOnlyRAMStorage(String dbprefix) throws IOException {
			super(dbprefix);

			int samples = Utilities.getIntFromProperties(dataStatus, Constants.NUMBERS_SAMPLE);
			x = new int[samples][];
			
			int idx = 0;
			int featureSize;
			
			for(int bytes = bis.read(tmp4bytes) ; bytes != -1; bytes = bis.read(tmp4bytes)){
				buff = ByteBuffer.wrap(tmp4bytes);
				featureSize = buff.getInt();
				x[idx] = new int[featureSize];
				for(int i = 0 ; i < featureSize; i++){
					bis.read(tmp4bytes);
					buff = ByteBuffer.wrap(tmp4bytes);
					x[idx][i] = buff.getInt();
				}
			}
			this.reOpenData();
		}

		protected void open() throws IOException {
			currentIdx = 0;
		}

		public void close() throws IOException {
			fis.close();
			bis.close();
			currentIdx = Utilities.getIntFromProperties(dataStatus, Constants.NUMBERS_SAMPLE);
		}

		public Sample next() throws IOException {
			// TODO Auto-generated method stub
			if (currentIdx >= this.x.length){
				this.close();
				return null;
			}else{
				for(int i = 0 ; i < x[currentIdx].length; i++)
					current.features[i] = x[currentIdx][i];
				current.featureSize = x[currentIdx].length;
				currentIdx += 1;
				return current;
			}
		}
		
	}
}
