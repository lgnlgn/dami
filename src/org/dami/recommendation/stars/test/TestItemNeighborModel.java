package org.dami.recommendation.stars.test;


import java.util.Properties;

import org.dami.common.Constants;
import org.dami.recommendation.common.DataSet;
import org.dami.recommendation.common.Evaluator;
import org.dami.recommendation.stars.model.ItemNeighborFactorization;

public class TestItemNeighborModel {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataSet traindata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.train");
		ItemNeighborFactorization model = new ItemNeighborFactorization();
		Properties prop = new Properties();
		prop.setProperty(Constants.LOOPS, "15");
		prop.setProperty(Constants.FACTOR, "100");
		prop.setProperty(Constants.ALPHA, "0.003");
		prop.setProperty(Constants.LAMBDA, "0.05");
		prop.setProperty("IncludeImplicity", "true");
		model.setProperties(prop);
		model.loadData(traindata);
		model.train();
		model.saveModel("e:/data/ml-10m/ml-10M100K/ratings.dat.train.neighbor");
		model.loadModel("e:/data/ml-10m/ml-10M100K/ratings.dat.train.neighbor");
		
		DataSet testdata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.test");
		System.out.println("RMSE : " + Evaluator.calculateRMSE1(testdata, model));
	}

}
