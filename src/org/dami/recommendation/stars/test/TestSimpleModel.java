package org.dami.recommendation.stars.test;

import java.io.IOException;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.recommendation.common.DataSet;
import org.dami.recommendation.common.Evaluator;
import org.dami.recommendation.common.Model;
import org.dami.recommendation.stars.model.SimpleFactorizationModel;

public class TestSimpleModel {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataSet traindata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.train");
		Model model = new SimpleFactorizationModel();
		Properties prop = new Properties();
		prop.setProperty(Constants.LOOPS, "15");
		prop.setProperty(Constants.FACTOR, "100");
		model.setProperties(prop);
		model.loadData(traindata);
		model.train();
		DataSet testdata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.test");
		System.out.println("RMSE:  " + Evaluator.calculateRMSE1(testdata, model));
	}

}
