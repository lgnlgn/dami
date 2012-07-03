package org.dami.common.collection;

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
	
	/**
	 * append an item
	 * @param elem
	 */
	public void add(int elem){
		if (capacity() == currentIndex){
			enlarge();
		}
		array[currentIndex++] = elem;
	}
	
	/**
	 * Capacity expansion with default MAX_ENLARGE_SIZE;
	 */
	public void enlarge(){
		int[] newarr = new int[Math.min(array.length * 3 / 2 + 1, array.length + MAX_ENLARGE_SIZE)];
		System.arraycopy(array, 0, newarr, 0, array.length);
		array = newarr;
	}
	
	/**
	 * 
	 * @param index
	 * @param elem
	 */
	public void set(int index, int elem) throws IndexOutOfBoundsException{
		if (index >= currentIndex)
			throw new IndexOutOfBoundsException();
		this.array[index] = elem;
		
	}
	
	/**
	 * increase capacity when index out of range
	 * be careful that the size of the Array will be set to index if Capacity just enlarged
	 * @param index
	 * @param elem
	 * @return true if capacity expansion occurs
	 */
	public boolean setForcibly(int index, int elem){
		if (index >= capacity()){
			while(index >= capacity()){
				enlarge();
			}
			this.array[index] = elem;
			currentIndex = index + 1;
			return true;
		}else{
			this.array[index] = elem;
			if (index >= currentIndex)
				currentIndex = index + 1;
			return false;
		}
	}
	
	public int capacity(){
		return array.length;
	}
	
	public int get(int index){
		if (index >= currentIndex)
			throw new IndexOutOfBoundsException();
		return this.array[index];
		
	}
	
	public int size(){
		return currentIndex;
	}
	
	public void clear(){
		array = new int[1000];
		currentIndex = 0;
	}
}
