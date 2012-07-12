package org.dami.recommendation.common;

/**
 * A triple : (userid, itemid, rating) 
 * @author liangguoning
 *
 */
public class RatingInfo {
		
	public int userId;
	public int itemId;
	public float rating;
	
	/**
	 * 
	 * @param line
	 * @return
	 */

	
	public String toString(){
		return String.format("uid: %d\tiid: %d \trating: %.1f", userId, itemId, rating);
	}
	
	public RatingInfo(){
		
	}
	
	public RatingInfo(int userId){
		this.userId = userId;
	}
}
