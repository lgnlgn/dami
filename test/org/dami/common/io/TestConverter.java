package org.dami.common.io;

import java.io.IOException;

import org.dami.common.io.FileVectorConverter;

public class TestConverter {
	public static void main(String[] args) throws IOException{
		String input = "e:/data/a9a.txt";
		String out = "d:/a9a";
		
		FileVectorConverter fvc = FileVectorConverter.normalclassificationFormatConverter(input, out);
		fvc.convert();
		System.out.println(fvc);
	}
}
