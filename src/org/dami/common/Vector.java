package org.dami.common;

import java.util.Arrays;

/**
 * A vector of sample for frequent itemset mining / classification / clustering
 * update members instead of initialize new one
 * all members are public to use. 
 * So make sure indexing will not be out of range ~  
 * @author lgn
 *
 */
public class Vector {
	
	public static class Status{
		
		public Status(boolean hasId, boolean hasCount, boolean hasLabel, boolean hasWeight){
			this.hasCount = hasCount;
//			this.hasFeature = hasFeature;
			this.hasId = hasId;
			this.hasWeight = hasWeight;
			this.hasLabel = hasLabel;
		}
		
		public Status(int parameterValue){
			this.setVectorParameter(parameterValue);
		}
		
		public boolean hasId;
		public boolean hasCount;
		public boolean hasLabel;
		public boolean hasWeight;  // if hasFeature is false, hasWeight will not be useful
//		public boolean hasFeature; // if hasFeature is false, hasWeight will not be useful 
		
		public int getVectorParameter(){
			int outParameter = 0;
			if (hasId)
				outParameter |= 0x1;
			if (hasCount)
				outParameter |= 0x2;
			if (hasLabel)
				outParameter |= 0x4;
			if (hasWeight)
				outParameter |= 0x8;
			return outParameter;
		}
		
		public void setVectorParameter(int parameter){
			if (parameter > 31 || parameter < 0)
				throw new IllegalArgumentException("Parameter error VectorParameter MUST IN [0, 15]");
			if ((parameter & 0x1) > 0)
				this.hasId = true;
			if ((parameter & 0x2) > 0)
				this.hasCount = true;
			if ((parameter & 0x4) > 0)
				this.hasLabel = true;
			if ((parameter & 0x8) > 0)
				this.hasWeight = true;
//			if ((parameter & 0x10) > 0)
//				this.hasWeight = true;
		}
		
		public String toString(){
			return String.valueOf(this.getVectorParameter());
		}
	}
	
	private final static int DEFAULT_FEATURES = 65536;
	
	public int label = -1; // original label range : [-32768, 32767]
	public int[] features;
	public float[] weights;
	public int featureSize = 0;
	public int count = 1; // how many vector exactly the same.  
	public int id = -1;   // whether has an id . if a vector has an id , it should have count=1
	
	/**
	 * Fast construction
	 * @param features
	 * @param weights
	 * @param id
	 * @param label
	 * @param count
	 */
	public Vector(int[] features, float[] weights, int id, int label, int count){
		this.features = features;
		this.weights = weights;
		this.featureSize = features.length;
		this.id = id;
		this.count = count;
		this.label = label;
	}
	
	public Vector(){
		init(DEFAULT_FEATURES);
	}
	
	public Vector(int featureSize){
		init(featureSize);
	}
	
	private void init(int featureSize){
		features = new int[featureSize ];
		weights = new float[featureSize ];
		Arrays.fill(weights, 1.0f);
	}
	
	public void enlarge(int size ){
		int[] newf = new int[features.length + size ];
		System.arraycopy(features, 0, newf, 0, features.length);
		this.features = newf;
		float[] neww = new float[features.length + size ];
		Arrays.fill(neww, weights.length -1, neww.length, 1.0f);	
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
		StringBuilder sb = new StringBuilder(String.format("id:%d\tcount:%d label:%d  (%d)[", 
				id, count, label, featureSize));
		for(int i = 0; i < featureSize; i++){
			sb.append(String.format("%d:%.3f", this.features[i], this.weights[i]) + ", ");
		}
		return sb.substring(0, sb.length()-2) + "]";
	}

	/**
	 * Status(0x8 + 0x4)
	 * @return
	 */
	public static Status normalClassificationFormat(){
		return new Status(0x8 + 0x4);
	}
	
	/**
	 * Status(0)
	 * @return
	 */
	public static Status normalFIMFormat(){
		return new Status(0);
	}
	
	/**
	 * Status(1)
	 * @return
	 */
	public static Status idOnlyFormat(){
		return new Status(1);
	}
}
