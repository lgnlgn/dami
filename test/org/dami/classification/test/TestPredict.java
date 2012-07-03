package org.dami.classification.test;

import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.SGDLogisticRegression;
import org.dami.common.Vector;
import org.dami.common.io.DataReader;
import org.dami.common.io.FileVectorReader;

public class TestPredict {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String db = "e:/data/mushrooms";
		String test = "e:/data/mushrooms.txt";
		SGDLogisticRegression lr = new SGDLogisticRegression();
		lr.loadModel(db + ".model");
		
		DataReader<Vector> reader = FileVectorReader.normalClassificationFormatLineReader(test);
//		DataReader<Vector> reader = new FileVectorReader.LabelFeatureWeightBytesReader(db);
		reader.open();
		Evaluator acc = new Evaluator.BinaryAccuracy();
		lr.predict(reader, test + ".result", acc);
		reader.close();
		
		System.out.println(acc);
	}

}
