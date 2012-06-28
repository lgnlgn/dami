package org.dami.classification.common;

import org.dami.common.Constants;

/**
 * implement your evaluator
 * @author lgn
 *
 */
public abstract class Evaluator {
	public abstract String resultString() ;
	
	public abstract void collect(int label, double[] probs);
	
	public static class BinaryMAE extends Evaluator{

		@Override
		public String resultString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void collect(int label, double[] probs) {
			// TODO Auto-generated method stub
			
		}
		
	}

	/**
	 * Accuracy evaluator for 2 classes classification
	 * @author lgn
	 *
	 */
	public static class BinaryAccuracy extends Evaluator{

		private int totals = 0;
		private int corrects = 0;
		
		@Override
		public String resultString() {
			// TODO Auto-generated method stub
			StringBuilder sb = new StringBuilder("-------------------------" + Constants.ENDL);
			sb.append(String.format(" Accuracy : %d/%d =%.2f" + Constants.ENDL, corrects, totals, (corrects * 100.0 / totals)));
			sb.append("-------------------------" + Constants.ENDL);
			return sb.toString();
		}

		/**
		 * use converted label!
		 */
		public void collect(int label, double[] probs) {
			// TODO Auto-generated method stub
			totals += 1;
			if ((probs[0] > 0.5 && label == 0) ||(probs[0] < 0.5 && label == 1))
				corrects += 1;
		}
		
		public String toString(){
			return this.resultString();
		}
		
	}
	
	public static class BinaryPrecisionRecall extends Evaluator{

		@Override
		public String resultString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void collect(int label, double[] probs) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class BinaryCommonEvaluator extends Evaluator{

		@Override
		public String resultString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void collect(int label, double[] probs) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
