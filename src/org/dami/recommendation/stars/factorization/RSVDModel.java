package org.dami.recommendation.stars.factorization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dami.common.Constants;
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
public class RSVDModel extends SVDModel{
	private static final String modelName = "RSVD";
	
	protected float[] userbias = null;
	protected float[] itembias = null;
	
	protected void init_spaces(){
		userspace = new float[maxuid + 1][]; 
		itemspace = new float[maxiid + 1][];
		float frag = (float)Math.sqrt((avgrating * 0.1f/ factor)); // pu * qi is about 10% effect of r^ui
		for(int u = 0; u < userspace.length; u++){
			userspace[u] = new float[factor];
			for (int f = 0 ; f < factor; f++){
				userspace[u][f] = (float)(frag * (Utilities.randomDouble())) ;
			}
		}
		System.out.println(itemspace.length);
		for(int i = 0; i < itemspace.length; i++){
			itemspace[i] = new float[factor];
			for (int f = 0 ; f < factor; f++){
				itemspace[i][f] = (float)(frag * (Utilities.randomDouble())) ;
			}
		}
		userbias = new float[maxuid + 1];
		itembias = new float[maxiid + 1];
		for (int u = 0 ; u < userbias.length; u++){
			userbias[u] = (float)Utilities.randomDouble(-0.5, 0.5) ; // 
		}
		for (int i = 0 ; i < itembias.length; i++){
			itembias[i] = (float)Utilities.randomDouble(-1.5, 1.5) ;
		}
		ur = new UserRatings(maxRates);
	}
	
	public void train() throws Exception {
		System.out.println( modelName + " start loading~~~~~");
		
		System.out.println("maxuid:" + maxuid);
		System.out.println("maxiid:" + maxiid);
		System.out.println("AVGRATING:" + avgrating);
		init_spaces();
		System.out.println("start training~~~~");
		System.out.println("f:"+ this.factor + " loops:" + this.loops + " alpha:" + this.alpha+ " lambda:"+this.lambda);
		_train();

	}
	
	private void _train() throws IOException{
		System.out.println("--------------------");
		float learningSpeed = this.alpha;
		
		for (int loop = 0; loop < this.loops; loop++){
			dataEntry.reOpenDataSet();
			double totalError = 0;
			int n = 0;
			long timeStart = System.currentTimeMillis();
			// core computation
			
			for(dataEntry.nextUser(ur); ur.getItemNum() > 0; dataEntry.nextUser(ur)){
				for(RatingInfo ri = ur.getNormalNextRating(); ri != null ; ri = ur.getNormalNextRating()){
					float eui = ri.rating - (this.avgrating + userbias[ri.userId] + itembias[ri.itemId] + Utilities.innerProduct(
							userspace[ri.userId], itemspace[ri.itemId]));
					//perform gradient on user/item bias
					userbias[ri.userId] += learningSpeed * (eui - this.lambda * userbias[ri.userId]);
					itembias[ri.itemId] += learningSpeed * (eui - this.lambda * itembias[ri.itemId]);
					// perform gradient on pu/qi
					for(int f = 0 ; f < this.factor; f++){
						userspace[ri.userId][f] = userspace[ri.userId][f] + learningSpeed * (eui * itemspace[ri.itemId][f] - this.lambda * userspace[ri.userId][f]);
						itemspace[ri.itemId][f] = itemspace[ri.itemId][f] + learningSpeed * (eui * userspace[ri.userId][f] - this.lambda * itemspace[ri.itemId][f]);
					}
					totalError += eui;
					n+=1;
				}
			}

			long timeSpent = System.currentTimeMillis() - timeStart;
			learningSpeed *= this.convergence;
			System.out.println(String.format("loop:%d\ttime(ms):%d\tavgerror:%.6f\tnext alpha:%.5f", loop, timeSpent, (totalError/n),learningSpeed));
//			System.out.print("loop " + loop + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next speed :" + learningSpeed);
//			System.out.println(" total training ratings = " + n);
		}
		dataEntry.close();
	}
	
	public double predict(int userId, int itemId) {
		return this.avgrating + userbias[userId] + itembias[itemId] + 
				Utilities.innerProduct(userspace[userId] , itemspace[itemId]);
	}
	
	public void loadModel(String modelPath) throws IOException, ClassNotFoundException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modelPath));
		ObjectInputStream ois = new ObjectInputStream(bis);
		this.userspace = (float[][])ois.readObject();
		this.itemspace = (float[][])ois.readObject();
		this.userbias = (float[])ois.readObject();
		this.itembias = (float[])ois.readObject();
		this.avgrating = (Float)ois.readObject();
		ois.close();
		bis.close();
		this.maxuid = userspace.length -1;
		this.maxiid = itemspace.length -1;
	}

	public void saveModel(String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.userspace);
		oos.writeObject(this.itemspace);
		oos.writeObject(this.userbias);
		oos.writeObject(this.itembias);
		oos.writeObject(new Float(this.avgrating));
		oos.close();
		bos.close();
	}
}
