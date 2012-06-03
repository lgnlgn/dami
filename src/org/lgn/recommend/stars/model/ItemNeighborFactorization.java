package org.lgn.recommend.stars.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;

import org.lgn.dami.common.Constants;
import org.lgn.dami.common.Utilities;
import org.lgn.recommend.common.DataSet;
import org.lgn.recommend.common.Pair;
import org.lgn.recommend.common.RatingInfo;
import org.lgn.recommend.common.Recommender;
import org.lgn.recommend.common.UserRatings;

/**
 * Factors in the neighbor
 * item neighborhood
 * predict r^ui = avg + bu + bi + qi * sigma( xi + yi)
 * use stochastic gradient descent 
 * @author liangguoning
 *
 */
public class ItemNeighborFactorization extends RSVDModel implements Recommender{
	private static final String modelName = "Item Neighbor Factorization";
	
	//userspace will be null
	
	protected double[][] explictItemspace = null; //xi
	protected double[][] implictItemspace = null; //yi
	protected boolean includeImplicity = false;
	
	
	public double[] offline(int userId) throws Exception {
		// TODO Auto-generated method stub
		UserRatings user = this.dataEntry.getUserById(userId);
		if (user == null){
			return null;
		}else{
			double[] predicts = new double[this.maxiid + 1];
			
			double[] latentUserVector = new double[factor];//pu
			int rates = user.getItemNum();
			double itemNumNormalized = 1 / Math.sqrt(rates);
			
			double basic = avgrating + userbias[user.uid];
			
//			double[] buArray = new double[rates]; //record buj = avg + bu + bj
			for(int j = 0 ; j < rates; j++){
				RatingInfo rij = user.getRatingByIndex(j);
				double buj = basic + this.itembias[rij.itemId];
//				buArray[j] = buj;
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += (rij.rating - buj) * explictItemspace[rij.itemId][f];
				}
			}
			for(int f = 0 ; f < factor; f++){
				latentUserVector[f] *= itemNumNormalized;
			}
			if (this.includeImplicity){
				double[] tmpImplicityVec = new double[factor];
				for(int j = 0 ; j < rates; j++){
					RatingInfo rij = user.getRatingByIndex(j);
					for(int f = 0 ; f < factor; f++){
						tmpImplicityVec[f] +=  implictItemspace[rij.itemId][f];
					}
				}
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += itemNumNormalized * tmpImplicityVec[f];
				}
			}
			//predict for all items 
			// this could be slow 
			for( int i = 1 ; i <= maxiid ; i++ ){
				predicts[i] = basic + this.itembias[i] + 
						Utilities.innerProduct(this.itemspace[i], latentUserVector);
			}
			return predicts;
		}
	}
		
		
		
	public double[] online(List<Pair<Integer, Float>> items) {
		// TODO Auto-generated method stub
		return null;
	}
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.itemspace = (double[][])ois.readObject();
		this.userbias = (double[])ois.readObject();
		this.itembias = (double[])ois.readObject();
		this.explictItemspace = (double[][])ois.readObject();
		if (this.includeImplicity)
			this.implictItemspace = (double[][])ois.readObject();
		System.out.print(this.itemspace.length + ", " + this.itemspace[1].length + "\n");
		System.out.println(this.implictItemspace == null ?  "no implict data" :this.implictItemspace.length);
		ois.close();
		bis.close();
	}
	
	public double online(int userId, int itemId)  {
		UserRatings user = null;
		try{
			user = this.dataEntry.getUserById(userId);
		}catch (Exception e) {
			// TODO: handle exception
			return -1;
		}
		if (user == null){
			System.err.print("!");
			return -1;
		}else{
			double[] latentUserVector = new double[factor];//pu
			int rates = user.getItemNum();
			double itemNumNormalized = 1 / Math.sqrt(rates);
			
			double basic = avgrating + userbias[user.uid];

			//accumulate pu 
			for(int j = 0 ; j < rates; j++){
				RatingInfo rij = user.getRatingByIndex(j);
				double buj = basic + this.itembias[rij.itemId];
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += (rij.rating - buj) * explictItemspace[rij.itemId][f];
				}
			}
			for(int f = 0 ; f < factor; f++){
				latentUserVector[f] *= itemNumNormalized;
			}
			//accumulate pu 
			if (this.includeImplicity){
				double[] tmpImplicityVec = new double[factor];
				for(int j = 0 ; j < rates; j++){
					RatingInfo rij = user.getRatingByIndex(j);
					for(int f = 0 ; f < factor; f++){
						tmpImplicityVec[f] +=  implictItemspace[rij.itemId][f];
					}
				}
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += itemNumNormalized * tmpImplicityVec[f];
				}
			}
			// r^ui = avg + bu + bi + qi * pu
			return  basic + this.itembias[itemId] + 
					Utilities.innerProduct(this.itemspace[itemId], latentUserVector);
		}
	}
	
	public void saveModel(String filePath) throws IOException  {
		// TODO Auto-generated method stub
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		System.out.println(this.itemspace.length);
		System.out.println(this.userbias.length);
		oos.writeObject(this.itemspace);
		oos.writeObject(this.userbias);
		oos.writeObject(this.itembias);
		oos.writeObject(this.explictItemspace);
		if (this.includeImplicity)
			oos.writeObject(this.implictItemspace);
		oos.close();
		bos.close();
	}
	
	public void setProperties(Properties prop) {
		// TODO Auto-generated method stub
		alpha = Double.parseDouble(prop.getProperty(Constants.ALPHA, "0.003"));
		lambda = Double.parseDouble(prop.getProperty(Constants.LAMBDA, "0.04"));
		convergence = Double.parseDouble(prop.getProperty(Constants.CONVERGENCE, "0.90"));
		loops = Integer.parseInt(prop.getProperty(Constants.LOOPS, "10"));
		factor = Integer.parseInt(prop.getProperty(Constants.FACTOR, "50"));
		this.includeImplicity = Boolean.parseBoolean(prop.getProperty("IncludeImplicity", "false"));
	}
	
	final void init_spaces(DataSet data){
		// qi
		itemspace = new double[maxiid + 1][];
		double frag = Math.sqrt((avgrating / factor));
		for(int i = 1; i < itemspace.length; i++){
			itemspace[i] = new double[factor];
			for (int f = 0 ; f < factor; f++){
				itemspace[i][f] = frag * Utilities.randomDouble() / 2;
			}
		}
		//initialize bias: bu, bi
		userbias = new double[maxuid + 1];
		itembias = new double[maxiid + 1];
		for (int u = 0 ; u < userbias.length; u++){
			userbias[u] = Utilities.randomDouble(-0.4, 0.4) ; 
		}
		for (int i = 0 ; i < itembias.length; i++){
			itembias[i] = Utilities.randomDouble(-0.6, 0.6) ;
		}
		// xi yi
		explictItemspace = new double[maxiid + 1][];
		for(int i = 1 ; i < explictItemspace.length; i++){
			explictItemspace[i] = new double[factor];
			for(int f = 0 ; f < factor; f++){
				// 
				explictItemspace[i][f] = frag * Utilities.randomDouble() / 20;
			}
		}
		if (includeImplicity){
			implictItemspace = new double[maxiid + 1][];
			for(int i = 1 ; i < implictItemspace.length; i++){
				implictItemspace[i] = new double[factor];
				for(int f = 0 ; f < factor; f++){
					implictItemspace[i][f] = frag * Utilities.randomDouble() / 50;
				}
			}
		}
	}
	
	@Override
	public void train() throws Exception {
		// TODO Auto-generated method stub
		System.out.println(modelName);
		
		this.init_spaces(dataEntry);
		double learningSpeed = this.alpha;
		
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			long timeStart = System.currentTimeMillis();
			int n = 0;
			//for each user
			for(UserRatings ur = dataEntry.nextUser(); ur!= null; ur = dataEntry.nextUser()){
				double[] latentUserVector = new double[factor];//pu
				int rates = ur.getItemNum();
				double itemNumNormalized = 1 / Math.sqrt(rates);
				// compute latentUserVector for later computation
				// accumulate explicit record for pu
				double[] predictRuiArray = new double[rates]; //record r^uj = ruj - (avg + bu + bj)
				for(int j = 0 ; j < rates; j++){
					RatingInfo rij = ur.getRatingByIndex(j);
					double buj = avgrating + this.userbias[ur.uid] + this.itembias[rij.itemId];
					predictRuiArray[j] = rij.rating - buj;
					for(int f = 0 ; f < factor; f++){
						latentUserVector[f] += predictRuiArray[j] * explictItemspace[rij.itemId][f];
					}
					n += 1;
				}
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] *= itemNumNormalized;
				}
				// accumulate implicit record for pu 
				if (includeImplicity){
					double[] tmpImplicityVec = new double[factor];
					for(int j = 0 ; j < rates; j++){
						RatingInfo rij = ur.getRatingByIndex(j);
						for(int f = 0 ; f < factor; f++){
							tmpImplicityVec[f] +=  implictItemspace[rij.itemId][f];
						}
					}
					for(int f = 0 ; f < factor; f++){
						latentUserVector[f] += itemNumNormalized * tmpImplicityVec[f];
					}
				}
				double[] sumVec = new double[factor];
				
				for(int i = 0 ; i < rates; i++){
					RatingInfo rii = ur.getRatingByIndex(i);
					double eui = rii.rating - (avgrating  + this.userbias[ur.uid] + this.itembias[rii.itemId] 
					        + Utilities.innerProduct(itemspace[rii.itemId], latentUserVector));
//					---Accumulate information for gradient steps on xi, yi:
					for(int f = 0 ; f < factor; f++){
						sumVec[f] += eui * itemspace[rii.itemId][f];
//						Perform gradient step on qi,
						itemspace[rii.itemId][f] += learningSpeed * (eui * latentUserVector[f] - this.lambda * itemspace[rii.itemId][f]);
						//
					}
					//Perform gradient step on bu, bi:
					userbias[rii.userId] += learningSpeed * (eui - this.lambda * userbias[rii.userId]);
					itembias[rii.itemId] += learningSpeed * (eui - this.lambda * itembias[rii.itemId]);
					
				}
				// Perform gradient step on explicit record
				for(int i = 0 ; i < rates; i++){
					RatingInfo rii = ur.getRatingByIndex(i);
					for(int f = 0 ; f < factor; f++){
						explictItemspace[rii.itemId][f] += learningSpeed * (
								itemNumNormalized *	predictRuiArray[i] * sumVec[f] 
								- this.lambda * explictItemspace[rii.itemId][f]); 
					}
				}
//				 Perform gradient step on implicit record
				if (includeImplicity){
					for(int i = 0 ; i < rates; i++){
						RatingInfo rii = ur.getRatingByIndex(i);
						for(int f = 0 ; f < factor; f++){
							implictItemspace[rii.itemId][f] += learningSpeed * (
									itemNumNormalized  * sumVec[f] 
									- this.lambda * implictItemspace[rii.itemId][f]); 
						}
					}
				}
			}
			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.print("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next speed :" + learningSpeed);
			System.out.println(" total training rating = " + n);
		}
			
	}
}
