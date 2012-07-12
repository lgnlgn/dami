package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;

/**
 * support random access for vector by vector's id
 * @author lgn
 *
 */
public abstract class RandomAccessVectorStorage extends VectorStorage{

	protected RandomAccessVectorStorage(FileVectorReader reader)
			throws IOException {
		super(reader);
	}

	public abstract Vector getVectorById(int id) throws IOException ;
	
	
	/**
	 * Require uniqueness and (id > 0) of vector's id!
	 * Calling {@link #getVectorById(Vector sample, int id)} will affect {@link #next(Vector)} method ! 
	 * @author lgn
	 *
	 */
	public static class RAMSparseStorage extends RandomAccessVectorStorage{
		int[] xlabels = null; // label 
		int[][] x = null;  // features
		float[][] xweights = null;  //weights
		int[] xcounts = null;  //count
//		int[] xids = null;     //id
		int currentIdx = 0;  //current index of array
		
		Vector.Status vs = null;
		public RAMSparseStorage(FileVectorReader reader) throws IOException {
			super(reader);
			vs = this.reader.getVectorParameter();
			int samples = Utilities.getIntFromProperties(dataStatus, Constants.MAXVECTORID) + 1;
			Vector current = new Vector();
			
			x = new int[samples][];
			
			if (vs.hasLabel)
				xlabels = new int[samples];
			if (vs.hasWeight)
				xweights = new float[samples][];
			if (vs.hasCount)
				xcounts = new int[samples];
			
			reader.open();
			for(reader.next(current); current.featureSize >= 0; reader.next(current)){
				x[current.id] = new int[current.featureSize];
				if (vs.hasWeight){
					xweights[current.id] = new float[current.featureSize];
					for(int i = 0 ; i < current.featureSize; i++){
						x[current.id][i] = current.features[i];
						xweights[current.id][i] = current.weights[i];
					}
				}
				else{
					System.arraycopy(current.features, 0, x[current.id], 0, current.featureSize);
//					for(int i = 0 ; i < current.featureSize; i++){
//						x[current.id][i] = current.features[i];
//					}
				}
				if (vs.hasLabel)
					xlabels[current.id] = current.label;
				if (vs.hasCount)
					xcounts[current.id] = current.count;

			}
			reader.close();
		}


		@Override
		public Vector getVectorById(int id) throws IOException {
			if (x[id] == null){
				return null;
			}else{
				float[] weights = null;
				int count = 1;
				int label = -1;
				if (vs.hasWeight)
					weights = this.xweights[id];
				if (vs.hasCount)
					count = xcounts[id];
				if (vs.hasLabel)
					label = xlabels[id];
				// initialization in a light weight way
				Vector sample = new Vector(x[id], weights, id, label, count);
				currentIdx = id + 1; 
				return sample;
			}
			
		}

		public void open() throws IOException {
			currentIdx= 0;
		}

		public void close() throws IOException {
			currentIdx = this.x.length;
		}

		/**
		 * update the input Vector
		 */
		public synchronized void next(Vector sample) throws IOException {
			while(currentIdx < this.x.length && x[currentIdx] == null){
				currentIdx += 1;
			}
			if (currentIdx >= this.x.length){
				sample.featureSize = -1;
				return ;
			}
			else{
//				try{
				if (vs.hasLabel)
					sample.label = xlabels[currentIdx];
				if (vs.hasCount)
					sample.count = xcounts[currentIdx];

				sample.featureSize = x[currentIdx].length; //always >= 0 
				sample.id = currentIdx;
				if (vs.hasWeight)
					for(int i = 0 ; i < x[currentIdx].length; i++){
						sample.features[i] = x[currentIdx][i];
						sample.weights[i] =  xweights[currentIdx][i];
					}
				else{
					System.arraycopy(x[currentIdx], 0, sample.features, 0, x[currentIdx].length);
//					for(int i = 0 ; i < x[currentIdx].length; i++){
//						sample.features[i] = x[currentIdx][i];
//					}
				}
//				}catch (NullPointerException e){
//					System.out.println(currentIdx);
//				}
			}
			currentIdx += 1;
		}
		
	}


}
