package org.dami.common.io;

import java.io.IOException;

public class TestGraphConverter {

	public static void test() throws IOException{
		String input1 = "e:/data/pr.txt";
		String input2 = "e:/data/pr2.txt";
		String output1 = "e:/data/pr.ser";
		String output2 = "e:/data/pr2.ser";
		FileVectorConverter fvc = FileVectorConverter.graphWithoutWeightConverter(input2, output2, 1);
		fvc.convert();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		test();
	}

}
