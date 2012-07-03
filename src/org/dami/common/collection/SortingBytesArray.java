package org.dami.common.collection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.dami.common.Utilities;


public class SortingBytesArray{
	private final static int MAX_ENLARGE_SIZE = 1500000;
	private byte[][] storage = new byte[1000][];
	String delim = null;
	int currentIndex = 0;
	StringTokenizer st;
	String aggColumnString;
	
	public static class SortCmp implements Comparator<byte[]>{

		private int flag = 1;
		
		public SortCmp(boolean reverse){
			if (reverse)
				flag = -1;
			else
				flag = 1;
		}
		
		
		@Override
		public int compare(byte[] o1, byte[] o2) {
			int v1 = (((int)o1[0] & 0xff) << 24) | (((int)o1[1] & 0xff) << 16) | (((int)o1[2] & 0xff) << 8) | ((int)o1[3] & 0xff);
			int v2 = (((int)o2[0] & 0xff) << 24) | (((int)o2[1] & 0xff) << 16) | (((int)o2[2] & 0xff) << 8) | ((int)o2[3] & 0xff);
			if (v1 < v2)
				return -1 * flag;
			else if (v1 ==v2)
				return 0;
			else 
				return 1 * flag;
		}
		
	}
	
	public SortingBytesArray(String delim){
		this.delim = delim;
	}
	public SortingBytesArray(){
		this(null);
	}
	
	public void add(String line, int aggColumn){
		if (currentIndex >= storage.length){
			byte[][] newstore = new byte[Math.min(storage.length * 3 / 2 + 1, storage.length + MAX_ENLARGE_SIZE)][];
			System.arraycopy(storage, 0, newstore, 0, storage.length);
			storage = newstore;
		}
		if (delim == null)
			st = new StringTokenizer(line);
		else
			st = new StringTokenizer(line, delim);
		int idx = 0;
		while(st.hasMoreTokens() && idx < aggColumn){
			aggColumnString = st.nextToken();
			idx += 1;
		}
		if (idx == aggColumn){
			byte[] lineBytes = line.getBytes();
			byte[] toStore = new byte[lineBytes.length + 4];
			System.arraycopy(Utilities.int2outputbytes(Integer.parseInt(aggColumnString)), 0, toStore, 0, 4);
			System.arraycopy(lineBytes, 0, toStore, 4, lineBytes.length);
			storage[currentIndex++] = toStore;
		}else{
			throw new IllegalArgumentException("Aggregate column is OUT OF line column");
		}
	}
	
	public void sort(boolean reverse){
		if (reverse)
			Arrays.sort(this.storage, 0, currentIndex, new SortCmp(reverse));
		else
			Arrays.sort(this.storage, 0, currentIndex, new SortCmp(false));
	}
	
	public void sort(){
		this.sort(false);
	}
	
	public int size(){
		return currentIndex;
	}
	
	public void set(int idx, byte[] elem){
		if (idx >= currentIndex)
			throw new IndexOutOfBoundsException();
		this.storage[idx] = elem;
	}
	
	public byte[] get(int idx){
		if (idx >= currentIndex)
			throw new IndexOutOfBoundsException();
		return storage[idx];
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for( int i = 0 ; i < Math.min(currentIndex, 10) ; i ++){
			sb.append(new String(storage[i], 4, storage[i].length -4) + "\n");
		}
		sb.append("...");
		return sb.toString();
	}
	
	public void readIn(File input, int aggColumn) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
		for(String line = br.readLine(); line != null; line = br.readLine()){
			this.add(line, aggColumn);
		}
		br.read();
	}
	
	public void writeOut(String output , boolean append) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(output, append));
		for( int i = 0 ; i < currentIndex ; i ++){
			bw.write(new String(storage[i], 4, storage[i].length -4) + "\n");
		}
		bw.close();
	}

	public void clear(){
//		storage = new byte[1000][];
		currentIndex = 0;
	}
	
	public static void main(String[] args) throws IOException {
		SortingBytesArray sba = new SortingBytesArray();
		long t = System.currentTimeMillis();
		sba.readIn(new File("e:/data/soc/part.0"), 2);
//		sba.readIn("e:/data/g.txt", 2);
		System.out.println(System.currentTimeMillis() - t);
		sba.sort();
		System.out.println(sba);
	}

}
