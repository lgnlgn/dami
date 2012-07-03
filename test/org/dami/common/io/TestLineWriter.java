package org.dami.common.io;

import java.io.IOException;

import org.dami.common.Vector;

public class TestLineWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		FileVectorReader fvr = new FileVectorReader.LineReader("e:/data/mushrooms.txt", " ", ":", 12);
		
		FileVectorReader fvr = new FileVectorReader.BytesReader("d:/mu", new Vector.Status(0x10 + 0x08 + 0x04));
//		FileVectorWriter fvw = new FileVectorWriter.BytesWriter("d:/mu", 12);
		fvr.open();
//		fvw.open();
		for(Vector v = fvr.next(); v!= null; v=fvr.next()){
			System.out.println(v);
//			fvw.write(v);
		}
		fvr.close();
//		fvw.close();
	}

}
