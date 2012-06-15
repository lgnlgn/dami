package org.dami.classification.test;

import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.SGDLogisticRegression;

public class TestPredict {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String db = "e:/data/a9a.txt";
		String test = "e:/data/a9a.t";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		lr.loadModel(db + ".model");
		lr.predict(test, test + ".result", new Evaluator.BinaryAccuracy());
	}

}
