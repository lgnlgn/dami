package org.dami.common.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.dami.common.collection.ByteArray;

/**
 * read byte[] from file
 * be cautious that currently the buffer is immutable, so it's not safe for long line text file!
 * TODO safe reading for long line text  
 * @author lgn
 *
 */
public class FileBytesReader {
	final static int buffSize = 1024 * 1024 * 8; //8M buffer
	FileInputStream fis ;
	BufferedInputStream bis;
	
	byte[]  buff;
	byte[]  backup;
	byte[]  swap;
	
	ByteArray tmp;
	
	int startIdx = 0;
	int endIdx = 0;
	int lastReads = -2;
	
	
	public FileBytesReader(String path) throws IOException{
		this.fis = new FileInputStream(path);
		this.bis = new BufferedInputStream(fis);
		this.buff = new byte[buffSize];
		this.backup = new byte[buffSize];
		tmp = new ByteArray(buff);
	}
	
	public void close()throws IOException{
		bis.close();
		fis.close();
	}
	
	public ByteArray readLine() throws IOException{
		if (lastReads == -2){
			lastReads  = bis.read(buff);	
		}
		if (lastReads == -1){
			System.err.println("!!!!!!");
			return null;
		}
		for(endIdx = startIdx; endIdx < lastReads && buff[endIdx]!= 10 ; endIdx++){
			;
		}
		if (endIdx >= buff.length){// some chars are on the next loop
			System.arraycopy(buff, startIdx, backup, 0, endIdx - startIdx);
			swap = buff;
			buff = backup;
			backup = swap;
			startIdx = endIdx - startIdx;
			lastReads = bis.read(buff, startIdx, buff.length - startIdx);
			lastReads += startIdx;
			startIdx = 0;
			tmp.array = buff; 
			if (lastReads == -1){
				System.err.println("33");
				return null;
			}
			else
				return readLine();
		}else if (startIdx >= endIdx){ //
//			System.err.println("11111");
			return null;
		}else{
			tmp.setSegment(startIdx, endIdx + 1);
			startIdx = endIdx + 1;
			return tmp;
		}

	}
}
