package org.dami.significanttesting;

/**
 * do not support remove function
 * @author lgn
 *
 */
public class IntArray {
	final static int MAX_ENLARGE_SIZE = 1024*1024*8;
	private int[] array;
	private int currentIndex = 0;
	public IntArray(){
		this(1024);
	}
	
	public IntArray(int initSize){
		this.array = new int[initSize];
	}
	
	public void add(int elem){
		if (array.length == currentIndex){
			int[] newarr = new int[Math.min(array.length * 3 / 2 + 1, array.length + MAX_ENLARGE_SIZE)];
			System.arraycopy(array, 0, newarr, 0, array.length);
			array = newarr;
		}
		array[currentIndex++] = elem;
	}
	
	public void set(int index, int elem){
		this.array[index] = elem;
	}
	
	
	public int get(int index){
		return this.array[index];
	}
	
	public int size(){
		return currentIndex;
	}
}
