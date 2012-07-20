package org.dami.common;

import java.io.IOException;

import org.dami.common.io.TupleAggregator;

public class TestAggregater {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		String input = "e:/data/soc-livej1.txt";
		String output = "d:/yyyy";
		String prefix = "e:/data/soc/part";
		TupleAggregator tg = new TupleAggregator();
//		tg.splitData(input, 2, prefix, 5);
//		tg.splitData2("e:/data/soc-livej1.txt", 2, prefix, 5);
		
//		tg.merge("e:/data/soc", "d:/yyyy", 2);
		tg.aggregate(input, output, 2);
	}

}
