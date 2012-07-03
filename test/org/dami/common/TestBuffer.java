package org.dami.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.*;

public class TestBuffer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ByteBuffer buff;
		buff = ByteBuffer.wrap(Utilities.int2outputbytes(3245));
		System.out.println(buff.getInt());
		byte[] kk = "aaarrr".getBytes();
		byte[] cc = new byte[4+kk.length];
		System.arraycopy(Utilities.int2outputbytes(3245), 0, cc, 0, 4);
		System.arraycopy(kk, 0, cc, 4, kk.length);
		buff = ByteBuffer.wrap(cc, 0, 4);
		System.out.println(buff.getInt());
		System.out.println(new String(cc, 4, cc.length -4));

		byte[][] rr = new byte[][]{Utilities.int2outputbytes(3245), Utilities.int2outputbytes(345)};
		byte[][] r2 = new byte[3][];
		System.arraycopy(rr, 0, r2, 0, 2);
		System.out.println(ByteBuffer.wrap(r2[0]).getInt());
		System.out.println(ByteBuffer.wrap(r2[1]).getInt());
		
//		BufferedReader br = new BufferedReader(new FileReader("e:/data/soc-livej1.txt"), 8124);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream("e:/data/soc-livej1.txt"));
		int i = 0;
		byte[] k = new byte[4096];
		int ii = 0;
		long t = System.currentTimeMillis();
		for(ii = bis.read(k); ii !=-1; ii=bis.read(k)){
			i +=1;
		}
//		for(String line = br.readLine(); line!= null; line = br.readLine()){
//			i+=1;
//		}
		System.out.println(i);
		System.out.println();
		System.out.println(System.currentTimeMillis()- t);
	}
	

}
