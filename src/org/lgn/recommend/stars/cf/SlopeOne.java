package org.lgn.recommend.stars.cf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;


import org.lgn.dami.common.Constants;
import org.lgn.recommend.common.DataSet;
import org.lgn.recommend.common.Model;
import org.lgn.recommend.common.Pair;
import org.lgn.recommend.common.RatingInfo;
import org.lgn.recommend.common.Recommender;
import org.lgn.recommend.common.UserRatings;

public class SlopeOne implements Model, Recommender{

	DataSet dataEntry = null;	
	private int maxiid ;

	private static final String modelName = "Slope One";


	float[][] diffs = null;
	int[][] coRating = null;

	public void loadData(DataSet data){
		this.dataEntry = data;

	}

	private void init_space(DataSet data){
		dataEntry = data;
		maxiid = (Integer)(this.dataEntry.getDataSetInfo().get(Constants.MAXITEMID));

		System.out.println(Constants.MAXITEMID + "  :  " + maxiid);
		System.out.println( modelName + " start loading~~~~~");

		diffs = new float[maxiid + 1][];
		for(int i = 0 ; i < diffs.length; i++){
			diffs[i] = new float[maxiid +1];
		}
		coRating = new int[maxiid + 1][];
		for(int i = 0 ; i < coRating.length; i++){
			coRating[i] = new int[maxiid +1];
		}
		System.out.println("initialize finish");
	}


	@Override
	public void train() throws Exception {
		this.init_space(dataEntry);
		for(UserRatings ur = dataEntry.nextUser(); ur!= null; ur = dataEntry.nextUser()){
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
			if (ur.uid % 2000 == 0){
				System.out.print(".");
			}
		}
		System.out.println("finish~");
	}

	@Override
	public void setProperties(Properties prop) {
		// TODO Auto-generated method stub

	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double online(int userId, int itemId) throws Exception {
		// TODO Auto-generated method stub
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
			return predict / coRate;
		}
	}

	@Override
	public double[] offline(int userId) throws Exception{
		// TODO Auto-generated method stub
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
	public double[] online(List<Pair<Integer, Float>> items) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void saveModel(String filePath) throws Exception {
		// TODO Auto-generated method stub
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.diffs);
		oos.writeObject(this.coRating);
		oos.close();
		bos.close();
	}


	@Override
	public void loadModel(String modelPath) throws Exception {
		// TODO Auto-generated method stub
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.diffs = (float[][])ois.readObject();
		this.coRating = (int[][])ois.readObject();
		ois.close();
		bis.close();
	}

	@Override
	public void crossValidation(int fold) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
