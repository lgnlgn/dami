package org.dami.common.io;

import java.io.IOException;

import org.dami.common.io.FileVectorConverter;

public class TestConverter {
	public static void main(String[] args) throws IOException{
		String input = "e:/data/mushrooms.txt";
		String out = "d:/mu";
		
		FileVectorConverter fvc = FileVectorConverter.normalclassificationFormatConverter(input, out);
		fvc.convert();
		System.out.println(fvc);
	}
}
