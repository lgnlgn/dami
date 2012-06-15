package org.dami.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * One should specify deserializer for data storage. Careful mapping should be made. 
 * @author lgn
 *
 */
public abstract class DataSerializer {
	String textInput = null;
	String dbPrefix = null;
	
    protected static Class<? extends DataStorage> dataDeserializer = null;
	
	protected void initFilePath(String textInput, String outPrefix) throws IOException{
		File f = new File(textInput);
		
		if (f.exists() && f.isFile() ){
			this.textInput = textInput;
			this.dbPrefix = outPrefix;
		}else{
			throw new IOException("Data File NOT FOUND!");
		}
		
	}
	
	abstract public void transform()throws IOException;
	
	public static Class<? extends DataStorage> getDataDeserializer(){
		return dataDeserializer;
	}
	
	public String toString(){		
		return String.format("format:LabelFeatureWeight\ndecode:%s\ninput:%s\toutput:%s", 
				getDataDeserializer().getName(), this.textInput, this.dbPrefix);
	}
	
	
	/**
	 * data format for classification, same as for libsvm 
	 * format -> label featureid1:weight1 featureid2:weight2 ...
	 * label : less than 256 classes, can be < 0
	 * weight must be in (0, 1]
	 * deserializer : DataStorage.LabelFeatureWeightFileStorage
	 * @author lgn
	 *
	 */
	public static class LabelFeatureWeightData extends DataSerializer{
		static{
			dataDeserializer = DataStorage.LabelFeatureWeightFileStorage.class;
		}
		String countLabelSplit = null;
		String labelFeatureSplit = null;
		String featureSplit = null;
		String featureWeightSplit = null;
		
		/**
		 * specify input output, data format default to that for libsvm
		 * labelFeatureSplit = "\\s+"
		 * featureSplit = "\\s+"
		 * featureWeightSplit = ":"
		 * @param textInput
		 * @param dbPrefix
		 * @throws IOException
		 */
		public LabelFeatureWeightData(String textInput, String dbPrefix) throws IOException {
			this(textInput, dbPrefix, null, "\\s+", "\\s+", ":");
		}

		public LabelFeatureWeightData(String textInput, String dbPrefix, String countLabelSplit,
				String labelFeatureSplit, String featureSplit, String featureWeightSplit) throws IOException {
			this.initFilePath(textInput, dbPrefix);
			this.labelFeatureSplit = labelFeatureSplit;
			this.featureWeightSplit = featureWeightSplit;
			this.featureSplit = featureSplit;
			this.countLabelSplit = countLabelSplit;
		}
		
		@Override
		public void transform() throws IOException {
			// TODO Auto-generated method stub
			int maxfeatureid = 0;
			int samples = 0;
			HashMap<Integer, int[]> labelInfoBag = new HashMap<Integer, int[]>(); // { label : [id, counts] ... }
			
			BufferedReader br = new BufferedReader(new FileReader(this.textInput));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.dbPrefix + ".ser"));
			byte[] weightArray = new byte[1];
			for(String line = br.readLine(); line!= null; line = br.readLine()){
				int sampleCount = 1; //default each line is a sample
				int label;
					
				String[] labelFeatures = null;
				if (this.countLabelSplit == null){ //default format
					labelFeatures = line.split(labelFeatureSplit,2);
				}else{ // if 
					String[] countlabelFeatures = line.split(countLabelSplit,2);
					sampleCount = Integer.parseInt(countlabelFeatures[0]);
					labelFeatures = countlabelFeatures[1].split(labelFeatureSplit, 2);
				}
				// label = +1 +2 ....
				label = Integer.parseInt(labelFeatures[0].startsWith("+")?labelFeatures[0].substring(1):labelFeatures[0]); 
				
				//original label ids are transformed to [0, 1, ...]
				int[] labelInfo = labelInfoBag.get(label);
				if (labelInfo == null){
					labelInfo = new int[]{labelInfoBag.size(), 1}; // original_label:[new_label, count]
					labelInfoBag.put(label, labelInfo);
				}else{
					labelInfo[1] += sampleCount;
				}
				if (labelFeatures[1].isEmpty())
					continue;
				bos.write(Utilities.int2outputbytes(sampleCount));

				String[] features = labelFeatures[1].split(featureSplit); 
				int first = (labelInfo[0]  << 24) | ((features.length) & 0xffffff); // label & #features
				bos.write(Utilities.int2outputbytes(first));
				
				for(int i = 0 ; i < features.length; i++){
					String[] fidAndWeight = features[i].split(this.featureWeightSplit);
//					if (fidAndWeight.length < 2){
//						System.out.println(line + " " + features.length);
//					}
					double weight = Double.parseDouble(fidAndWeight[1]);
					byte nweight = (byte)(weight * 255);
					weightArray[0] = nweight;
					int featureId = Integer.parseInt(fidAndWeight[0]);
					maxfeatureid = featureId > maxfeatureid ? featureId:maxfeatureid;
					bos.write(Utilities.int2outputbytes(featureId));
					bos.write(weightArray);
				}
				samples+=1;
			}
			bos.close();
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.dbPrefix + ".sta"));
			bw.write(String.format("%s=%d\n", Constants.MAXFEATUREID, maxfeatureid));
			bw.write(String.format("%s=%d\n", Constants.NUMBERS_SAMPLE, samples));
			bw.write(String.format("%s=%d\n", Constants.NUMBERS_CLASS, labelInfoBag.size()));
			StringBuilder sb = new StringBuilder();
			for(Entry<Integer, int[]> entry : labelInfoBag.entrySet()){
				sb.append(String.format("%d:%d:%d ", entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
			}
			bw.write(String.format("%s=%s\n", Constants.DATASET_INFO, sb.toString()));
			bw.write(String.format("%s=%s\n", Constants.DATADESERIALIZER, getDataDeserializer().getName()));
			bw.close();
		
		}

	
	}
	
	/**
	 * data format for frequent pattern mining, same as that in FIMI03 
	 * feature starts from 1
	 * deserializer : DataStorage.FeatureOnlyFileStorage
	 * @author lgn
	 *
	 */
	public static class FeatureOnlyData extends DataSerializer{
		static{
			dataDeserializer = DataStorage.FeatureOnlyFileStorage.class;
		}
		String splitRegex = null;
		
		public FeatureOnlyData(String textInput, String dbPrefix, String splitRegex) throws IOException {
			this.initFilePath(textInput, dbPrefix);
			this.splitRegex = splitRegex;
		}
		
		public FeatureOnlyData(String textInput, String dbPrefix) throws IOException {
			this.initFilePath(textInput, dbPrefix);
			this.splitRegex = "\\s+";
		}
		
		@Override
		public void transform() throws IOException {
			// TODO Auto-generated method stub
			int maxfeatureid = 0;
			int samples = 0;
			BufferedReader br = new BufferedReader(new FileReader(this.textInput));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.dbPrefix + ".ser"));
			for(String line = br.readLine(); line!= null; line = br.readLine()){
				String[] info = line.split(splitRegex);
				bos.write(Utilities.int2outputbytes(info.length)); //#features
				for(int i = 0 ; i < info.length; i++){
					int featureId = Integer.parseInt(info[0]);
					maxfeatureid = featureId > maxfeatureid ? featureId:maxfeatureid;
					bos.write(Utilities.int2outputbytes(featureId));
				}
				samples += 1;
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.dbPrefix + ".sta"));
			bw.write(String.format("%s=%d\n", Constants.MAXFEATUREID, maxfeatureid));
			bw.write(String.format("%s=%d\n", Constants.NUMBERS_SAMPLE, samples));
			bw.write(String.format("%s=%s\n", Constants.DATADESERIALIZER, getDataDeserializer().getName()));
			bw.close();
		}

		
	}
}

