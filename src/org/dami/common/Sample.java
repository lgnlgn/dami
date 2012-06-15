package org.dami.common;

/**
 * A vector of sample for frequent itemset mining / classification / clustering
 * @author lgn
 *
 */
public class Sample {
	private final static int DEFAULT_FEATURES = 65535;
	public int label = -1;
	public int[] features;
	public double[] weights;
	public int featureSize = 0;
	public int count = 1;
	public int id = -1;
	
	public Sample(){
		init(DEFAULT_FEATURES);
	}
	
	public Sample(int featureSize){
		init(featureSize);
	}
	
	private void init(int featureSize){
		features = new int[featureSize + 1];
		weights = new double[featureSize + 1];
		this.featureSize =  featureSize + 1;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(String.format("id:%d\tlabel:%d  #%d  (%d)[", 
				id, label, count, featureSize));
		for(int i = 0; i < featureSize; i++){
			sb.append(String.format("%d:%.3f", this.features[i], this.weights[i]) + ", ");
		}
		return sb.substring(0, sb.length()-2) + "]";
	}

}
