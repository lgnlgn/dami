package org.dami.recommendation.test;

import java.io.IOException;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.RandomAccessVectorStorage;
import org.dami.common.io.VectorStorage;
import org.dami.recommendation.common.CFDataStorage;
import org.dami.recommendation.common.Evaluation;
import org.dami.recommendation.common.Recommender;
import org.dami.recommendation.stars.factorization.ItemNeighborModel;
import org.dami.recommendation.stars.factorization.RSVDModel;
import org.dami.recommendation.stars.factorization.SVDModel;
import org.dami.recommendation.stars.memory.SlopeOne;

public class TestSVD {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String train = "E:/data/ml-10M100K/movielens.train";
		String test = "E:/data/ml-10M100K/movielens.test";
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta(train);
		VectorStorage vs = new RandomAccessVectorStorage.RAMSparseStorage(reader);
		
		CFDataStorage data = new CFDataStorage(vs);
		
		/*
		 *  different model may fitted for different parameters!
		 */
//		Recommender model = new SVDModel();
//		Recommender model = new RSVDModel();
//		Recommender model = new ItemNeighborModel();
		Recommender model = new SlopeOne();
		
		Properties p = new Properties();
		p.setProperty(Constants.ALPHA, "0.006");
		p.setProperty(Constants.LAMBDA, "0.015");
		p.setProperty(Constants.LOOPS, "10");
		p.setProperty(Constants.FACTOR, "50");
		p.setProperty(Constants.CONVERGENCE, "0.90");
		p.setProperty("-i", "true");
		model.setProperties(p);
		model.loadData(data);
		
		model.train();
		
		
		
		FileVectorReader reader2 = FileVectorReader.getBytesReaderFromSta(test);
		VectorStorage vs2 = new RandomAccessVectorStorage.RAMSparseStorage(reader2);
		
		double result = Evaluation.runRMSE(new CFDataStorage(vs2), model);
		
		System.out.println("\n----\nRMSE:\t" + result);
	}

}
