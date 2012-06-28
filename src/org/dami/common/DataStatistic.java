package org.dami.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public abstract class DataStatistic {
	abstract public void sampleInfoStat(Vector sample);
	
	abstract public void featureStat(Vector sample, int index);
	
	/**
	 * return key=value format string
	 * @return
	 * @throws IOException
	 */
	abstract public String getNextStat() throws IOException;
	
	public String getOutputSuffix(){
		return Constants.STAT_SUFFIX;
	}
	
	public static class LabelStatistic extends DataStatistic{
		HashMap<Integer, int[]> labelInfoBag = new HashMap<Integer, int[]>();
		int i = 0;
		public void sampleInfoStat(Vector sample) {
			int[] labelInfo = labelInfoBag.get(sample.label);
			if (labelInfo == null){
				labelInfoBag.put(sample.label, new int[]{labelInfoBag.size(), sample.count});
			}else{
				labelInfo[1] += sample.count;
			}
		}

		public String getNextStat() throws IOException {
			if (i == 0){
				StringBuilder sb = new StringBuilder();
				for(Entry<Integer, int[]> entry : labelInfoBag.entrySet()){
					sb.append(String.format("%d:%d:%d ", entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
				}
				i += 1;
				return String.format("%s=%s", Constants.DATASET_INFO, sb.toString());
			}else if (i == 1){
				i += 1;
				return String.format("%s=%d", Constants.NUMBER_CLASSES, labelInfoBag.size());
			}
			else{
				return null;
			}
		}

		@Override
		public void featureStat(Vector sample, int index) {
			//do nothing;
			
		}

		
	}
	
	public static class NormalStatistic extends DataStatistic{
		int maxFeatureId = -1;
		int samples = 0;
		long features = 0;
		int i = 0;
		public void featureStat(Vector sample, int index) {
			
			if (sample.features[index] > maxFeatureId)
				maxFeatureId = sample.features[index];
			features += 1;
			
		}

		

		@Override
		public void sampleInfoStat(Vector sample) {
			samples += 1;
		}

		@Override
		public String getNextStat() throws IOException {
			// TODO Auto-generated method stub
			if (i == 0){
				i += 1;
				return String.format("%s=%d", Constants.MAXFEATUREID, maxFeatureId);
			}else if (i == 1){
				i += 1;
				return String.format("%s=%d", Constants.NUMBER_SAMPLES, samples);
			}else if (i == 2){
				i += 1;
				return String.format("%s=%d", Constants.TOTAL_FEATURES, features);
			}
			return null;
		}
	}
	
	public static class FeatureFrequencyStatistic extends DataStatistic{
		HashMap<Integer, Integer> featureBag = new HashMap<Integer, Integer>();
		Iterator<Entry<Integer, Integer>> iter =null;

		public String getOutputSuffix() {
			return ".freq";
		}


		@Override
		public void sampleInfoStat(Vector sample) {
			;
		}


		@Override
		public void featureStat(Vector sample, int index) {
			Integer counts = featureBag.get(sample.features[index]);
			if (counts == null){
				featureBag.put(sample.features[index], sample.count);
			}else{
				featureBag.put(sample.features[index], sample.count + counts);
			}
		}
		
		@Override
		public String getNextStat() throws IOException {
			// TODO Auto-generated method stub
			if (iter == null){
				iter = this.featureBag.entrySet().iterator();
			}
			if (iter.hasNext()){
				Entry<Integer, Integer>  entry= iter.next();
				return 
						String.format("%d\t%d", entry.getKey(), entry.getValue());
			}else
				return null;
		}
	}
}
