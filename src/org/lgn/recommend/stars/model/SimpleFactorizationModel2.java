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



@Deprecated
public class SimpleFactorizationModel2 implements Model{
	protected DataSet dataEntry = null;
	private double[] userspace = null;
	private double[] itemspace = null;
	
	private double alpha = 0.004;
	private double lambda = 0.002;
	private double convergence = 0.95;
	private int factor = 50;
	private int loops = 10;
	
	protected int maxIdEnlargement = 100; // new arrays that length = maxXid + enlargement
	
	protected int maxuid ;
	protected int maxiid ;
	protected double avgrating ;
	
	private static final String modelName = "Simple Factorization2";
	
	public void loadData(DataSet data) throws IOException{
		this.dataEntry = data;
		maxuid = (Integer)(this.dataEntry.getDataSetInfo().get(Constants.MAXUSERID));
		maxiid = (Integer)(this.dataEntry.getDataSetInfo().get(Constants.MAXITEMID));
		avgrating = (Double)(this.dataEntry.getDataSetInfo().get(Constants.AVGRATING));
	}
	
	private void init_spaces(){

		userspace = new double[(maxuid + 1) * this.factor];
		itemspace = new double[(maxiid + 1) * this.factor];
		double frag = Math.sqrt((avgrating / factor));
		for(int u = this.factor; u < userspace.length; u++){
			userspace[u] = (float)(frag * Utilities.randomDouble()) ;
		}
		for(int i = this.factor; i < itemspace.length; i++){
			itemspace[i] = (float)(frag * Utilities.randomDouble()) ;
		}
	}
	
	private void _train() throws IOException{
		double learningSpeed = this.alpha;
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			long timeStart = System.currentTimeMillis();
			// core computation
			int n = 0;
			for(UserRatings ur = dataEntry.nextUser(); ur!= null; ur = dataEntry.nextUser()){
				for(RatingInfo ri = ur.getNormalNextRating(); ri != null ; ri = ur.getNormalNextRating()){
					int userStart = ri.userId * this.factor;
					int itemStart = ri.itemId * this.factor;
					
					double eui = ri.rating - Utilities.innerProduct(userspace, itemspace, 
							userStart, itemStart, this.factor);
					
					for(int f = 0 ; f < this.factor; f++){
						userspace[userStart + f] += learningSpeed * (eui * itemspace[itemStart + f] - this.lambda * userspace[userStart + f]);
						itemspace[itemStart + f] += learningSpeed * (eui * userspace[userStart + f] - this.lambda * itemspace[itemStart + f]);
					}
					n+=1;
				}
			}
			
			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.print("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next alpha :" + learningSpeed);
			System.out.println(" total training rating = " + n);
		}
		
	}
	

	
	
	@Override
	public void train() throws IOException {
		// TODO Auto-generated method stub
		System.out.println( modelName + " start loading~~~~~");
		System.out.println("maxuid:" + maxuid);
		System.out.println("maxiid:" + maxiid);
		System.out.println("AVGRATING:" + avgrating);
		init_spaces();

		System.out.println("start training~~~~");
		System.out.println("f:"+ this.factor + " loops:" + this.loops + " alpha:" + this.alpha+ " lambda:"+this.lambda);
		_train();
		

	}

	@Override
	public void setProperties(Properties prop) {
		// TODO Auto-generated method stub
		alpha = Double.parseDouble(prop.getProperty(Constants.ALPHA, "0.005"));
		lambda = Double.parseDouble(prop.getProperty(Constants.LAMBDA, "0.003"));
		convergence = Double.parseDouble(prop.getProperty(Constants.CONVERGENCE, "0.9"));
		loops = Integer.parseInt(prop.getProperty(Constants.LOOPS, "10"));
		factor = Integer.parseInt(prop.getProperty(Constants.FACTOR, "50"));
	}

	@Override
	public double online(int userId, int itemId) {
		// TODO Auto-generated method stub
		return Utilities.innerProduct(userspace, itemspace, userId * factor, itemId * factor, factor);
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
		this.userspace = (double[])ois.readObject();
		this.itemspace = (double[])ois.readObject();
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
