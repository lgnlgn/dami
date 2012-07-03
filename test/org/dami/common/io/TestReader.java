package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;

public class TestReader {

	public static void testlineReader() throws IOException{
		String input = "e:/data/mushrooms.txt";
		int i = 0;
		FileVectorReader fvr = FileVectorReader.normalClassificationFormatLineReader(input);
		fvr.open();
		for(Vector v = fvr.next(); v!= null; v=fvr.next()){
			System.out.println(v);
		}
		fvr.close();
		System.out.println(i);
	}
	
	public static void test2() throws IOException, ClassNotFoundException{
		String serfile = "e:/data/pr.ser";
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta(serfile);
		System.out.println(reader);
	}
	
	public static void testIdReader() throws IOException{
		String input = "e:/data/pr2.ser";
		int i = 0;
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(input);
		fvr.open();
		for(Vector v = fvr.next(); v!= null; v=fvr.next()){
			System.out.println(v);
		}
		fvr.close();
		System.out.println(i);
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		testIdReader();
	}	

}
