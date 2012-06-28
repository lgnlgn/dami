package org.dami.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import org.dami.common.Vector;

/**
 * Reads vector from file, either text or bytes. Parsing logic must be embedded in.
 * @author lgn
 *
 * 
 */
public abstract class FileVectorReader implements DataReader<Vector>{

	FileInputStream fis = null;
	BufferedInputStream istream = null;
	Vector sample = new Vector();
	String filePath ;
	ByteBuffer  buff;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void open() throws IOException {
		// TODO Auto-generated method stub
		this.fis = new FileInputStream(this.filePath);
		this.istream = new BufferedInputStream(fis);
	}

	@Override
	public void close() throws IOException {

		this.istream.close();
		this.fis.close();
	}

	public static class LabelFeatureWeightBytesReader extends FileVectorReader{

		byte[] tmp10bytes = new byte[10];
		int size = 2048;
		byte[] byteArray = new byte[size * 8];
		public LabelFeatureWeightBytesReader(String filePath){
			this.filePath = filePath;
		}
		


		@Override
		public Vector next() throws IOException {
			if (istream == null){
				throw new IOException("Reader not init yet!   Call open() explicitly");
			}
			int bytes = istream.read(tmp10bytes);
			if (bytes < 0){ //EOF
				return null;
			}else{
				buff = ByteBuffer.wrap(tmp10bytes);
				sample.count = buff.getInt(0);
				sample.label = buff.getShort(4);
				sample.featureSize = buff.getInt(6);
				while(sample.featureSize >= sample.capacity()){
					sample.enlarge();
				}
				int start = 0;
				int toLoop = sample.featureSize / size;
				int remain = sample.featureSize % size;

				for(int i = 0 ; i < toLoop; i++){
					int reads = istream.read(byteArray, 0, size << 3);
					for( int j = 0 ; j < reads; j+= 8){
						sample.features[start + (j>>> 3)] = buff.getInt(j);
						sample.weights[start + (j>>> 3)] = buff.getFloat(j + 4);
					}
					start += size;
				}
				int reads = istream.read(byteArray, 0,  remain << 3);
				buff = ByteBuffer.wrap(byteArray);
				for( int j = 0 ; j < reads; j+= 8){
					sample.features[start + (j>>> 3)] = buff.getInt(j);
					sample.weights[start + (j>>> 3)] = buff.getFloat(j + 4);
				}
			}
			return sample;
		}
		
	}
	
	public static class FeatureOnlyBytesReader extends FileVectorReader{

		byte[] tmp4bytes = new byte[4];
		int size = 2048;
		byte[] byteArray = new byte[size * 4];
		
		public FeatureOnlyBytesReader(String filePath){
			this.filePath = filePath;
		}
		

		@Override
		public Vector next() throws IOException {
			// TODO Auto-generated method stub
			int bytes = istream.read(tmp4bytes);
			if (bytes < 0){
				return null;
			}else{
				buff = ByteBuffer.wrap(tmp4bytes);
				sample.featureSize = buff.getInt();
				while(sample.featureSize >= sample.capacity()){
					sample.enlarge();
				}
				int start = 0;
				int toLoop = sample.featureSize / size;
				int remain = sample.featureSize % size;

				for(int i = 0 ; i < toLoop; i++){
					int reads = istream.read(byteArray, 0, size << 2);
					for( int j = 0 ; j < reads; j+= 4){
						sample.features[start + (j>>> 2)] = buff.getInt(j);
					}
					start += size;
				}
				int reads = istream.read(byteArray, 0,  remain);
				for( int j = 0 ; j < reads; j+= 4){
					sample.features[start + (j>>> 2)] = buff.getInt(j);
				}
			}
			return sample;

		}
		
	}
	
	public static class LabelFeatureWeightLineReader extends FileVectorReader{
		String countLabelSplit = null;
		String labelFeatureSplit = null;
		String featureSplit = null;
		String KVSplit = null;
		
		BufferedReader br ;
		public LabelFeatureWeightLineReader(String filePath){
			this(filePath,  null, " ", " ", ":");
		}
		

		public LabelFeatureWeightLineReader( String filePath, String countLabelSplit,
				String labelFeatureSplit, String featureSplit, String KVSplit) {
			this.filePath = filePath;
			this.labelFeatureSplit = labelFeatureSplit;
			this.KVSplit = KVSplit;
			this.featureSplit = featureSplit;
			this.countLabelSplit = countLabelSplit;
		}
		
		
		@Override
		public void open() throws IOException {
			// TODO Auto-generated method stub
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
		}

		@Override
		public void close() throws IOException {
			br.close();	
			fis.close();
		}

		@Override
		public Vector next() throws IOException {
			// TODO Auto-generated method stub
			String line = br.readLine();
			if (line == null)
				return null;
			int sampleCount = 1;
			String labelFeatureStr = null;
			int idx = 0;
			if (this.countLabelSplit == null){ //default format
				labelFeatureStr = line;
			}else{ // if 
				idx = line.indexOf(countLabelSplit);
				sampleCount = Integer.parseInt(line.substring(0, idx));
				labelFeatureStr = line.substring(idx + 1);
			}
			
			idx = labelFeatureStr.indexOf(labelFeatureSplit);
			String labelStr = labelFeatureStr.substring(0, idx);
			// label = +1 +2 ....
			sample.label = Integer.parseInt(labelStr.startsWith("+") ? labelStr.substring(1) : labelStr); 
			sample.count = sampleCount;
			String featureWeightStr = labelFeatureStr.substring(idx + 1);
			if (featureWeightStr.isEmpty())
				sample.featureSize = 0;
			else{
				StringTokenizer kvTokenizer = new StringTokenizer(featureWeightStr, featureSplit);
				int i = 0;
				while(kvTokenizer.hasMoreTokens()){
					String fw = kvTokenizer.nextToken();
					idx = fw.indexOf(KVSplit);
					if (i+1 >= sample.capacity())
						sample.enlarge();
					sample.features[i] = Integer.parseInt(fw.substring(0,idx));
					sample.weights[i]= Float.parseFloat(fw.substring(idx+1));
					i += 1;
				}
				sample.featureSize = i;
			}
			return sample;
		}
	}
	
	public static class FeatureOnlyLineReader extends FileVectorReader{

		BufferedReader br ;
		String delim ;
		
		public FeatureOnlyLineReader(String filePath, String delim){
			this.filePath = filePath;
			this.delim = delim;
		}
		
		public FeatureOnlyLineReader(String filePath){
			this(filePath, " ");
		}
		
		@Override
		public void open() throws IOException {
			// TODO Auto-generated method stub
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
		}

		@Override
		public void close() throws IOException {
			br.close();
			fis.close();
		}

		@Override
		public Vector next() throws IOException {
			// TODO Auto-generated method stub
			String line = br.readLine();
			if (line == null)
				return null;
			StringTokenizer st = new StringTokenizer(line, this.delim);
			int i = 0;
			while(st.hasMoreTokens()){
				if (i+1 >= sample.capacity())
					sample.enlarge();
				sample.features[i] = Integer.parseInt(st.nextToken());
				i++;
			}
			sample.featureSize = i;
			return sample;
		}
		
	}
	
}
