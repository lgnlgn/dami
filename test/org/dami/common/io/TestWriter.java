package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;

public class TestWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FileVectorReader reader = new FileVectorReader.FeatureOnlyLineReader("e:/data/mushroom.dat");
		FileVectorWriter writer = new FileVectorWriter.FeatureOnlyLineWriter("d:/mu");
		
		reader.open();
		writer.open();
		for(Vector sample = reader.next(); sample != null; sample = reader.next()){
			writer.write(sample);
		}
		reader.close();
		writer.close();
	}

}
