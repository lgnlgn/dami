package org.dami.recommendation.common;

import org.dami.common.Utilities;

public class UserRatings {
	public int uid;
	private int[] ratings ; //point to that []
	
	public int indexOfItem = 0;
	public int start;
	public int end;
		
	private RatingInfo current = new RatingInfo();
	
	public UserRatings(int[] ratingObj){
		ratings = ratingObj;
	}
	
	
	public RatingInfo getNormalNextRating(){
		indexOfItem += 1;
		return getRatingByIndex(indexOfItem - 1);
	}
	
	public RatingInfo getRatingByIndex(int idx) throws ArrayIndexOutOfBoundsException{
		if (end - start <= idx){
			return null;
		}
		//do not create a new RatingInfo object  
		//update rating info by modify Object's members, return it's reference 
		current.userId = this.uid;
		current.itemId = (ratings[start + idx]  &  0xfffff); //tail 3 bytes for itemid
		current.rating = Utilities.getDoubleFromSer(ratings[start + idx]);
		return current;
	}
	
	public int getItemNum(){
		return end - start;
	}
	
	public void updateToNew(int uid, int start, int end){
		this.uid = uid;
		this.start = start;
		this.end = end;
		this.indexOfItem = 0;
	}
	
}
