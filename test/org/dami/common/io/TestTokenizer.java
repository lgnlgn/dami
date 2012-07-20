package org.dami.common.io;

import java.io.IOException;

import org.dami.common.collection.ByteArray;
import org.dami.common.collection.NumericTokenizer;

public class TestTokenizer {

	public static void testLine(){
		ByteArray ba = new ByteArray("1 2 3.4\t8\r\n".getBytes());
		NumericTokenizer dt = new NumericTokenizer();
		dt.load(ba);
		while(dt.hasNext()){
			System.out.println(dt.nextNumber());
		}
	}
	
	public static void testFileBytes() throws IOException{
		FileBytesReader fbr = new FileBytesReader("e:/data/pr.txt");
		for(ByteArray ba = fbr.readLine(); ba!= null; ba = fbr.readLine()){
			System.out.print(ba.toString());
		}
		fbr.close();
	}
	
	
	public static void testFormat1(){
		ByteArray ba = new ByteArray(".1 -1:0.5 +2:-1".getBytes());
		NumericTokenizer dt = new NumericTokenizer();
		dt.load(ba);

		System.out.println(dt.nextNumber());
		while(dt.hasNext()){
			long kv = dt.nextKeyValuePair();
			System.out.println(NumericTokenizer.extractFeatureId(kv) + ":" + NumericTokenizer.extractWeight(kv));
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		testLine();
//		testFileBytes();
		testFormat1();
	}

}
