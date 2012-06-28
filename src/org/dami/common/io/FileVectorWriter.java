package org.dami.common.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;

/**
 * writer vector to file, either texts or bytes
 * @author lgn
 *
 */
public abstract class FileVectorWriter implements DataWriter<Vector>{

	String outPrefix = null;
	FileOutputStream fos = null;
	BufferedOutputStream ostream;
	Vector current;
	
	
	public String getOutPrefix() {
		return outPrefix;
	}

	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}

	public void open() throws IOException {
		this.fos = new FileOutputStream(this.outPrefix);
		this.ostream = new BufferedOutputStream(fos);
	}

	public void close() throws IOException {
		this.ostream.flush();
		this.ostream.close();
		this.fos.close();
	}

	public static class LabelFeatureWeightBytesWriter extends FileVectorWriter{
		
		public LabelFeatureWeightBytesWriter(String prefix) {
			this.outPrefix = prefix;
		}

		@Override
		public Class<? extends DataReader<Vector>> getDeserClass() {
			// TODO Auto-generated method stub
			return FileVectorReader.LabelFeatureWeightBytesReader.class;
		}


		@Override
		public void write(Vector sample) throws IOException {
			// TODO Auto-generated method stub
			ostream.write(Utilities.int2outputbytes(sample.count));
			ostream.write(Utilities.short2outputbytes((short)sample.label));
			ostream.write(Utilities.int2outputbytes(sample.featureSize));
			for(int i = 0 ; i < sample.featureSize; i++){
				ostream.write(Utilities.int2outputbytes(sample.features[i]));
				ostream.write(Utilities.float2OutputBytes((float)sample.weights[i]));
			}
		}
		
	}
	
	public static class FeatureOnlyBytesWriter extends FileVectorWriter{
	
		public FeatureOnlyBytesWriter(String prefix) {
			this.outPrefix = prefix;
		}
		@Override
		public Class<? extends DataReader<Vector>> getDeserClass() {
			// TODO Auto-generated method stub
			return FileVectorReader.FeatureOnlyBytesReader.class;
		}

		@Override
		public void write(Vector sample) throws IOException {
			// TODO Auto-generated method stub
			ostream.write(Utilities.int2outputbytes(sample.featureSize));
			for(int i = 0 ; i < sample.featureSize; i++){
				ostream.write(Utilities.int2outputbytes(sample.features[i]));
			}
		}
		
	}
	
	public static class LabelFeatureWeightLineWriter extends FileVectorWriter{

		@Override
		public Class<? extends DataReader<Vector>> getDeserClass() {
			// TODO Auto-generated method stub
			return FileVectorReader.LabelFeatureWeightLineReader.class;
		}

		@Override
		public void write(Vector sample) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class FeatureOnlyLineWriter extends FileVectorWriter{
		
		String delim = null;
		public FeatureOnlyLineWriter(String prefix, String delim){
			this.delim = delim;
			this.outPrefix = prefix;
		}
		
		public FeatureOnlyLineWriter(String prefix){
			this(prefix, " ");
		}
		
		@Override
		public Class<? extends DataReader<Vector>> getDeserClass() {
			// TODO Auto-generated method stub
			return FileVectorReader.FeatureOnlyLineReader.class;
		}


		@Override
		public void write(Vector sample) throws IOException {
			// TODO Auto-generated method stub
			for(int i = 0 ; i < sample.featureSize; i++){
				String fid = sample.features[i] + delim;
				ostream.write(fid.getBytes() );
			}
			ostream.write(Constants.ENDL.getBytes());
		}
		
	}
}
