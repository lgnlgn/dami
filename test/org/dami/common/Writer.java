package org.dami.common;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;

public class Writer {

	static byte[] floatTobytes(float input){
		int data=Float.floatToIntBits(input);
		int j = 0;
		byte[] outData=new byte[4];
		outData[j++]=(byte)(data>>>24);
	      outData[j++]=(byte)(data>>>16);
	      outData[j++]=(byte)(data>>>8);
	      outData[j++]=(byte)(data>>>0);
	      return outData;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		byte[] a = Utilities.int2outputbytes(1);
		FileOutputStream fos = new FileOutputStream("d:/aaa.ser");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		FileChannel fc = fos.getChannel();
//		
		long t1 = System.currentTimeMillis();
//		ByteBuffer buff = ByteBuffer.allocate(4);
		for(int i = 0 ; i < 2; i++){
//			fb.put(1+0.0f);
//			fb.
//			fc.write(buff)
			fos.write("aaa   bbb".getBytes());
//			bos.write(Utilities.int2outputbytes(i));
		}
		fos.close();
		long t2 = System.currentTimeMillis();
		System.out.println(t2 -t1);
	}

}

