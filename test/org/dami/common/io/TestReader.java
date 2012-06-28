package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;

public class TestReader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String input = "e:/data/mushrooms.txt";
		int i = 0;
		FileVectorReader fvr = new FileVectorReader.LabelFeatureWeightLineReader(input);
		fvr.open();
		for(Vector v = fvr.next(); v!= null; v=fvr.next()){
			System.out.println(v);
		}
		fvr.close();
		System.out.println(i);
	}	

}
