package org.dami.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dami.common.Utilities;

public class TestBytesIO {

	public static void testWrite(int size) throws IOException{
		long t = System.currentTimeMillis();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("d:/ddd"));
		for(int i = 0 ; i < size; i++){
			bos.write(Utilities.int2outputbytes(i));
		}
		bos.close();
		System.out.println(System.currentTimeMillis() -t);
	}
	
	
	public static void testRead(int size) throws IOException{
		long t = System.currentTimeMillis();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream("d:/ddd"));
		byte[] a = new byte[size];
		for(int b = bis.read(a); b != -1; b = bis.read(a)){
			;
		}
		bis.close();
		System.out.println(System.currentTimeMillis() -t);
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		testWrite(1000000000);
//		testRead(65536);
	}

}
