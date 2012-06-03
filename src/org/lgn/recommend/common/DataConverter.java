package org.lgn.recommend.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;

import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.lgn.dami.common.Utilities;


public class DataConverter {

	private static String SPLIT = "::";

	/**
	 * Data structure contains 2 parts: Rating Array(.ser) & User Offset Array(.idx)
	 * each element in RA is a int(4 bytes) of rating-item; 1+3 bytes
	 * rating byte : 'x.' (head 4 bits) + '.x' (tail 4 bits) 8bits 
	 * each element in UO represent a user's index range(start, end = 4+4 bytes) for rating_info in RA; index = uid ,
	 * for example: UO[1] = 21 represent USER1's rating_info from RA's [0, 21) range
	 * only support UserId ordered
	 * @param input
	 * @throws IOException
	 */
	public static void serilizeRatingFile(String input) throws IOException{

		BufferedReader br = new BufferedReader(new FileReader(input));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(input + ".ser"));

		HashMap<Integer, Long> uidIndex = new HashMap<Integer, Long>();

		int uid;
		int iid;
//		long ts = 0;
		int lines = 0;

		int lastUid = -1;

		int maxuid = 0;
		int maxiid = 0;


		for (String line = br.readLine(); line!= null; line = br.readLine()){
			String[] info = line.split(SPLIT);
			uid = new Integer(info[0]);
			iid = new Integer(info[1]);
			//			if (info.length == 4)
			//				ts = new Long(info[3]);
			long out = (((long)uid << 32) + iid);

			int rated;
			byte s = 0;
			if (info[2].length() == 3){
				s = new Byte(info[2].substring(2,3));;// register '.x'  on tail 4 bits 	
			}
			rated = new Byte(info[2].substring(0,1));
			int head = rated << 4;
			s = (byte)(s | head);  // register '.x'  on tail 4 bits

			if (uid > lastUid){ //meet a new user
				
				Long startEnd = uidIndex.get(lastUid);
				if (startEnd != null){
					startEnd += lines; //end 4 bytes
					uidIndex.put(lastUid, startEnd);
				}
				Long nstartEnd = (long)(lines) << 32; //start 4 bytes
				uidIndex.put(uid, nstartEnd);
				lastUid = uid;
			}else if (uid == lastUid){ 
				;
			}else{
				;
			}


			maxuid = uid > maxuid ? uid : maxuid; //max user id
			maxiid = iid > maxiid ? iid : maxiid; // max item id
			lines += 1;
			bos.write(Utilities.long2outputbytes(out)); //uid iid
			bos.write(s); //rating
//			bos.write(Utilities.long2outputbytes(ts)); //timestamp

		}
		System.out.println(lines);
		bos.close();
		br.close();

		bos = new BufferedOutputStream(new FileOutputStream(input + ".idx"));

		Long lastIndex = uidIndex.get(maxuid); 
		lastIndex += lines; // last user's index-range
		uidIndex.put(maxuid, lastIndex);
		bos.write(Utilities.long2outputbytes((long)maxuid));
		bos.write(Utilities.long2outputbytes((long)maxiid));
		bos.write(Utilities.long2outputbytes((long)lines));
		for(Entry<Integer, Long> entry : uidIndex.entrySet()){
			bos.write(Utilities.int2outputbytes(entry.getKey()));
			bos.write(Utilities.long2outputbytes(entry.getValue()));
		}

		bos.close();
	}


	public static void setSplit(String split){
		DataConverter.SPLIT = split;
	}

	/**
	 * a checking tool , you may need to modify it
	 * @param trainfile
	 * @throws IOException
	 */
	public static void displayIndex(String trainfile) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(trainfile + ".idx"));
		byte[] tmp24 = new byte[24];
		bis.read(tmp24);
		byte[] tmp12 = new byte[12];
		while(true){
			int b = bis.read(tmp12);
			if (b== -1){
				break;
			}
			ByteBuffer buff = ByteBuffer.wrap(tmp12);
			int uid = buff.getInt();
			long offset = buff.getLong(4);
			if (uid > 69800 || uid < 10)
				System.out.println(uid + ":  " + ((offset & 0xfffffff00000000L) >> 32) + " - " + (offset & 0xffffffffl));

		}

	}

	/**
	 * a checking tool ,you may need to modify it
	 * @param trainfile
	 * @throws IOException
	 */
	public static void displayData(String trainfile) throws IOException{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(trainfile + ".ser"));
		byte[] b8 = new byte[8];
		byte[] b1 = new byte[1];
		while(true){
			int b = bis.read(b8);
			if (b == -1){
				break;
			}
			ByteBuffer buff = ByteBuffer.wrap(b8);
			int uid = buff.getInt();
			int iid = buff.getInt(4);
			bis.read(b1);
			byte r = b1[0];
			int tail = (int)(r & 0xf); // x.
			int head = ((int)(r & 0xf0) >>> 4); //.x
			System.out.println(uid + " - " + iid + " : " + (head + tail/ 10.0));
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DataConverter.setSplit(",");

		DataConverter.serilizeRatingFile("e:/data/ml-10m/ml-10M100K/sl.test");
		//		DataConverter.displayIndex("e:/data/ml-10m/ml-10M100K/ratings.dat.test");
		//		DataConverter.displayData("e:/data/ml-10m/ml-10M100K/ratings.dat.test");

	}

}
