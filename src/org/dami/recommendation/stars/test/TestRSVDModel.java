package org.dami.recommendation.stars.test;

import java.util.Properties;

import org.dami.common.Constants;
import org.dami.recommendation.common.DataSet;
import org.dami.recommendation.common.Evaluator;
import org.dami.recommendation.common.Model;
import org.dami.recommendation.stars.model.RSVDModel;

public class TestRSVDModel {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataSet traindata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.train");
		Model model = new RSVDModel();
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
