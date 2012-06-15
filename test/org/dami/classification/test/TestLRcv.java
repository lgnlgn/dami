package org.dami.classification.test;


import java.util.Properties;

import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.SGDLogisticRegression;
import org.dami.common.Constants;
import org.dami.common.DataStorage;
public class TestLRcv {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String db = "e:/data/a9a.txt";
//		String db = "e:/data/covtype";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		DataStorage ds = new DataStorage.LabelFeatureWeightRAMStorage(db);
		lr.loadData(ds);
		
		Properties property = new Properties();
//		property.setProperty("-wi1", "3");
//		property.setProperty(Constants.ALPHA, "0.0008");
//		property.setProperty(Constants.LAMBDA, "0.0000001");
		property.setProperty(Constants.LOOPS, "50");
//		property.setProperty(Constants.STOPCRITERIA, "0.00001");
		lr.setProperties(property);
//		System.out.println(lr);
		Evaluator acc = new Evaluator.BinaryAccuracy();
		lr.crossValidation(5, acc);
		System.out.println(acc.resultString());
		System.out.println(lr);

	}

}
