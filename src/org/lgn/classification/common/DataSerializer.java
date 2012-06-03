package org.lgn.classification.common;

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


import org.lgn.dami.common.Constants;
import org.lgn.dami.common.Utilities;

/**
 * the data format MUST BE exactly same as the liblinear input!
 * @author lgn
 *
 */
public class DataSerializer {
	String filePath = null;
	public DataSerializer(String filePath) throws IOException{
		File f = new File(filePath);
		if (f.exists() && f.isFile()){
			this.filePath = filePath;
		}else{
			throw new IOException("Data File NOT FOUND!");
		}
		
	}
	
	public void transform() throws IOException{
		int maxfeatureid = 0;
		int samples = 0;
		HashMap<Integer, int[]> labelInfoBag = new HashMap<Integer, int[]>(); // { label : [id, counts] ... }
		
		BufferedReader br = new BufferedReader(new FileReader(this.filePath));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.filePath + ".ser"));
		
		for(String line = br.readLine(); line!= null; line = br.readLine()){
			String[] info = line.split("\\s+");
			int label = Integer.parseInt(info[0].startsWith("+")?info[0].substring(1):info[0]); 
			int[] labelInfo = labelInfoBag.get(label);
			if (labelInfo == null){
				labelInfo = new int[]{labelInfoBag.size(), 1};
				labelInfoBag.put(label, labelInfo);
			}else{
				labelInfo[1] += 1;
			}
			
			int first = (labelInfo[0]  << 24) | ((info.length-1) & 0xffffff); // label & #feature
			bos.write(Utilities.int2outputbytes(first));
			for(int i = 1 ; i < info.length; i++){
				String[] fidAndWeight = info[i].split(":");
				int featureId = Integer.parseInt(fidAndWeight[0]) & 0xffffff;
				double weight = Double.parseDouble(fidAndWeight[1]);
				int nweight = (int)(weight * Byte.MAX_VALUE) << 24; //weight split into 255 parts, e.g. precision only at 0.004
				maxfeatureid = featureId > maxfeatureid ? featureId:maxfeatureid;
				bos.write(Utilities.int2outputbytes( nweight | featureId ));
			}
			samples += 1;
		}
		bos.close();
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.filePath + ".sta"));
		bw.write(String.format("%s=%d\n", Constants.MAXFEATUREID, maxfeatureid));
		bw.write(String.format("%s=%d\n", Constants.NUMBERS_SAMPLE, samples));
		bw.write(String.format("%s=%d\n", Constants.NUMBERS_CLASS, labelInfoBag.size()));
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, int[]> entry : labelInfoBag.entrySet()){
			sb.append(String.format("%d:%d:%d ", entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
		}
		bw.write(String.format("%s=%s\n", Constants.DATASET_INFO, sb.toString()));
		bw.close();
	}
	
	
	public static void main(String[] args) throws IOException{
		String file = "e:/data/covtype";
		DataSerializer ds = new DataSerializer(file);
		ds.transform();
	}
}
