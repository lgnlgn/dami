package org.lgn.recommend.stars.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.lgn.dami.common.Constants;
import org.lgn.dami.common.Utilities;
import org.lgn.recommend.common.DataSet;
import org.lgn.recommend.common.Model;
import org.lgn.recommend.common.RatingInfo;
import org.lgn.recommend.common.UserRatings;

/**
 * SVD method for recommendation
 * predict r^ui =  qu * pi
 * use stochastic gradient descent to solve min[sigma( rui - qu * pi )^2 + sigma(qu^2) + sigma(pi^2)]
 * @author liangguoning
 *
 */
public class SimpleFactorizationModel implements Model{

	protected DataSet dataEntry = null;
	protected double[][] userspace = null;
	protected double[][] itemspace = null;
	
	protected double alpha = 0.003;
	protected double lambda = 0.001;
	protected double convergence = 0.97;
	protected int factor = 50;
	protected int loops = 10;
		
	protected int maxuid ;
	protected int maxiid ;
	protected double avgrating ;
	
	
	
	private static final String modelName = "Simple Factorization";
	
	public void loadData(DataSet data){
		this.dataEntry = data;
		maxuid = (Integer)(this.dataEntry.getDataSetInfo().get(Constants.MAXUSERID));
		maxiid = (Integer)(this.dataEntry.getDataSetInfo().get(Constants.MAXITEMID));
		avgrating = (Double)(this.dataEntry.getDataSetInfo().get(Constants.AVGRATING));
		
	}
	
	protected void init_spaces(){

		userspace = new double[maxuid + 1][];
		itemspace = new double[maxiid + 1][];
		double frag = Math.sqrt((avgrating / factor));
		for(int u = 1; u < userspace.length; u++){
			userspace[u] = new double[factor];
			for (int f = 0 ; f < factor; f++){
				userspace[u][f] = frag * Utilities.randomDouble() ;
			}
		}
		System.out.println(itemspace.length);
		for(int i = 1; i < itemspace.length; i++){
			itemspace[i] = new double[factor];
			for (int f = 0 ; f < factor; f++){
				itemspace[i][f] = frag * Utilities.randomDouble() ;
			}
		}
	}
	
	private void _train() throws IOException{
		double learningSpeed = this.alpha;
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			long timeStart = System.currentTimeMillis();
			
			// core computation
			// for each rating
			for(UserRatings ur = dataEntry.nextUser(); ur!= null; ur = dataEntry.nextUser()){
				for(RatingInfo ri = ur.getNormalNextRating(); ri != null ; ri = ur.getNormalNextRating()){
					double eui = ri.rating - Utilities.innerProduct(
							userspace[ri.userId], itemspace[ri.itemId]);
					//perform gradient
					for(int f = 0 ; f < this.factor; f++){
						userspace[ri.userId][f] = userspace[ri.userId][f] + learningSpeed * (eui * itemspace[ri.itemId][f] - this.lambda * userspace[ri.userId][f]);
						itemspace[ri.itemId][f] = itemspace[ri.itemId][f] + learningSpeed * (eui * userspace[ri.userId][f] - this.lambda * itemspace[ri.itemId][f]);
					}	
				}
			}
			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.println("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next alpha :" + learningSpeed);
		}
	}
	

	
	
	@Override
	public void train() throws Exception {
		// TODO Auto-generated method stub
		System.out.println( modelName + " start loading~~~~~");
		System.out.println("maxuid:" + maxuid);
		System.out.println("maxiid:" + maxiid);
		System.out.println("AVGRATING:" + avgrating);
		System.out.println("start training~~~~");
		System.out.println("f:"+ this.factor + " loops:" + this.loops + " alpha:" + this.alpha+ " lambda:"+this.lambda);
		init_spaces();
		_train();
		

	}

	@Override
	public void setProperties(Properties prop) {
		// TODO Auto-generated method stub
		alpha = Double.parseDouble(prop.getProperty(Constants.ALPHA, "0.005"));
		lambda = Double.parseDouble(prop.getProperty(Constants.LAMBDA, "0.003"));
		convergence = Double.parseDouble(prop.getProperty(Constants.CONVERGENCE, "0.90"));
		loops = Integer.parseInt(prop.getProperty(Constants.LOOPS, "10"));
		factor = Integer.parseInt(prop.getProperty(Constants.FACTOR, "50"));
	}

	@Override
	public double online(int userId, int itemId) {
		// TODO Auto-generated method stub
		return Utilities.innerProduct(userspace[userId] , itemspace[itemId]);
	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.userspace = (double[][])ois.readObject();
		this.itemspace = (double[][])ois.readObject();
		ois.close();
		bis.close();
	}

	public void saveModel(String filePath) throws IOException {
		// TODO Auto-generated method stub
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.userspace);
		oos.writeObject(this.itemspace);
		oos.close();
		bos.close();
	}

	@Override
	public void crossValidation(int fold) throws Exception {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public double[] offline(int userId) throws Exception {
//		// TODO Auto-generated method stub
//		double[] scores = new double[maxiid + 1];
//		for (int itemid = 0 ; itemid <= maxiid; itemid++){
//			scores[itemid] = online(userId, itemid);			
//		}
//		return scores;
//	}
	
	

}
