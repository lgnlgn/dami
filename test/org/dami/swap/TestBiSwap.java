package org.dami.swap;

import java.io.IOException;

import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;
import org.dami.significanttesting.BigraphRandomizeSwapper;

public class TestBiSwap {
	public static void main(String[] args) throws IOException{
		
		FileVectorReader reader = FileVectorReader.normalFIMFormatLineReader("e:/data/mushroom.dat");
		FileVectorWriter writer = FileVectorWriter.normalFIMFormatLineWriter("d:/mu");
		BigraphRandomizeSwapper swapper = new BigraphRandomizeSwapper(reader, writer, 500000, 4);
		swapper.runSwap();
	}
}
