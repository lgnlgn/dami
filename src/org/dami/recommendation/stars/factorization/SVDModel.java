package org.dami.recommendation.stars.factorization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.rmi.CORBA.Util;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.recommendation.common.CFDataStorage;
import org.dami.recommendation.common.RatingInfo;
import org.dami.recommendation.common.Recommender;
import org.dami.recommendation.common.UserRatings;

public class SVDModel implements Recommender{
	final static String modelName = "SVD model";
	CFDataStorage dataEntry;
	protected float[][] userspace = null;
	protected float[][] itemspace = null;
	
	protected float alpha = 0.003f;
	protected float lambda = 0.001f;
	protected float convergence = 0.97f;
	protected int factor = 50;
	protected int loops = 10;
		
	protected int maxuid ;
	protected int maxiid ;
	protected float avgrating ;
	protected int totalrating;
	protected int uids ;
	protected int maxRates ; //max number of items rated by a user (i.e. max number of features within a vector)
	
	UserRatings ur; // a global object
	
	@Override
	public void loadData(CFDataStorage data) throws Exception {
		this.dataEntry = data;
		maxuid = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.MAXVECTORID);
		maxiid = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.MAXFEATUREID);
		avgrating = (float)Utilities.getDoubleFromProperties(dataEntry.getDataSetInfo(),Constants.AVG_WEIGHT);
		totalrating = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.TOTAL_FEATURES);
		uids = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.NUMBER_SAMPLES);
		maxRates = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.MAXFEATURESIZE);
	}

	protected void init_spaces(){

		userspace = new float[maxuid + 1][];
		itemspace = new float[maxiid + 1][];
		double frag = Math.sqrt((avgrating / factor));
		for(int u = 0; u < userspace.length; u++){
			userspace[u] = new float[factor];
			for (int f = 0 ; f < factor; f++){
				userspace[u][f] = (float)(frag * Utilities.randomDouble());
			}
		}
		System.out.println(itemspace.length);
		for(int i = 0; i < itemspace.length; i++){
			itemspace[i] = new float[factor];
			for (int f = 0 ; f < factor; f++){
				itemspace[i][f] = (float)(frag * Utilities.randomDouble()) ;
			}
		}
		ur = new UserRatings(maxRates);
	}
	
	public void train() throws Exception {
		System.out.println( modelName + " start loading~~~~~");
		System.out.println("maxuid:" + maxuid);
		System.out.println("maxiid:" + maxiid);
		System.out.println("AVGRATING:" + avgrating);
		System.out.println("start training~~~~");
		System.out.println("f:"+ this.factor + " loops:" + this.loops + " alpha:" + this.alpha+ " lambda:"+this.lambda);
		init_spaces();
		
		_train();
		
	}
	
	private void _train() throws Exception {
		float learningSpeed = this.alpha;
		UserRatings ur = new UserRatings(maxRates);
		
		
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			float totalError = 0;
			int n = 0;
			long timeStart = System.currentTimeMillis();
			
			// core computation
			// for each rating
			for(dataEntry.nextUser(ur); ur.getItemNum() > 0 ; dataEntry.nextUser(ur)){
				for(RatingInfo ri = ur.getNormalNextRating(); ri != null ; ri = ur.getNormalNextRating()){
					float eui = ri.rating - Utilities.innerProduct(
							userspace[ri.userId], itemspace[ri.itemId]);
					//perform gradient on pu/qi
					for(int f = 0 ; f < this.factor; f++){
						userspace[ri.userId][f] = userspace[ri.userId][f] + learningSpeed * (eui * itemspace[ri.itemId][f] - this.lambda * userspace[ri.userId][f]);
						itemspace[ri.itemId][f] = itemspace[ri.itemId][f] + learningSpeed * (eui * userspace[ri.userId][f] - this.lambda * itemspace[ri.itemId][f]);
					}
					totalError += eui;
					n += 1;
				}
			}
			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.println(String.format("loop:%d\ttime(ms):%d\tavgerror:%.6f\tnext alpha:%.5f", loop, timeSpent, (totalError/n),learningSpeed));
//			System.out.println("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next alpha :" + learningSpeed);
		}
		dataEntry.close();
	}

	@Override
	public void setProperties(Properties prop) {
		alpha = Float.parseFloat(prop.getProperty(Constants.ALPHA, "0.005"));
		lambda = Float.parseFloat(prop.getProperty(Constants.LAMBDA, "0.003"));
		convergence = Float.parseFloat(prop.getProperty(Constants.CONVERGENCE, "0.95"));
		loops = Integer.parseInt(prop.getProperty(Constants.LOOPS, "10"));
		factor = Integer.parseInt(prop.getProperty(Constants.FACTOR, "50"));
	}

	@Override
	public Properties getProperties() {
		//TODO
		return null;
	}

	@Override
	public double predict(int userId, int itemId) throws Exception {
		return Utilities.innerProduct(userspace[userId] , itemspace[itemId]);
	}

	@Override
	public double[] predict(int userId, int[] itemIds) throws Exception {
		double[] result = new double[itemIds.length];
		for(int i = 0 ; i < itemIds.length; i++){
			result[i] = predict(userId, itemIds[i]);
		}
		return result;
	}

	@Override
	public double[] predict(int userId) throws Exception {
		//TODO
		return null;
	}

	@Override
	public double[] predict(UserRatings user) throws Exception {
		return null;
	}
	
	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.userspace = (float[][])ois.readObject();
		this.itemspace = (float[][])ois.readObject();
		ois.close();
		bis.close();
		this.maxiid = itemspace.length -1;
		this.maxuid = userspace.length -1;
	}

	public void saveModel(String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.userspace);
		oos.writeObject(this.itemspace);
		oos.close();
		bos.close();
	}

}
