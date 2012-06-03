package org.lgn.recommend.stars.test;

import java.io.IOException;

import org.lgn.recommend.common.DataSet;
import org.lgn.recommend.common.Evaluator;
import org.lgn.recommend.stars.cf.SlopeOne;

public class TestSlopeOne {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataSet traindata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.train");
		SlopeOne slopeone = new SlopeOne();
		slopeone.loadData(traindata);
		slopeone.train();
		System.out.println("savemodel~");
		slopeone.saveModel("e:/data/ml-10m/ml-10M100K/ratings.dat.train.slopemodel");
		System.out.println("finish~");
		DataSet testdata = new DataSet.MemDataSet("e:/data/ml-10m/ml-10M100K/ratings.dat.test"); 
		
		System.out.println("RMSE:  " + Evaluator.calculateRMSE2(testdata, slopeone));
	}

}
