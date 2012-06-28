package org.dami.common;

/**
 * A vector of sample for frequent itemset mining / classification / clustering
 * update members instead of initialize new one
 * all members are public to use. 
 * So make sure indexing will not be out of range ~  
 * @author lgn
 *
 */
public class Vector {
	private final static int DEFAULT_FEATURES = 65535;
	
	public int label = -1; // orginal label range : [-128, 127]
	public int[] features;
	public float[] weights;
	public int featureSize = 0;
	public int count = 1;
	public int id = -1;
	
	public Vector(){
		init(DEFAULT_FEATURES);
	}
	
	public Vector(int featureSize){
		init(featureSize);
	}
	
	private void init(int featureSize){
		features = new int[featureSize + 1];
		weights = new float[featureSize + 1];
	}
	
	public void enlarge(int size ){
		int[] newf = new int[features.length + size + 1];
		System.arraycopy(features, 0, newf, 0, features.length);
		this.features = newf;
		float[] neww = new float[features.length + size + 1];
		System.arraycopy(weights, 0, neww, 0, weights.length);
		this.weights = neww;
	}
	
	/**
	 * increase size by a half of million 
	 */
	public void enlarge(){
		this.enlarge(65536 * 8); 
	}
	
	public int capacity(){
		return this.features.length;
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
