package org.dami.recommendation.stars.factorization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.common.Utilities;

import org.dami.recommendation.common.RatingInfo;
import org.dami.recommendation.common.UserRatings;

/**
 * Implementation of Koren's "Factor in the neighbors" 
 * @author lgn
 *
 */
public class ItemNeighborModel extends RSVDModel{
	private static final String modelName = "Item Neighbor Factorization";
	
	//userspace will be null
	
	protected float[][] explicitItemspace = null; //xi
	protected float[][] implicitItemspace = null; //yi
	protected boolean includeImplicity = false;
	
	protected void init_spaces(){
		// qi
		itemspace = new float[maxiid + 1][];
		double frag = Math.sqrt((avgrating * 0.1f / factor));
		for(int i = 1; i < itemspace.length; i++){
			itemspace[i] = new float[factor];
			for (int f = 0 ; f < factor; f++){
				itemspace[i][f] = (float)(frag * (Utilities.randomDouble())) ;
			}
		}
		//initialize bias: bu, bi
		userbias = new float[maxuid + 1];
		itembias = new float[maxiid + 1];
		for (int u = 0 ; u < userbias.length; u++){
			userbias[u] = (float)Utilities.randomDouble(-0.5, 0.5) ; // 
		}
		for (int i = 0 ; i < itembias.length; i++){
			itembias[i] = (float)Utilities.randomDouble(-1.0, 1.0) ; //
		}
		
		int avgRates = totalrating / uids;
		// xi yi
		explicitItemspace = new float[maxiid + 1][];
		if (includeImplicity){ //include implicit feedback
			implicitItemspace = new float[maxiid + 1][];
			for(int i = 1 ; i < explicitItemspace.length; i++){
				explicitItemspace[i] = new float[factor];
				implicitItemspace[i] = new float[factor];
				for(int f = 0 ; f < factor; f++){
					explicitItemspace[i][f] = (float)(frag * Utilities.randomDouble() * 0.2 / Math.sqrt(avgRates));
					implicitItemspace[i][f] = (float)(frag * Utilities.randomDouble() * 0.1 / Math.sqrt(avgRates));
				}
			}
		}else{  //without implicit feedback

			for(int i = 1 ; i < explicitItemspace.length; i++){
				explicitItemspace[i] = new float[factor];
				for(int f = 0 ; f < factor; f++){
					explicitItemspace[i][f] = (float)(frag * Utilities.randomDouble() * 0.3 / Math.sqrt(avgRates));
				}
			}
		}
		ur = new UserRatings(maxRates);
	}

	
	public void train() throws Exception {
		System.out.println(modelName);
		
		this.init_spaces();
		float learningSpeed = this.alpha;
		
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			long timeStart = System.currentTimeMillis();
			float totalError = 0;
			int n = 0; // total ratings trained
			//for each user
			for( dataEntry.nextUser(ur); ur.getItemNum() > 0 ; dataEntry.nextUser(ur)){
				float[] latentUserVector = new float[factor];// special pu
				int rates = ur.getItemNum();
				float itemNumNormalized = (float)(1 / Math.sqrt(rates));
				// compute latentUserVector for later computation
				// accumulate explicit record for pu
				float[] diffArray = new float[rates]; //to record diff: (ruj - buj) = ruj - (avg + bu + bj)
				for(int j = 0 ; j < rates; j++){
					RatingInfo rij = ur.getRatingByIndex(j);
					float buj = avgrating + this.userbias[ur.getUid()] + this.itembias[rij.itemId];
					diffArray[j] = rij.rating - buj;  // (ruj - buj) = ruj - (avg + bu + bj)
					for(int f = 0 ; f < factor; f++){
						latentUserVector[f] += diffArray[j] * explicitItemspace[rij.itemId][f];
					}
					n += 1;
				}
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] *= itemNumNormalized;
				}
				// accumulate implicit record for pu , no diffArray needed
				if (includeImplicity){
					float[] tmpImplicityVec = new float[factor];
					for(int j = 0 ; j < rates; j++){
						RatingInfo rij = ur.getRatingByIndex(j);
						for(int f = 0 ; f < factor; f++){
							tmpImplicityVec[f] +=  implicitItemspace[rij.itemId][f];
						}
					}
					for(int f = 0 ; f < factor; f++){
						latentUserVector[f] += itemNumNormalized * tmpImplicityVec[f];
					}
				}
				float[] sumVec = new float[factor];
				
				for(int i = 0 ; i < rates; i++){
					RatingInfo rii = ur.getRatingByIndex(i);
					
					// try a prediction : rui - r^ui
					float eui = rii.rating - (avgrating  + this.userbias[ur.getUid()] + this.itembias[rii.itemId] 
					        + Utilities.innerProduct(itemspace[rii.itemId], latentUserVector));
					totalError += eui;
					//---Accumulate information for gradient steps on xi, yi:
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
						explicitItemspace[rii.itemId][f] += learningSpeed * (
								itemNumNormalized *	diffArray[i] * sumVec[f] 
								- this.lambda * explicitItemspace[rii.itemId][f]); 
					}
				}
//				 Perform gradient step on implicit record
				if (includeImplicity){
					for(int i = 0 ; i < rates; i++){
						RatingInfo rii = ur.getRatingByIndex(i);
						for(int f = 0 ; f < factor; f++){
							implicitItemspace[rii.itemId][f] += learningSpeed * (
									itemNumNormalized  * sumVec[f] 
									- this.lambda * implicitItemspace[rii.itemId][f]); 
						}
					}
				}
			}
			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.println(String.format("loop:%d\ttime(ms):%d\tavgerror:%.6f\tnext alpha:%.5f", loop, timeSpent, (totalError/n),learningSpeed));

//			System.out.println(" total training rating = " + n);
		}
			
	}
	
	public double predict(int userId, int itemId)  {
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
			float[] latentUserVector = new float[factor];//pu
			int rates = user.getItemNum();
			float itemNumNormalized = (float)(1 / Math.sqrt(rates));
			
			float basic = avgrating + userbias[user.getUid()];

			//accumulate pu 
			for(int j = 0 ; j < rates; j++){
				RatingInfo rij = user.getRatingByIndex(j);
				float buj = basic + this.itembias[rij.itemId];
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += (rij.rating - buj) * explicitItemspace[rij.itemId][f];
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
						tmpImplicityVec[f] +=  implicitItemspace[rij.itemId][f];
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
	
	public double[] predict(int userId) throws Exception {
		// to predict all items
		int[] itemIds = new int[this.maxiid + 1]; 
		for(int i = 0 ; i < itemIds.length; i++){
			itemIds[i] = i;
		}
		return predict(userId, itemIds);
	}
	
	public double[] predict(int userId, int[] itemIds) throws Exception{
		UserRatings user = this.dataEntry.getUserById(userId);
		if (user == null){
			return null;
		}else{
			double[] predicts = new double[itemIds.length];
			
			float[] latentUserVector = new float[factor];//pu
			int rates = user.getItemNum();
			float itemNumNormalized = (float)(1 / Math.sqrt(rates));
			
			float basic = avgrating + userbias[user.getUid()];
			
//			double[] buArray = new double[rates]; //record buj = avg + bu + bj
			for(int j = 0 ; j < rates; j++){
				RatingInfo rij = user.getRatingByIndex(j);
				float buj = basic + this.itembias[rij.itemId];
//				buArray[j] = buj;
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += (rij.rating - buj) * explicitItemspace[rij.itemId][f];
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
						tmpImplicityVec[f] +=  implicitItemspace[rij.itemId][f];
					}
				}
				for(int f = 0 ; f < factor; f++){
					latentUserVector[f] += itemNumNormalized * tmpImplicityVec[f];
				}
			}
			//predict for each in itemIds[] 
			for( int i = 0 ; i < predicts.length ; i++ ){
				predicts[i] = basic + this.itembias[itemIds[i]] + 
						Utilities.innerProduct(this.itemspace[itemIds[i]], latentUserVector);
			}
			return predicts;
		}
	}
	
	public void setProperties(Properties prop){
		super.setProperties(prop);
		this.includeImplicity = Boolean.parseBoolean(prop.getProperty("-i", "false"));
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
		oos.writeObject(this.explicitItemspace);
		oos.writeObject(new Boolean(this.includeImplicity));
		if (this.includeImplicity){
			oos.writeObject(this.implicitItemspace);
		}else{
			;
		}
		oos.close();
		bos.close();
	}
	
	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.itemspace = (float[][])ois.readObject();
		this.userbias = (float[])ois.readObject();
		this.itembias = (float[])ois.readObject();
		this.explicitItemspace = (float[][])ois.readObject();
		this.includeImplicity = (Boolean)ois.readObject();
		if (this.includeImplicity)
			this.implicitItemspace = (float[][])ois.readObject();
		this.maxuid = userbias.length - 1;
		this.maxiid = itembias.length - 1;
		System.out.print(this.itemspace.length + ", " + this.itemspace[1].length + "\n");
		System.out.println(this.implicitItemspace == null ?  "no implict data" :this.implicitItemspace.length);
		ois.close();
		bis.close();
	}
}
