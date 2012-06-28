package org.dami.common;

import java.io.BufferedReader;
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
		// TODO Auto-generated method stub
		String f = "e:/data/covtype";
		long t1 = System.currentTimeMillis();
		Pattern p = Pattern.compile("(\\d+):(\\d+(\\.?\\d.*?)?)");
		BufferedReader br = new BufferedReader(new FileReader(f));
//		Splitter ss = Splitter.on(" ");
		for(String line = br.readLine(); line!= null; line = br.readLine()){
//			String[] aa = line.split("\\s+",2);
			int idx = line.indexOf(" ");
			String ab = line.substring(idx+1);
//			Matcher m = p.matcher(ab);
//			while(m.find()){
//				;
//			}
			String[] bb = ab.split("\\s+|:");
//			for(int i = 0 ; i < bb.length; i++){
//				Double.parseDouble(bb[i]);
//			}
		}
		br.close();
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1);
	}

}
