package org.lgn.classification.common;


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

	public static class BinaryAccuracy extends Evaluator{

		private int totals = 0;
		private int corrects = 0;
		
		@Override
		public String resultString() {
			// TODO Auto-generated method stub
			StringBuilder sb = new StringBuilder("-------------------------\n");
			sb.append(String.format(" Accuracy : %d/%d =%.2f\n", corrects, totals, (corrects * 100.0 / totals)));
			sb.append("-------------------------\n");
			return sb.toString();
		}

		@Override
		public void collect(int label, double[] probs) {
			// TODO Auto-generated method stub
			totals += 1;
			if ((probs[0] > 0.5 && label == 0) ||(probs[0] < 0.5 && label == 1))
				corrects += 1;
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
