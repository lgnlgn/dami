package org.dami.recommendation.stars.memory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.recommendation.common.CFDataStorage;
import org.dami.recommendation.common.RatingInfo;
import org.dami.recommendation.common.Recommender;
import org.dami.recommendation.common.UserRatings;

public class SlopeOne implements Recommender{
	CFDataStorage dataEntry;
	protected int maxiid ;
	protected float avgrating; 
	private static final String modelName = "Slope One";


	float[][] diffs = null;
	int[][] coRating = null;


	private void init_space(){
		System.out.println(Constants.MAXFEATUREID + "  :  " + maxiid);
		System.out.println( modelName + " start loading~~~~~");

		diffs = new float[maxiid + 1][];
		coRating = new int[maxiid + 1][];
		for(int i = 0 ; i <= maxiid; i++){
			diffs[i] = new float[maxiid +1];
			coRating[i] = new int[maxiid +1];
		}
		System.out.println("initialize finish");
	}
	
	@Override
	public void loadData(CFDataStorage data) throws Exception {
		dataEntry = data;
		maxiid = Utilities.getIntFromProperties(this.dataEntry.getDataSetInfo(), Constants.MAXFEATUREID);
		avgrating = (float)Utilities.getDoubleFromProperties(dataEntry.getDataSetInfo(),Constants.AVG_WEIGHT);

	}

	@Override
	public void train() throws Exception {
		this.init_space();
		UserRatings ur = new UserRatings();
		for(dataEntry.nextUser(ur); ur.getItemNum() > 0; dataEntry.nextUser(ur)){
			int rates = ur.getItemNum();
			for(int i = 0 ; i < rates-1; i++){
				RatingInfo rii = ur.getRatingByIndex(i);
				int firstItem = rii.itemId;
				double firstRating = rii.rating;
				for(int j = i+1; j < rates; j++){
					RatingInfo rij = ur.getRatingByIndex(j); //actually rii changed
					int secondItem = rij.itemId;
					double secondRating = rij.rating;

					diffs[firstItem][secondItem] += (float)(firstRating - secondRating);
					diffs[secondItem][firstItem] -= (float)(firstRating - secondRating);

					coRating[secondItem][firstItem] += 1;
					coRating[firstItem][secondItem] += 1;
				}
			}
			if (ur.getUid() % 2000 == 0){
				System.out.print(".");
			}
		}
		System.out.println("finish~");
		
	}

	@Override
	public void setProperties(Properties prop) {

	}

	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public double predict(int userId, int itemId) throws Exception {
		UserRatings user = this.dataEntry.getUserById(userId);
		if (user == null){
			return -1;
		}else{
			double predict = 0;
			int coRate = 0;
			for(RatingInfo ri = user.getNormalNextRating(); ri != null; ri = user.getNormalNextRating()){
				predict += ri.rating * this.coRating[ri.itemId][itemId] - this.diffs[ri.itemId][itemId];
				coRate += this.coRating[ri.itemId][itemId];
			}
			return (float)(predict / coRate);
		}
	}

//	public double[] predict(int userId, int[] itemIds) throws Exception{
//		UserRatings user = this.dataEntry.getUserById(userId);
//		if (user == null){
//			return null;
//		}else{
//			double[] predicts = new double[itemIds.length];
//			int[] coRatingsArray = new int[itemIds.length];
//			//for each rated item
//			for(RatingInfo ri = user.getNormalNextRating(); ri != null; ri = user.getNormalNextRating()){
//				float[] diffOfItem = diffs[ri.itemId];
//				int[] coRates = this.coRating[ri.itemId];
//				//get co-relation vector 
//				for(int i = 0 ; i < itemIds.length; i++){
//					if (coRates[i] != 0){
//						predicts[i] += ri.rating * coRates[i] - diffOfItem[i];
//						coRatingsArray[i] += coRates[i];
//					}
//				}
//			}
//			for(int i=0; i < itemIds.length; i++){
//				if (coRatingsArray[i] != 0){
//					predicts[i] /= coRatingsArray[i];
//				}
//			}
//			return predicts;	
//		}
//	}
	
	
	/**
	 * 
	 */
	public double[] predict(int userId) throws Exception {
		UserRatings user = this.dataEntry.getUserById(userId);
		if (user == null){
			return null;
		}else{
			double[] predicts = new double[this.maxiid + 1];
			int[] coRatingsArray = new int[this.maxiid + 1];
			//for each rated item
			for(RatingInfo ri = user.getNormalNextRating(); ri != null; ri = user.getNormalNextRating()){
				float[] diffOfItem = diffs[ri.itemId];
				int[] coRates = this.coRating[ri.itemId];
				//get co-relation vector 
				for(int i = 0 ; i <= this.maxiid; i++){
					if (coRates[i] != 0){
						predicts[i] += ri.rating * coRates[i] - diffOfItem[i];
						coRatingsArray[i] += coRates[i];
					}
				}
			}
			for(int i=0; i <= this.maxiid; i++){
				if (coRatingsArray[i] != 0){
					predicts[i] /= coRatingsArray[i];
				}
			}
			return predicts;
		}
	}

	@Override
	public void saveModel(String filePath) throws Exception {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.diffs);
		oos.writeObject(this.coRating);
		oos.close();
		bos.close();
	}


	@Override
	public void loadModel(String modelPath) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.diffs = (float[][])ois.readObject();
		this.coRating = (int[][])ois.readObject();
		ois.close();
		bis.close();
		this.maxiid = this.diffs.length - 1;
	}

	@Override
	public double[] predict(UserRatings user) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] predict(int userId, int[] itemIds) throws Exception {
		UserRatings user = this.dataEntry.getUserById(userId);
		if (user == null){
			return null;
		}else{
			double[] predicts = new double[itemIds.length];
			int[] coRatingsArray = new int[itemIds.length];
			
			for(RatingInfo ri = user.getNormalNextRating(); ri != null; ri = user.getNormalNextRating()){
				
				for( int i = 0 ; i < itemIds.length; i++){
					predicts[i] += ri.rating * this.coRating[ri.itemId][itemIds[i]] - this.diffs[ri.itemId][itemIds[i]];
					coRatingsArray[i] += this.coRating[ri.itemId][itemIds[i]];
				}
			}
			for( int i = 0 ; i < itemIds.length; i++){
				if (coRatingsArray[i] != 0){
					predicts[i] /= coRatingsArray[i];
				}else{
					predicts[i] = avgrating;
				}
			}
			return predicts;
		}
	}

}
