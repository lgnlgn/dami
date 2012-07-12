package org.dami.recommendation.common;


import java.util.Properties;

import org.dami.recommendation.common.CFDataStorage;

public interface Recommender {
	
	public void loadData(CFDataStorage data) throws Exception;
	
	public void train() throws Exception;
		
	public void setProperties(Properties prop);
	
	public Properties getProperties();
	
	/**
	 * A single rating prediction
	 * @param userId
	 * @param itemId
	 * @return
	 * @throws Exception
	 */
	public double predict(int userId, int itemId) throws Exception;

	/**
	 * batch prediction of {@link #predict(int, int)}, often for testing
	 * @param userId
	 * @param itemIds
	 * @return
	 * @throws Exception
	 */
	public double[] predict(int userId, int[] itemIds) throws Exception;
	
	/**
	 * Predict a user's all-items rating. 
	 * To predict all user's all-item, use {@link #predict(int, int[])} with an integer array with int[i]=i
	 * Some implementations require user history loaded in the model!
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public double[] predict(int userId) throws Exception;
	
	/**
	 * Predict a user's all-items rating, using his history. 
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public double[] predict(UserRatings user)  throws Exception;
	
	
	
	public void saveModel(String filePath) throws Exception;
	
	public void loadModel(String modelPath) throws Exception;
	
	
}
