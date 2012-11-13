package org.dami.classification.test;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.AbstractSDGLogisticRegression;
import org.dami.classification.lr.SGDL1LR;
import org.dami.classification.lr.SGDL2LR;
import org.dami.common.Constants;
import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.TestReader;
import org.dami.common.io.VectorStorage;

/**
 *  Change storage setting in the test method
 * @author Administrator
 *
 */
public class TestSGDLR {

	private static Class<? extends VectorStorage> storageClz ;
	
	private static void testCrossValidation(Class<? extends AbstractSDGLogisticRegression> sgd, String db, Properties property) throws Exception{
		AbstractSDGLogisticRegression lr = sgd.newInstance();
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(db);
		Constructor<VectorStorage> constructor = (Constructor<VectorStorage>)storageClz.getConstructor(FileVectorReader.class);
		VectorStorage vs = constructor.newInstance(fvr);
		
		lr.loadData(vs);
		lr.setProperties(property);
		System.out.println(lr);
//		System.out.println(lr);
		Evaluator acc = new Evaluator.BinaryAccuracy();
		lr.crossValidation(5, acc);
		System.out.println(acc.resultString());
		System.out.println(lr);
	}
	
	
	private static void testTrain(Class<? extends AbstractSDGLogisticRegression> sgd, String db, Properties property) throws Exception{

		AbstractSDGLogisticRegression lr = sgd.newInstance();
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(db);
		Constructor<VectorStorage> constructor = (Constructor<VectorStorage>)storageClz.getConstructor(FileVectorReader.class);
		VectorStorage vs = constructor.newInstance(fvr);
		
		lr.loadData(vs);
		System.out.println(lr);
		lr.setProperties(property);
		Evaluator acc = new Evaluator.BinaryAccuracy();
		lr.train();
		lr.saveModel(db + ".model");
		System.out.println(lr);
	}
	
	private static void testTrainAndTest(Class<? extends AbstractSDGLogisticRegression> sgd, String train, String test, Properties property) throws Exception{
		testTrain(sgd, train, property);
		
		AbstractSDGLogisticRegression lr = sgd.newInstance();
		Evaluator acc = new Evaluator.BinaryAccuracy();
		
		lr.loadModel(train + ".model");
		FileVectorReader  fvr = FileVectorReader.getBytesReaderFromSta(test);
		fvr.open();
		lr.predict(fvr, test + ".result", acc);
		fvr.close();
		System.out.println(acc.toString());
		
	}
	
	
	
	public static void testL1Train(String db, Properties p) throws Exception{
		testTrain(SGDL1LR.class, db, p);
	}
	
	public static void testL2Train(String db, Properties p) throws Exception{
		testTrain(SGDL2LR.class, db, p);
	}
	
	public static void testL1CV(String db, Properties p) throws Exception{
		testCrossValidation(SGDL1LR.class, db, p);
	}
	
	public static void testL2CV(String db, Properties p) throws Exception{
		testCrossValidation(SGDL2LR.class, db, p);
	}
	
	public static void testL2TestAndTrain(String train, String test, Properties p) throws Exception{
		testTrainAndTest(SGDL2LR.class, train, test, p);
	}
	
	public static void testL1TestAndTrain(String train, String test, Properties p) throws Exception{
		testTrainAndTest(SGDL1LR.class, train, test, p);
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// specify storage
		storageClz = VectorStorage.RAMCompactStorage.class;
		
		String db = "d:/data/real-sim";
		String train = db;
		String test = db;
		Properties p = new Properties();
//		p.setProperty(Constants.LAMBDA, "0.3");
//		p.setProperty(Constants.ALPHA, "0.3");
//		testL2CV(db, p);
		testL1Train(db, p);
//		testL2TestAndTrain(train, test, p);
	}

}
