package org.lgn.recommend.stars.test;

import java.util.Properties;

import org.lgn.dami.common.Constants;
import org.lgn.recommend.common.DataSet;
import org.lgn.recommend.common.Evaluator;
import org.lgn.recommend.common.Model;
import org.lgn.recommend.stars.model.RSVDModel;;

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
