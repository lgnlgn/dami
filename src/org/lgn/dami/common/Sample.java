package org.lgn.dami.common;

/**
 * A vector of sample for classification and clustering
 * @author lgn
 *
 */
public class Sample {
	private final static int DEFAULT_FEATURES = Short.MAX_VALUE;
	public int label = -1;
	public int[] features;
	public double[] weights;
	public int featureSize = 0;
	
	public Sample(){
		init(DEFAULT_FEATURES);
	}
	
	public Sample(int featureSize){
		init(featureSize);
	}
	
	private void init(int featureSize){
		features = new int[featureSize];
		weights = new double[featureSize];
		this.featureSize =  featureSize;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(String.format("%d #(%d)  [", label, featureSize));
		for(int i = 0; i < featureSize; i++){
			sb.append(String.format("%d:%.3f", this.features[i], this.weights[i]) + ", ");
		}
		return sb.substring(0, sb.length()-2) + "]";
	}

}
