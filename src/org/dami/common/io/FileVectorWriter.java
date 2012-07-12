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

	protected String outPrefix = null;
	protected FileOutputStream fos = null;
	protected BufferedOutputStream ostream;

	
	// whether ignore Vector's member
	protected Vector.Status vectorStatus = null;
	
	public String getOutPrefix() {
		return outPrefix;
	}

	public Vector.Status getVectorStatus(){
		return this.vectorStatus;
	}
	
	public void setVectorStatus(Vector.Status vecStatus){
		this.vectorStatus = vecStatus;
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

	/**
	 * featureSize [id] [aggrC] [label] (feature weight)...
	 * @author lgn
	 *
	 */
	public static class BytesWriter extends FileVectorWriter{
		
		
		public BytesWriter(String db, Vector.Status vs){
			this.outPrefix = db;
			this.setVectorStatus(vs);
		}
		

		public Class<? extends DataReader<Vector>> getDeserClass() {
			return FileVectorReader.BytesReader.class;
		}

		@Override
		public void write(Vector sample) throws IOException {
			ostream.write(Utilities.int2outputbytes(sample.featureSize));
			if (vectorStatus.hasId){
				ostream.write(Utilities.int2outputbytes(sample.id));
			}
			if (vectorStatus.hasCount){
				ostream.write(Utilities.int2outputbytes(sample.count));
			}
			if (vectorStatus.hasLabel){
				ostream.write(Utilities.short2outputbytes((short)sample.label));
			}
//			if (vectorStatus.hasFeature)
				if (vectorStatus.hasWeight){
					for(int i = 0 ; i < sample.featureSize; i++){
						ostream.write(Utilities.int2outputbytes(sample.features[i]));
						ostream.write(Utilities.float2OutputBytes(sample.weights[i]));
					}
				}else{
					for(int i = 0 ; i < sample.featureSize; i++){
						ostream.write(Utilities.int2outputbytes(sample.features[i]));
					}
				}
//			else{
//				//no need to write anything
//				;
//			}
		}
	}
	
	
	/**
	 * [id] [aggrC] [label] (feature weight)...
	 * @author lgn
	 *
	 */
	public static class LineWriter extends FileVectorWriter{
		String delim;
		String kvSplit;
	
		byte[] endl = Constants.ENDL.getBytes();
		
		
		public LineWriter(String filePath, String delim, String kvSplit, Vector.Status vs) {
			this.outPrefix = filePath;
			this.delim = delim;
			this.kvSplit = kvSplit;

			this.setVectorStatus(vs);
		}
		
		
		@Override
		public Class<? extends DataReader<Vector>> getDeserClass() {
			// TODO Auto-generated method stub
			return FileVectorReader.LineReader.class;
		}

		@Override
		public void write(Vector sample) throws IOException {
			if (vectorStatus.hasId){
				ostream.write(String.valueOf(sample.id).getBytes());
			}
			if (vectorStatus.hasCount){
				ostream.write(String.valueOf(sample.count).getBytes());
			}
			if (vectorStatus.hasLabel){
				ostream.write(String.valueOf(sample.label).getBytes());
			}
//			if (vectorStatus.hasFeature)
				if (vectorStatus.hasWeight){
					for(int i = 0 ; i < sample.featureSize; i++){
						ostream.write((sample.features[i] + kvSplit + sample.weights[i] + delim).getBytes());
					}
				}else{
					for(int i = 0 ; i < sample.featureSize; i++){
						ostream.write((sample.features[i] + delim).getBytes());
					}
				}
//			else{
//				//no need to write anything 
//				;
//			}
			ostream.write(endl);
		}
	}

	/**
	 * libsvm format 
	 * @param filePath 
	 * @return
	 */
	public static BytesWriter normalClassificationFormatBytesWriter(String filePath){
		// id feature weight
		return new BytesWriter(filePath, Vector.normalClassificationFormat());
	}
	
	/**
	 * feature only . FIMI03 format
	 * @param filePath
	 * @return
	 */
	public static BytesWriter normalFIMFormatBytesWriter(String filePath){
		// feature only
		return new BytesWriter(filePath, Vector.normalFIMFormat());
	}
	
	/**
	 * libsvm format 
	 * @param filePath 
	 * @return
	 */
	public static LineWriter normalClassificationFormatLineWriter(String filePath){
		// id feature weight
		return new LineWriter(filePath, " ", ":", Vector.normalClassificationFormat());
	}
	
	/**
	 * feature only . FIMI03 format
	 * @param filePath
	 * @return
	 */
	public static LineWriter normalFIMFormatLineWriter(String filePath){
		// feature only
		return new LineWriter(filePath, " ", ":", Vector.normalFIMFormat());
	}
	
}
