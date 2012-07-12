package org.dami.recommendation.common;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.io.VectorStorage;

import org.dami.recommendation.common.RatingInfo;
import org.dami.recommendation.common.Recommender;
import org.dami.recommendation.common.UserRatings;

public class Evaluation {
	/**
	 * testing RMSE using batch prediction 
	 * @param data
	 * @param recommender
	 * @return
	 * @throws Exception
	 */
	public static double runRMSE(CFDataStorage data, Recommender recommender) throws Exception{
		double error = 0;
		UserRatings ur = new UserRatings();
		int dbsize = Utilities.getIntFromProperties(data.getDataSetInfo(), Constants.TOTAL_FEATURES);
		System.out.println(dbsize);
		int cc = 0;
		data.reOpenDataSet();
		for(data.nextUser(ur); ur.getItemNum() > 0 ;  data.nextUser(ur)){
			
			int[] itemIds = new int[ur.getItemNum()];
			// draw itemid apart
			for(int i = 0 ; i < ur.getItemNum(); i++){
				itemIds[i] = ur.getRatingByIndex(i).itemId;
			}
			// predict in batch mode
			double[] predicts = recommender.predict(ur.getUid(), itemIds);
			if (predicts == null)
				dbsize -= ur.getItemNum();
			else{
				for(int i = 0 ; i < ur.getItemNum(); i++ ){
					RatingInfo ri = ur.getRatingByIndex(i);
					error += Math.pow((ri.rating - predicts[i]), 2);
				}
			}
			cc += 1;
			if (cc % 2000 == 0){
				System.out.print(".");
			}
		}
		return Math.sqrt(error / dbsize);
	}
}
