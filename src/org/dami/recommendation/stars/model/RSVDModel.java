package org.dami.recommendation.stars.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dami.common.Utilities;
import org.dami.recommendation.common.RatingInfo;
import org.dami.recommendation.common.UserRatings;

/**
 * RSVD method for recommendation
 * Added user and item biases to SVD
 * predict r^ui = avg + qu * pi + bu + bi
 * use stochastic gradient descent to solve min[sigma( rui - avg - qu * pi - bu -bi)^2 + sigma(qu^2) + sigma(pi^2) + sigma(bu^2) + sigma(bi^2)]
 * @author liangguoning
 *
 */
public class RSVDModel extends SimpleFactorizationModel{
	
	private static final String modelName = "RSVD";
	
	protected double[] userbias = null;
	protected double[] itembias = null;
	
	
	protected void init_spaces(){
		super.init_spaces();
		userbias = new double[maxuid + 1];
		itembias = new double[maxiid + 1];
		for (int u = 0 ; u < userbias.length; u++){
			userbias[u] = Utilities.randomDouble(-0.5, 0.5) ; 
		}
		for (int i = 0 ; i < itembias.length; i++){
			itembias[i] = Utilities.randomDouble(-0.8, 0.8) ;
		}
	}
	
	private void _train() throws IOException{
		System.out.println("--------------------");
		double learningSpeed = this.alpha;
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			long timeStart = System.currentTimeMillis();
			// core computation
			int n = 0;
			for(UserRatings ur = dataEntry.nextUser(); ur!= null; ur = dataEntry.nextUser()){
				for(RatingInfo ri = ur.getNormalNextRating(); ri != null ; ri = ur.getNormalNextRating()){
					double eui = ri.rating - (this.avgrating + userbias[ri.userId] + itembias[ri.itemId] + Utilities.innerProduct(
							userspace[ri.userId], itemspace[ri.itemId]));
					
					userbias[ri.userId] += learningSpeed * (eui - this.lambda * userbias[ri.userId]);
					itembias[ri.itemId] += learningSpeed * (eui - this.lambda * itembias[ri.itemId]);
					
					for(int f = 0 ; f < this.factor; f++){
						userspace[ri.userId][f] = userspace[ri.userId][f] + learningSpeed * (eui * itemspace[ri.itemId][f] - this.lambda * userspace[ri.userId][f]);
						itemspace[ri.itemId][f] = itemspace[ri.itemId][f] + learningSpeed * (eui * userspace[ri.userId][f] - this.lambda * itemspace[ri.itemId][f]);
					}
					n+=1;
				}
			}

			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.print("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next speed :" + learningSpeed);
			System.out.println(" total training rating = " + n);
		}
	}
	
	@Override
	public void train() throws Exception {
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
	public double online(int userId, int itemId) {
		// TODO Auto-generated method stub
		return this.avgrating + userbias[userId] + itembias[itemId] + 
				Utilities.innerProduct(userspace[userId] , itemspace[itemId]);
	}
	
	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.userspace = (double[][])ois.readObject();
		this.itemspace = (double[][])ois.readObject();
		this.userbias = (double[])ois.readObject();
		this.itembias = (double[])ois.readObject();
		ois.close();
		bis.close();
	}

	public void saveModel(String filePath) throws IOException {
		// TODO Auto-generated method stub
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.userspace);
		oos.writeObject(this.itemspace);
		oos.writeObject(this.userbias);
		oos.writeObject(this.itembias);
		oos.close();
		bos.close();
	}
}
