package org.lgn.classification.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.lgn.dami.common.Constants;
import org.lgn.dami.common.Sample;


public abstract class DataSet {
	
	public abstract Sample next() throws IOException;
	
	public abstract void reOpenDataSet() throws IOException;

	public abstract Map<String, Object> getDataSetInfo() ;

	public abstract void close()  throws IOException;
	

	public static class SerializedStreamDataSet extends DataSet{
		
		Map<String, Object> dataStatus = null;
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		Sample current = null;
		String dbprefix = null;
		byte[] tmp4byte = new byte[4];
		ByteBuffer buff;
		
		public SerializedStreamDataSet(String dbprefix) throws IOException{
			this.dbprefix = dbprefix;
			dataStatus = new HashMap<String, Object>();
			Properties p = new Properties();
			Reader reader = new FileReader(this.dbprefix + ".sta");
			p.load(reader);
			reader.close();
			dataStatus.put(Constants.NUMBERS_SAMPLE, Integer.parseInt((String)p.getProperty(Constants.NUMBERS_SAMPLE)));
			dataStatus.put(Constants.MAXFEATUREID, Integer.parseInt((String)p.getProperty(Constants.MAXFEATUREID)));
			dataStatus.put(Constants.NUMBERS_CLASS, Integer.parseInt((String)p.getProperty(Constants.NUMBERS_CLASS)));

			dataStatus.put(Constants.DATASET_INFO, p.getProperty(Constants.DATASET_INFO));
			
			this.open();
			
			current = new Sample((Integer)dataStatus.get(Constants.MAXFEATUREID));
		}
		
		private void open() throws FileNotFoundException{
			fis = new FileInputStream(this.dbprefix + ".ser");
			bis = new BufferedInputStream(fis);
		}
		
		@Override
		public Sample next() throws IOException {
			// TODO Auto-generated method stub
			int bytes = bis.read(tmp4byte);
			if (bytes == -1){
				this.close();
				return null;
			}else{
				buff = ByteBuffer.wrap(tmp4byte);
				int sampleInfo = buff.getInt();
				int featureSize = sampleInfo & 0xffffff;
				int label = (sampleInfo & 0xff000000) >>> 24;
				current.label = label;
				current.featureSize = featureSize;
				for(int i = 0 ; i < featureSize; i++){
					bis.read(tmp4byte);
					int weightAndFid = ByteBuffer.wrap(tmp4byte).getInt();
					current.features[i] = weightAndFid & 0xffffff;
					current.weights[i]= ((weightAndFid & 0xff000000) >>> 24 ) / (Byte.MAX_VALUE + 0.0);
				}
				return current;
			}
		}

		@Override
		public void reOpenDataSet() throws IOException {
			// TODO Auto-generated method stub
			this.close();
			this.open();
		}

		@Override
		public Map<String, Object> getDataSetInfo() {
			// TODO Auto-generated method stub
			return this.dataStatus;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			bis.close();
			fis.close();
		}
		
	}

	public static class MemoryDataSet extends SerializedStreamDataSet{
		
		int[] y = null; //include label 
		int[] fSArray = null;
		int[][] x = null;
		int currentIdx = -1;
		
		public MemoryDataSet(String dbprefix) throws IOException{
			super(dbprefix);
			
			y = new int[(Integer)dataStatus.get(Constants.NUMBERS_SAMPLE)];
			fSArray = new int[(Integer)dataStatus.get(Constants.NUMBERS_SAMPLE)];
			x = new int[(Integer)dataStatus.get(Constants.NUMBERS_SAMPLE)][];
			int idx = 0;
			for(int bytes = bis.read(tmp4byte) ; bytes != -1; bytes = bis.read(tmp4byte)){
				buff = ByteBuffer.wrap(tmp4byte);
				int sampleInfo = buff.getInt();
				int featureSize = sampleInfo & 0xffffff;
				int label = (sampleInfo & 0xff000000) >>> 24;
				y[idx] = label;
				fSArray[idx] = featureSize;
				x[idx] = new int[featureSize];
				for(int i = 0 ; i < featureSize; i++){
					bis.read(tmp4byte);
					int weightAndFid = ByteBuffer.wrap(tmp4byte).getInt();
					x[idx][i] = weightAndFid;
				}
				idx += 1;
			}
			this.close();
		}
		
		@Override
		public Sample next() throws IOException {
			// TODO Auto-generated method stub
			if (currentIdx >= this.y.length){
				this.close();
				return null;
			}
			current.label = y[currentIdx];
			current.featureSize = fSArray[currentIdx];
			for(int i = 0 ; i < current.featureSize; i++){
				current.features[i] = x[currentIdx][i] & 0xffffff;
				current.weights[i]= ((x[currentIdx][i] & 0xff000000) >>> 24 ) / (Byte.MAX_VALUE + 0.0);		
			}
			currentIdx += 1;
			return current;
		}

		@Override
		public void reOpenDataSet() throws IOException {
			// TODO Auto-generated method stub
			currentIdx = 0;
		}

		
	}

	
}
