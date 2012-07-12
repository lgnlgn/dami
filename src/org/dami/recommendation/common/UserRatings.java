package org.dami.recommendation.common;

import org.dami.common.Vector;
import org.dami.recommendation.common.RatingInfo;

/**
 * Update to a new one by modifying vector and calling {@link #update()} explicitly
 * Since rating for recommendation is in small range, (0, 5],  
 * We use 4 bytes (a int type) to store id(3 bytes, range can be up to 16 million) and rating(1 byte) 
 * @author lgn
 *
 */
public class UserRatings{
	Vector vector; 
	RatingInfo current;
	int indexOfItem = 0;
	
	public UserRatings(Vector sample){
		this.vector = sample;
		current = new RatingInfo();
		current.userId = sample.id;
	}
	
	
	public UserRatings(int size ){
		this(new Vector(size));
	}
	
	public UserRatings(){
		this(new Vector());
	}
	
	public int getUid(){
		return vector.id;
	}
	
	public int getItemNum(){
		return vector.featureSize;
	}
	
	public RatingInfo getNormalNextRating(){
		RatingInfo tmp = getRatingByIndex(indexOfItem);
		indexOfItem += 1;
		return tmp;
	}
	
	/**
	 * not thread safe!
	 * @param idx
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public RatingInfo getRatingByIndex(int idx) throws ArrayIndexOutOfBoundsException{
		if (vector.featureSize <= idx){
			return null;
		}
		//do not create a new RatingInfo object  
		//update rating info by modify Object's members, return it's reference 
//		current.itemId = (this.vector.features[idx] & 0xffffff); //tail 3 bytes for itemid, 
//		if ((vector.features[idx] & 0xf0000000) > 0){  //check whether  
//			current.rating = ((this.vector.features[idx] & 0xf0000000) >>> 28) + 
//								((this.vector.features[idx] & 0x0f000000) >>> 24) * 0.1f;
//		}else{
//			current.rating = this.vector.weights[idx];
//		}
		current.userId = vector.id;
		current.itemId = vector.features[idx];
		current.rating = vector.weights[idx];
		indexOfItem = idx + 1;
		return current;
	}
	
	public void refresh(){
		indexOfItem = 0;
	}
}
