package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;

public class TestTupleReader {
	public static void main(String[] args) throws IOException{
		FileVectorReader reader = new FileVectorReader.TupleReader("e:/data/pr2.txt", 1);
		reader.open();
		for(Vector v = reader.next(); v!= null; v=reader.next()){
			System.out.println(v);
		}
		reader.close();
	}
}
