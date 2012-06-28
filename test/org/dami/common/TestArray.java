package org.dami.common;

import java.util.ArrayList;

import org.dami.significanttesting.IntArray;

public class TestArray {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
//		ArrayList<Integer> a1 = new ArrayList<Integer>();
		long t1 = System.currentTimeMillis();
//		for(int i = 0 ; i < 50000000; i++){
//			a1.add(i);
//		}
//		long t2 = System.currentTimeMillis();
//		System.out.println(t2-t1);
		
		IntArray ia = new IntArray(10);
		for(int i = 0 ; i < 20; i++){
			ia.add(i);
		}
		System.out.println(ia.size());
		for(int i = 0 ; i < ia.size(); i++){
			System.out.println(ia.get(i) + "---");
		}
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1);
		Thread.sleep(5555);
	}

}
