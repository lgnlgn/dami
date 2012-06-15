package org.dami.recommendation.common;

/**
 * A triple : (userid, itemid, rating) 
 * @author liangguoning
 *
 */
public class RatingInfo {
		
	public int userId;
	public int itemId;
	public double rating;
	
	/**
	 * 
	 * @param line
	 * @return
	 */

	
	public String toString(){
		return userId + " , " + itemId + " : " + rating;
	}
	
	public RatingInfo(){
		
	}
	
	public RatingInfo(int userId){
		this.userId = userId;
	}
}
