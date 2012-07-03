package org.dami.classification.test;


import java.util.Properties;


import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.SGDLogisticRegression;
import org.dami.common.Constants;
import org.dami.common.Vector;

import org.dami.common.io.DataReader;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.VectorStorage;
public class TestLR2 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String db = "e:/data/rcv1";
		String db = "e:/data/mushrooms";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		
		FileVectorReader fvr = FileVectorReader.normalClassificationFormatBytesReader(db);
		VectorStorage vs = new VectorStorage.RAMStorage(fvr);
		
		lr.loadData(vs);
		Properties p = new Properties();
//		p.setProperty(Constants.ALPHA, "0.05");
//		p.setProperty(Constants.LAMBDA, "0.0001");
		p.setProperty(Constants.LOOPS, "50");
//		p.setProperty(Constants.STOPCRITERIA, "0.001");
//		p.setProperty("-wi1", "3");
		lr.setProperties(p);
		Evaluator acc = new Evaluator.BinaryAccuracy();
//		lr.crossValidation(5, acc);
//		System.out.println(acc.resultString());
		lr.train();
		lr.saveModel(db + ".model");
		System.out.println();
		fvr.open();
		lr.predict(fvr, "d:/mush.txt", acc);
		fvr.close();
		System.out.println(acc.toString());
		
		
		
	}

}
