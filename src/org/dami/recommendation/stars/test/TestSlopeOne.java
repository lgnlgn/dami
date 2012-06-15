package org.dami.recommendation.stars.test;

import java.io.IOException;

import org.dami.recommendation.common.DataSet;
import org.dami.recommendation.common.Evaluator;
import org.dami.recommendation.stars.memory.SlopeOne;

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
