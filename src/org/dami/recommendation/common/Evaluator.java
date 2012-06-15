package org.dami.recommendation.common;

import org.dami.common.Constants;

/**
 * implement some evaluate metrics 
 * for example : RMSE
 * @author liangguoning
 *
 */
public class Evaluator {

	
	public static double calculateRMSE1(DataSet data, Model model) throws Exception{
		double error = 0;
		int dbsize = (Integer)(data.getDataSetInfo().get(Constants.TOTALRATINGS));
		for(UserRatings urs = data.nextUser(); urs != null;  urs = data.nextUser()){
			for(RatingInfo ri = urs.getNormalNextRating(); ri!= null; ri = urs.getNormalNextRating() ){
				double predict =  model.online(ri.userId, ri.itemId);
				if (predict == -1){ //error in prediction 
					dbsize -= 1;
				}else{
					error += Math.pow((ri.rating - predict), 2);
				}
			}

		}
		return Math.sqrt(error / dbsize);
	}
	
	public static double calculateRMSE2(DataSet data, Recommender recommender) throws Exception{
		double error = 0;
		int dbsize = (Integer)(data.getDataSetInfo().get(Constants.TOTALRATINGS));
		System.out.println(dbsize);
		for(UserRatings urs = data.nextUser(); urs != null;  urs = data.nextUser()){
			double[] predicts = recommender.offline(urs.uid);
			if (predicts == null){
				System.err.println(String.format("UID %d does not in train dataset", urs.uid));
				dbsize -= urs.getItemNum(); 
			}else{
				for(RatingInfo ri = urs.getNormalNextRating(); ri!= null; ri = urs.getNormalNextRating() ){
					error += Math.pow((ri.rating - predicts[ri.itemId]), 2);
				}
			}
			if (urs.uid % 2000 == 0){
				System.out.print(".");
			}
		}
		return Math.sqrt(error / dbsize);
	}
	
}
