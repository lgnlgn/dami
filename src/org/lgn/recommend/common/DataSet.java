package org.lgn.recommend.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lgn.dami.common.Constants;

/**
 * @author liangguoning
 *
 */
public abstract class DataSet {

	public abstract UserRatings nextUser() throws IOException;
	
	public abstract UserRatings getUserById(int uid) throws Exception;
	
	public abstract void reOpenDataSet() throws IOException;

	public abstract Map<String, Object> getDataSetInfo() ;

	public abstract void close()  throws IOException;
	

	
	/**
	 * Load dbfile into memory firstly. 
	 * Data structure contains 2 parts: Rating Array(RA) & User Offset(UO)
	 * each element in RA is a int(4 bytes) of rating-item; 1+3 bytes
	 * each element in UO represent a user's index range for rating_info in RA; index = uid ,
	 * for example: UO[1] = 21 represent USER1's rating_info from RA's [0, 21) range
	 * see DataConverter for more details
	 * @author liangguoning
	 *
	 */
	public static class MemDataSet extends DataSet{
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		
		//data all here 
		private int[] ratingObjs  =null; //each ratinginfo : 3+1 bytes for itemid & rating 
		private long[] userOffsets = null; //each userOffset : 4 + 4 for start(inclusive) & end(exclusive)
		
		private int maxUid;
		private int maxIid;
		private int totals;
		private double sumRating = 0;
		Map<String, Object> dataStatus = null;
		
		int uidx = 0;
		
		UserRatings user = null;
		
		public MemDataSet(String dbprefix) throws IOException {
			byte[] tmp24bytes = new byte[24];
			byte[] tmp12bytes = new byte[12];
			fis = new FileInputStream(dbprefix + ".idx");
			bis = new BufferedInputStream(fis);
			int bytes = 0;
			ByteBuffer buff;
			
			bis.read(tmp24bytes); //maxuid
			buff = ByteBuffer.wrap(tmp24bytes);
			maxUid = (int)(buff.getLong(0));
			maxIid = (int)(buff.getLong(8));
			totals = (int)(buff.getLong(16));
			ratingObjs = new int[totals];
			userOffsets = new long[maxUid + 1];
			while(true){
				bytes = bis.read(tmp12bytes); //uid, rating_offset
				if (bytes == -1){
					break;
				}				
				buff = ByteBuffer.wrap(tmp12bytes);
				int uid = buff.getInt(0);
				long offset = buff.getLong(4);
				
				userOffsets[uid] = offset;
			}
			fis.close();
			bis.close();
			fis = new FileInputStream(dbprefix + ".ser");
			bis = new BufferedInputStream(fis);
			byte[] tmp8bytes = new byte[8];
			byte[] tmp1bytes = new byte[1];
			int r;
			int head;
			int tail;
			for(int idx = 0; ;idx++){
				bytes = bis.read(tmp8bytes);
				if (bytes == -1){
					break;
				}
				bis.read(tmp1bytes);
				buff = ByteBuffer.wrap(tmp8bytes);//for itemid
				r = (int)(tmp1bytes[0]);
				long uidiid = buff.getLong();
//				int uid = (int)((uidiid & 0xffffffff00000000l) >>> 32);//do not store
				int iid = (int)(uidiid & 0xffffffffl);
				if (iid > maxIid){
					System.out.println("aaa");
				}
				ratingObjs[idx] = (r << 24) | iid; //RATING + Itemid : 1+3 bytes 
				tail = (int)(r & 0xf); // .x
				head = ((int)(r & 0xf0) >>> 4); //.x
				double rr = (head + tail / 10.0);
				sumRating += rr;
			}
			fis.close();
			bis.close();

			System.out.println(sumRating/totals);
		}



		public void reOpenDataSet() throws IOException{
			uidx = 0;
		}

		public void close() throws IOException{

		}


		@Override
		public UserRatings nextUser() throws IOException {
			// TODO Auto-generated method stub
			if (user == null){
				user = new UserRatings(this.ratingObjs);
			}
			if (uidx >= userOffsets.length){
				return null;
			}
			long offset = this.userOffsets[uidx];
			for(; offset == 0; ){ // go through missing user			
				uidx += 1;
				offset = this.userOffsets[uidx];
			}
			
			//do not create a new UserRatings object  
			//update user info by modify Object's members, return it's reference 
			user.updateToNew(uidx, 
					(int)((offset & 0xffffffff00000000L) >> 32), 
					(int)((offset & 0xffffffffL)));

			uidx += 1;
			return user;
		}


		@Override
		public Map<String, Object> getDataSetInfo() {
			// TODO Auto-generated method stub
			if (dataStatus == null){
				dataStatus = new HashMap<String, Object>();
				dataStatus.put(Constants.MAXUSERID, maxUid);
				dataStatus.put(Constants.MAXITEMID, maxIid );
				dataStatus.put(Constants.AVGRATING,  sumRating / this.totals);
				dataStatus.put(Constants.TOTALRATINGS, this.totals);
			}
			return dataStatus;
		}



		@Override
		public UserRatings getUserById(int uid) throws Exception {
			// TODO Auto-generated method stub
			if (user == null){
				user = new UserRatings(this.ratingObjs);
			}
			if (uid >= userOffsets.length){
				return null;
			}
			long offset = this.userOffsets[uid];
			if (offset == 0){
				return null;
			}
			
			//do not create a new UserRatings object  
			//update user info by modify Object's members, return it's reference 
			user.updateToNew(uid, 
					(int)((offset & 0xffffffff00000000L) >> 32), 
					(int)((offset & 0xffffffffL)));

			return user;
		}

	}
	
	


}
