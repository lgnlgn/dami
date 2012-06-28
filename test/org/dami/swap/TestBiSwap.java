package org.dami.swap;

import java.io.IOException;

import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;
import org.dami.significanttesting.BigraphRandomizeSwapper;

public class TestBiSwap {
	public static void main(String[] args) throws IOException{
		
		FileVectorReader reader = new FileVectorReader.FeatureOnlyLineReader("e:/data/mushroom.dat");
		FileVectorWriter writer = new FileVectorWriter.FeatureOnlyLineWriter("d:/mu");
		BigraphRandomizeSwapper swapper = new BigraphRandomizeSwapper(reader, writer, 500000, 4);
		swapper.runSwap();
	}
}
