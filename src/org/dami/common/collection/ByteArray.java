package org.dami.common.collection;

/**
 * 
 * @author lgn
 *
 */
public class ByteArray {
	final static int MAX_ENLARGE_SIZE = 1024*1024*8;
	public byte[] array;
	public int startIdx;
	public int endIdx;
	private int currentIdx;
	
	public ByteArray(byte[] array, int start, int end){
		this.array = array;
		this.setSegment(start, end);
	}
	
	public void setSegment(int start, int end){
		this.startIdx = start;
		this.endIdx = end;
		currentIdx = startIdx;
	}
	
	public ByteArray(byte[] array){
		this(array, 0, array.length);
	}
	
	
	public void add(byte elem){

	}
	
	public ByteArray(int size ){
		this.array = new byte[size];
		this.setSegment(0, size);
	}
	
	public int capacity(){
		return array.length - startIdx;
	}
	
	public int size(){
		return endIdx - startIdx ;
	}
	
	public byte quickGet(int idx){
		return array[idx + startIdx];
	}
	
	public byte get(int idx){
		if (idx + startIdx >= endIdx)
			throw new IndexOutOfBoundsException();
		return array[idx + startIdx];
	}
	
	public String toString(){
		return new String(array, startIdx, endIdx - startIdx);
	}

}
