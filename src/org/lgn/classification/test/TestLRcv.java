package org.lgn.classification.test;


import java.util.Properties;

import org.lgn.classification.lr.SGDLogisticRegression;
import org.lgn.classification.common.DataSet;
import org.lgn.classification.common.Evaluator;
import org.lgn.dami.common.Constants;
public class TestLRcv {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String db = "e:/data/rcv1_train.binary";
		String db = "e:/data/covtype";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		DataSet ds = new DataSet.SerializedStreamDataSet(db);
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
