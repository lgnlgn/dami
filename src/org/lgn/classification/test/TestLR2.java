package org.lgn.classification.test;


import java.util.Properties;

import org.lgn.classification.lr.SGDLogisticRegression;
import org.lgn.classification.common.DataSet;
import org.lgn.classification.common.Evaluator;
import org.lgn.dami.common.Constants;
public class TestLR2 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String db = "e:/data/a9a.txt";
//		String db = "e:/data/track1/train.dat";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		DataSet ds = new DataSet.SerializedStreamDataSet(db);
		lr.loadData(ds);
		Properties p = new Properties();
		p.setProperty(Constants.ALPHA, "0.05");
		p.setProperty(Constants.LAMBDA, "0.0001");
		p.setProperty(Constants.LOOPS, "30");
		p.setProperty(Constants.STOPCRITERIA, "0.001");
		p.setProperty("-wi1", "3");
		lr.setProperties(p);
//		Evaluator acc = new Evaluator.BinaryAccuracy();
//		lr.crossValidation(5, acc);
//		System.out.println(acc.resultString());
		lr.train();
		lr.saveModel(db + ".model");
		
	}

}
