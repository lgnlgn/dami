package org.dami.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.dami.common.collection.IntArray;


public abstract class DataStatistic {
	/**
	 * id count label featureSize ... 
	 * @param sample
	 */
	abstract public void sampleInfoStat(Vector sample);
	
	/**
	 * each feature[:weight] ...
	 * @param sample
	 * @param index
	 */
	abstract public void featureStat(Vector sample, int index);
	
	/**
	 * return key=value format string iterator
	 * @return
	 * @throws IOException
	 */
	abstract public Iterator<String> getStatIter() throws IOException;
	
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


		
		public Iterator<String> getStatIter(){
			return new Iterator<String>() {
				int i = 0; 
				@Override
				public boolean hasNext() {
					return (i < 2) ? true  : false;
				}

				@Override
				public String next() {
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
					return null;
				}

				@Override
				public void remove() {
					;
				}
			};
			
		}
		
		@Override
		public void featureStat(Vector sample, int index) {
			//do nothing;
			
		}

		
	}
	
	public static class CommonStatistic extends DataStatistic{
		int maxFeatureId = -1;
		int samples = 0;
		long features = 0;
		int maxVid = -1;
		int i = 0;
		double weightSum;
		int maxFeatureSize = 0;
		public void featureStat(Vector sample, int index) {
			if (sample.features[index] > maxFeatureId)
				maxFeatureId = sample.features[index];
			features += 1;
			weightSum += sample.weights[index];
		}


		@Override
		public void sampleInfoStat(Vector sample) {
			samples += 1;
			maxVid = Math.max(maxVid, sample.id);
			maxFeatureSize = Math.max(maxFeatureSize, sample.featureSize);
		}

		@Override
		public Iterator<String> getStatIter() throws IOException {
			return new Iterator<String>() {
				int i = 0;
				public boolean hasNext() {
					return (i < 6)? true : false;
				}

				@Override
				public String next() {
					if (i == 0){
						i += 1;
						return String.format("%s=%d", Constants.MAXFEATUREID, maxFeatureId);
					}else if (i == 1){
						i += 1;
						return String.format("%s=%d", Constants.NUMBER_SAMPLES, samples);
					}else if (i == 2){
						i += 1;
						return String.format("%s=%d", Constants.TOTAL_FEATURES, features);
					}else if (i == 3){
						i += 1;
						return String.format("%s=%d", Constants.MAXVECTORID, maxVid);
					}else if (i == 4){
						i += 1;
						return String.format("%s=%.5f", Constants.AVG_WEIGHT, weightSum / features);
					}else if (i == 5){
						i += 1;
						return String.format("%s=%d", Constants.MAXFEATURESIZE, maxFeatureSize);
					}
					return null;
				}

				@Override
				public void remove() {
				}
			};
		}
	}
	
	public static class FeatureFrequencyStatistic extends DataStatistic{
	
		IntArray featureArray = new IntArray(1024);
		int i = 0;

		public String getOutputSuffix() {
			return ".freq";
		}


		@Override
		public void sampleInfoStat(Vector sample) {
			;
		}


		@Override
		public void featureStat(Vector sample, int index) {
			if (featureArray.capacity() < sample.features[index]){
				featureArray.setForcibly(sample.features[index], sample.count);
			}else{
				int counts = featureArray.get(sample.features[index]);
				featureArray.set(sample.features[index], sample.count + counts);
			}
		}
		



		@Override
		public Iterator<String> getStatIter() throws IOException {
			return new Iterator<String>() {

				int i = 0;
				public boolean hasNext() {
					return (i < featureArray.size()) ? true : false;
						 
				}

				public String next() {
					while (featureArray.get(i) <= 0){
						i += 1;// pass 0
					}
					String a = String.format("%d\t%d", i ,featureArray.get(i));
					i += 1;
					return a;
				}

				@Override
				public void remove() {
				}
			};
		}
	}
	
	
	
}
