package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;

public class TestTupleReader {
	public static void main(String[] args) throws IOException{
		FileVectorReader reader = new FileVectorReader.TupleReader("e:/data/pr2.txt", 1);
		reader.open();
		Vector v = new Vector();
		for(reader.next(v); v.featureSize > 0; reader.next(v)){
			System.out.println(v);
		}
		reader.close();
	}
}
