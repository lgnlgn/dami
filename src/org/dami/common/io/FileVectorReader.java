package org.dami.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;
import org.dami.common.collection.ByteArray;
import org.dami.common.collection.NumericTokenizer;

/**
 * Reads vector from file, either text or bytes. Parsing logic must be embedded in.
 * @author lgn
 *
 * 
 */
public abstract class FileVectorReader implements DataReader<Vector>{

	protected FileInputStream fis = null;
	protected BufferedInputStream istream = null;
	//	Vector sample = new Vector();
	protected String filePath ;
	ByteBuffer  buff;

	// whether ignore Vector's member
	protected Vector.Status vectorStatus = null;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Vector.Status getVectorParameter(){
		return this.vectorStatus;
	}

	public void setVectorParameter(Vector.Status vecStatus){
		this.vectorStatus = vecStatus;
	}


	@Override
	public void open() throws IOException {
		this.fis = new FileInputStream(this.filePath);
		this.istream = new BufferedInputStream(fis);
	}

	@Override
	public void close() throws IOException {
		this.istream.close();
		this.fis.close();
	}


	/**
	 * fromId toId [weight]
	 * @author lgn
	 *
	 */
	public static class TupleReader extends FileVectorReader{
		final static int MAXFEATURES = 500000;

		BufferedReader br ;
		String currentLine = null;

		Object[] currentInfo = null;

		String delim ;
		int currentIdx ;

		int aggIdx = 0;
		int weightColumn;

		int lastId = -1;
		boolean stopped = false;

		/**
		 * fromId toId [weight]
		 * @param filePath
		 * @param delimRegex Fields delimiter
		 * @param aggIdx  Index for aggregate id, 1 or 1, i.e., fromId or toId
		 * @param weightIndex link weight column. 3 for (fromId, toId, weight). less than 2 for no weightColumn
		 */
		public TupleReader(String filePath, String delimRegex, int aggIdx, int weightColumn){
			if (aggIdx > 1 || aggIdx < 0){
				throw new IllegalArgumentException("Parameter error! index for aggregate id MUST BE 0 OR 1");
			}
			//			sample = new Vector(MAXFEATURES);
			this.aggIdx = aggIdx;
			this.weightColumn = weightColumn;
			this.delim = delimRegex;
			this.filePath = filePath;
		}

		/**
		 * default format: delimiter = "\\s+", aggregate ID = fromId, without link weight
		 * @param filePath
		 */
		public TupleReader(String filePath){
			this(filePath, "\\s+", 0, -1);
		}

		/**
		 * default format: delimiter = "\\s+", without link weight
		 * @param filePath 
		 * @param aggIdx index for aggregate ID
		 */
		public TupleReader(String filePath, int aggIdx ){
			this(filePath, "\\s+", aggIdx, -1);
		}

		public void open() throws IOException {
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
			currentIdx = 0;
		}

		public void close() throws IOException {
			br.close();
			fis.close();
		}

		/**
		 * parse line to Object[]
		 * @param info
		 */
		private void parseLineInfo(String[] info){
			currentInfo[0] = Integer.parseInt(info[0]);
			currentInfo[1] = Integer.parseInt(info[1]);
			if (weightColumn > 2 )
				currentInfo[2] = Float.parseFloat(info[weightColumn - 1]);
			else
				currentInfo[2] = 1.0f;
		}

		private void fillVector(Vector sample){
			sample.features[currentIdx] = (Integer)currentInfo[aggIdx ^ 1];
			if (weightColumn > 2)
				sample.weights[currentIdx] = (Float)currentInfo[2];
			else
				sample.weights[currentIdx] = 1.0f;
		}

		public void next(Vector sample) throws IOException {
			if (stopped){
				sample.featureSize = -1;
				return;
			}
			if (currentInfo != null){ //last yielding 
				fillVector(sample);
				currentIdx += 1;
			}else{ //only first line 
				currentInfo = new Object[3];
			}
			for(currentLine = br.readLine(); currentLine!= null; currentLine = br.readLine()){
				String[] info = currentLine.split(delim);
				this.parseLineInfo(info);
				if (lastId != (Integer)currentInfo[aggIdx] && lastId != -1){ //meets a new id
					sample.id = lastId;
					lastId = (Integer)currentInfo[aggIdx]; //refresh aggregate id
					sample.featureSize = currentIdx;
					currentIdx = 0;
					return;
					//					return sample;
				}
				//otherwise 
				lastId = (Integer)currentInfo[aggIdx];
				if (currentIdx == sample.capacity()){ 
					//exceed vector's capacity, return new line with same aggregate id
					sample.id = lastId;
					sample.featureSize = currentIdx;
					currentIdx = 0;
					//					return sample;
				}
				fillVector(sample);
				currentIdx += 1;

			}
			//EOF
			sample.id = lastId;
			sample.featureSize = currentIdx;
			currentIdx = 0;
			stopped = true;
			//			return sample;
		}

	}


	public static class BytesReader extends FileVectorReader{

		byte[] tmpbytes = new byte[14]; //for featuresize id aggcount label
		int size = 2048;
		byte[] byteArray = null;

		int readBytes = 4;

		public BytesReader(String filePath, Vector.Status vs){
			this.filePath = filePath;
			this.setVectorParameter(vs);
		}

		public String toString(){
			return String.format("%s\n%s\tvector status:%d", this.getClass().getName(), this.filePath, this.vectorStatus.getVectorParameter());
		}

		public void setVectorParameter(Vector.Status vs){
			super.setVectorParameter(vs);
			readBytes = 0;
			if (vs.hasId)
				readBytes += 4;
			if (vs.hasCount)
				readBytes += 4;
			if (vs.hasLabel)
				readBytes += 2;

			if (vs.hasWeight){
				byteArray = new byte[size * 8]; 
			}else{
				byteArray = new byte[size * 4];
			}
		}


		@Override
		public void next(Vector sample) throws IOException {
			if (istream == null){
				throw new IOException("Reader not init yet!   Call open() explicitly");
			}
			int bytes = istream.read(tmpbytes, 0 , readBytes + 4);
			int index = 4;
			if (bytes < 0){ //EOF
				sample.featureSize = -1;
				//				return;
			}else{
				buff = ByteBuffer.wrap(tmpbytes);
				sample.featureSize = buff.getInt(0);
				if (vectorStatus.hasId){
					sample.id = buff.getInt(index);
					index += 4;
				}
				if (vectorStatus.hasCount){
					sample.count = buff.getInt(index);
					index += 4;
				}
				if (vectorStatus.hasLabel){
					sample.label = buff.getShort(index);
				}
				while(sample.featureSize >= sample.capacity()){
					sample.enlarge();
				}
				int start = 0;
				int toLoop = sample.featureSize / size;
				int remain = sample.featureSize % size;

				if (vectorStatus.hasWeight){ 
					for(int i = 0 ; i < toLoop; i++){
						//							System.out.println();
						int reads = istream.read(byteArray, 0, size << 3);
						buff = ByteBuffer.wrap(byteArray);
						for( int j = 0 ; j < reads; j+= 8){
							sample.features[start + (j>>> 3)] = buff.getInt(j);
							sample.weights[start + (j>>> 3)] = buff.getFloat(j + 4);
						}
						start += size;
					}
					int reads = istream.read(byteArray, 0,  remain << 3); // each pair with 8 bytes
					buff = ByteBuffer.wrap(byteArray);
					for( int j = 0 ; j < reads; j+= 8){
						sample.features[start + (j>>> 3)] = buff.getInt(j);
						sample.weights[start + (j>>> 3)] = buff.getFloat(j + 4);

					}
				}else{
					for(int i = 0 ; i < toLoop; i++){
						int reads = istream.read(byteArray, 0, size << 2);
						for( int j = 0 ; j < reads; j+= 4){
							sample.features[start + (j>>> 2)] = buff.getInt(j);
						}
						start += size;
					}
					int reads = istream.read(byteArray, 0,  remain << 2); // each pair with 4 bytes
					buff = ByteBuffer.wrap(byteArray);
					for( int j = 0 ; j < reads; j+= 4){
						sample.features[start + (j>>> 2)] = buff.getInt(j);
					}
				}

			}
			//			return sample;
		}

	}

	/**
	 * general line parser for FileVectorReader 
	 * set 2 delimiters : fields & keyvalue
	 * E.g. 
	 * [id] [aggregtecount] [label] key1[:value] key2[:value] key3[:value]
	 * delim = " "  , kvSplit = ":"
	 * @author lgn
	 *
	 */
	public static class LineReader extends FileVectorReader{
		String delim;
		String kvSplit;

		BufferedReader br ;

		/**
		 * [id] [aggregtecount] [label] key1[:value] key2[:value] key3[:value]
		 * delim = " "  , kvSplit = ":"
		 * @param filePath
		 * @param delim
		 * @param kvSplit
		 * @param readId
		 * @param readAggCount
		 * @param readLabel
		 * @param readWeight
		 */
		public LineReader(String filePath, String delim, String kvSplit, Vector.Status vs){
			this.filePath = filePath;
			this.delim = delim;
			this.kvSplit = kvSplit;
			this.setVectorParameter(vs);
		}


		@Override
		public void open() throws IOException {
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
		}

		@Override
		public void close() throws IOException {
			br.close();
			fis.close();
		}

		@Override
		public void next(Vector sample) throws IOException {

			String line = br.readLine();
			if (line == null){
				sample.featureSize = -1;
				//				return null;
				return;
			}
			if (line.isEmpty()){
				next(sample);
			}else{
				StringTokenizer st = new StringTokenizer(line, this.delim);
				if (vectorStatus.hasId){
					sample.id = Integer.parseInt(st.nextToken());
				}
				if (vectorStatus.hasCount){
					sample.count = Integer.parseInt(st.nextToken());
				}
				if (vectorStatus.hasLabel){
					String labelStr = st.nextToken();
					// label = +1 +2 ....
					sample.label = Integer.parseInt(labelStr.startsWith("+") ? labelStr.substring(1) : labelStr); 
				}
				int i = 0;
				int idx = 0;
				// if no features are to read, 
				// while loop will not be entered because of the StringTokenizer.hasMoreTokens
				while(st.hasMoreTokens()){
					if (i+1 >= sample.capacity())
						sample.enlarge();
					String featureInfo = st.nextToken();
					if (vectorStatus.hasWeight){
						idx = featureInfo.indexOf(kvSplit);
						sample.features[i] = Integer.parseInt(featureInfo.substring(0,idx));
						sample.weights[i]= Float.parseFloat(featureInfo.substring(idx+1));
					}else{
						sample.features[i] = Integer.parseInt(featureInfo);
					}
					i++;
				}
				sample.featureSize = i;
				//			return sample;
			}
		}

	}

	public static class FastLineReader extends FileVectorReader{
		String delim;
		String kvSplit;

		FileBytesReader br;
		NumericTokenizer nt;

		/**
		 * non-digit chars as split
		 * @param filePath
		 * @param vs
		 */
		public FastLineReader(String filePath, Vector.Status vs){
			this.filePath = filePath;
			this.setVectorParameter(vs);
			nt = new NumericTokenizer();
		}


		@Override
		public void open() throws IOException {
			br = new FileBytesReader(filePath);
		}

		@Override
		public void close() throws IOException {
			br.close();
		}

		@Override
		public void next(Vector sample) throws IOException {

			ByteArray line = br.readLine();
			if (line == null){
				sample.featureSize = -1;
				//				return null;
				return;
			}

			nt.load(line);
			if (vectorStatus.hasId){
				sample.id = (Integer)(nt.nextNumber());
			}
			if (vectorStatus.hasCount){
				sample.count = (Integer)(nt.nextNumber());
			}
			if (vectorStatus.hasLabel){
				sample.label = (Integer)(nt.nextNumber());
			}
			int i = 0;
			long kv = 0;

			while(nt.hasNext()){
				if (i+1 >= sample.capacity())
					sample.enlarge();
				if (vectorStatus.hasWeight){
					kv = nt.nextKeyValuePair();
					sample.features[i] = NumericTokenizer.extractFeatureId(kv);
					sample.weights[i]= NumericTokenizer.extractWeight(kv);
				}else{
					sample.features[i] = (Integer)(nt.nextNumber());
				}
				i++;
			}
			sample.featureSize = i;

		}

	}


	/**
	 * libsvm format 
	 * @param filePath 
	 * @return
	 */
	public static BytesReader normalClassificationFormatBytesReader(String filePath){
		// id feature weight
		return new BytesReader(filePath, new Vector.Status(0x8 + 0x4));
	}

	/**
	 * feature only . FIMI03 format
	 * @param filePath
	 * @return
	 */
	public static BytesReader normalFIMFormatBytesReader(String filePath){
		//feature
		return new BytesReader(filePath, new Vector.Status(0));
	}

	/**
	 * libsvm format 
	 * @param filePath 
	 * @return
	 */
	public static LineReader normalClassificationFormatLineReader(String filePath){
		// id feature weight
		return new LineReader(filePath, " ", ":", new Vector.Status( 0x8 + 0x4));
	}

	/**
	 * feature only . FIMI03 format
	 * @param filePath
	 * @return
	 */
	public static LineReader normalFIMFormatLineReader(String filePath){
		//feature
		return new LineReader(filePath, " ", ":", new Vector.Status(0));
	}

	public static TupleReader TupleWithoutWeightLineReader(String filePath){
		return new TupleReader(filePath);
	}


	public static BytesReader getBytesReaderFromSta(String serFilePath) throws IOException{
		FileReader r = new FileReader(serFilePath + Constants.STAT_SUFFIX);
		Properties prop = new Properties();
		prop.load(r);
		r.close();
		try{
			//TODO @SuppressWarnings("unchecked")?
			//			@SuppressWarnings("unchecked")
			Class<BytesReader> clz = (Class<BytesReader>) Class.forName(prop.getProperty(Constants.DATADESERIALIZER));
			Constructor<FileVectorReader.BytesReader> constructor = clz.getConstructor(String.class, Vector.Status.class);
			return constructor.newInstance(serFilePath, new Vector.Status(Utilities.getIntFromProperties(prop, Constants.VESTOC_STATUS)));
		}catch (Exception e) {
			System.err.println(Constants.DATADESERIALIZER + " is NOT BytesReader!");
			e.printStackTrace();
			return null;
		}
	}

}
