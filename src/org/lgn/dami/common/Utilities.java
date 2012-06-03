package org.lgn.dami.common;

import java.util.Random;
import java.util.TreeMap;



public class Utilities {
	private static Random r = new Random();
	
	/**
	 * inner product for v1 and v2
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double innerProduct(double[] v1, double[] v2){
		double product = 0;
		for (int i = 0 ; i < v1.length; i++){
			product += v1[i] * v2[i];
		}
		return product ;
	}
	
	public static float innerProduct(float[] v1, float[] v2){
		float product = 0;
		for (int i = 0 ; i < v1.length; i++){
			product += v1[i] * v2[i];
		}
		return product ;
	}
	
	
	public static double innerProduct(double[] v1, double[] v2, int v1Start, int v2Start, int length){
		double product = 0;
		for(int i = v1Start, j = v2Start, k = 0; k < length; i++,j++,k++){
			product += v1[i] * v2[j];
		}
		return product ;
	}
	
	public static float innerProduct(float[] v1, float[] v2, int v1Start, int v2Start, int length){
		float product = 0;
		for(int i = v1Start, j = v2Start, k = 0; k < length; i++,j++,k++){
			product += v1[i] * v2[j];
		}
		return product ;
	}
	
	/**
	 * random double value between 0 and 1
	 * @return
	 */
	public static double randomDouble(){
		return r.nextDouble();
	}
	
	/**
	 * random double value between begin(inclusive) and end(exclusive)  
	 * @param begin
	 * @param end
	 * @return
	 */
	public static double randomDouble(double begin, double end){
		// random value - 0.5 * span + middle_of_(begin,end)
		return (r.nextDouble() - 0.5 ) * (end - begin) + (begin + end) / 2;
	}
	
	public static void main(String[] args){
		double[] a = {0.1,0.2,0.3};
		double[] b = {0.1,0.2,0.3};
		System.out.println(innerProduct(a, b));
	}
	
	/**
	 * std display array
	 * @param v
	 */
	public static void displayArray(double[] v){
		for (int i = 0 ; i < v.length; i++){
			System.out.print(v[i]+", ");
		}
		System.out.println();
	}
	
	
	/**
	 * convert long to byte[]
	 * @param res
	 * @return
	 */
	public static byte[] long2outputbytes(long res) { 
		byte[] targets = new byte[8];
		//对于输出方向，从左到右
		targets[7] = (byte) (res & 0xff);// 最低位 
		targets[6] = (byte) ((res >> 8) & 0xff);// 
		targets[5] = (byte) ((res >> 16) & 0xff);//  
		targets[4] = (byte) ((res >> 24) & 0xff);// 
		targets[3] = (byte) ((res >> 32) & 0xff);//  
		targets[2] = (byte) ((res >> 40) & 0xff);//  
		targets[1] = (byte) ((res >> 48) & 0xff);// 
		targets[0] = (byte) (res >>> 56);// 最高位,无符号右移。 
		return targets; 
	} 
	
	public static byte[] short2outputbytes(short res){
		byte[] targets = new byte[2];
		targets[1] = (byte) (res & 0xff);// 最低位 
		targets[0] = (byte) (res >>> 8) ;// 
		return targets;
	}
	
	public static byte[] int2outputbytes(int res) { 
		byte[] targets = new byte[4];
		//对于输出方向，从左到右
		targets[3] = (byte) (res & 0xff);// 最低位 
		targets[2] = (byte) ((res >> 8) & 0xff);// 
		targets[1] = (byte) ((res >> 16) & 0xff);//  
		targets[0] = (byte) (res >>> 24) ;// 

		return targets; 
	} 
	
	
	public static long bytes2long(byte[] b) {

		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 8; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static byte[] long2bytes(long num) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) (num >>> (56 - i * 8));
		}
		return b;
	}

	public static double getDoubleFromSer(int serint){
		int head = (serint & 0xf0000000) >>> 28; 
		int tail = (serint & 0xf000000) >> 24;
		return  head + tail /10.0; 
	}
	
	public static TreeMap<Integer, Double> parseVector(String vectorString){
		String[] info = vectorString.split("\\s+");
		TreeMap<Integer, Double> vec = new TreeMap<Integer, Double>();
		for(String str : info){
			String[] kw = str.split(":");
			vec.put(Integer.parseInt(kw[0]), Double.parseDouble(kw[1]));
		}
		return vec;
	}
}
