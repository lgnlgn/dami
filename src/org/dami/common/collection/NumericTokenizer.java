package org.dami.common.collection;

import java.util.Arrays;

/**
 * Split text line by non-digit chars
 * digit related chars include : '0'~'9', '.', '+-'
 * @author lgn
 *
 */
public class NumericTokenizer {
	final int[] POWERS_OF_10 =
	      {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
	
	byte[] delimeters;
	byte[] asciiTable = new byte[128];
	
	ByteArray line;
	
	int digitIdx = 0; // a token's cursor
	int bound = 0;
	private void init(byte[] delims){
		
		Arrays.fill(asciiTable, (byte)-3);
		this.delimeters = delims;
		asciiTable[48] = 0;
		asciiTable[49] = 1;
		asciiTable[50] = 2;
		asciiTable[51] = 3;
		asciiTable[52] = 4;
		asciiTable[53] = 5;
		asciiTable[54] = 6;
		asciiTable[55] = 7;
		asciiTable[56] = 8;
		asciiTable[57] = 9;
		asciiTable[46] = -1; // decimal point
		asciiTable[58] = -3; // ':'
		asciiTable[13] = -2; // endl
		asciiTable[10] = -2; // endl
		asciiTable[43] = 100;// '+'
		asciiTable[45] = 100;// '-'
		for(int i = 0; i < delimeters.length; i++){
			asciiTable[delimeters[i]] = -3; //split 
		}
	}
	
	/**
	 * split by [tab] or [space]
	 */
	public NumericTokenizer(){
		init( new byte[]{(byte)32, (byte)9});
	}
	
	@Deprecated
	public NumericTokenizer(byte... delims){
		init(delims);
	}
	@Deprecated
	public NumericTokenizer(char... delims){
		byte[] delim = new byte[delims.length];
		for(int i = 0 ; i < delim.length ; i ++)
			delim[i] =( byte)delims[i];
		init(delim);
	}
	
	/**
	 * for parse a new line;
	 * @param ba
	 */
	public void load(ByteArray ba){
		this.line = ba;
		this.digitIdx = 0;
		this.bound = line.size();
	}
	
	
	public boolean hasNext(){
		if (digitIdx >= bound){
			return false;
		}
		return true;
	}
	
	
	/**
	 * return a number  
	 * @return Integer or Float
	 */
	public Object nextNumber(){
		long value = 0;
		boolean isFloat = false;
		int dpIndex = 0;
		boolean isNegative = false;
		byte k = line.quickGet(digitIdx);
		for(; digitIdx < bound && asciiTable[line.quickGet(digitIdx)] > -2 ; digitIdx += 1){
			k = line.quickGet(digitIdx);
			if (k == 43){ // '+'
//				digitIdx += 1;
				continue;
			}else if (k == 45){ // '-'
				isNegative = true;
//				digitIdx += 1;
				continue;
			}
			if (asciiTable[k] == -1){
				isFloat = true;
				dpIndex = digitIdx;
			}else
				value = value * 10 + asciiTable[k];
//			digitIdx += 1;
		}
		int endIdx = digitIdx;
		//
		for(; digitIdx < bound && asciiTable[line.quickGet(digitIdx)] <= -2 ; digitIdx += 1){
			;
		}	
		if (!isNegative)
			if (isFloat){
				if (endIdx - dpIndex - 1 < 10)
					return new Float((value + 0.0) / POWERS_OF_10[endIdx - dpIndex - 1]);
				else
					return new Float(value / Math.pow(10, endIdx - dpIndex - 1));
			}else{
				return new Integer((int)value);
			}
		else
			if (isFloat){
				if (endIdx - dpIndex - 1 < 10)
					return new Float(-(value + 0.0) / POWERS_OF_10[endIdx - dpIndex - 1]);
				else
					return new Float(-value / Math.pow(10, endIdx - dpIndex - 1));
			}else{
				return new Integer(-(int)value);
			}
	}
	
	/**
	 * 64 bits for key:value pair
	 * first 32 bits(integer) for id, last 32 bits(float) for weight
	 * @return 64 bits(long) for key:value pair 
	 */
	public long nextKeyValuePair(){
		long id = (Integer)nextNumber();
		Object w = nextNumber();
		Float weight = (w instanceof Float )? (Float)w : (Integer)w;
		long result = (((long)id) << 32) | (Float.floatToIntBits(weight) & 0xffffffffl);
		return result;
	}
	
	public static int extractFeatureId(long kv){
		return (int) ((kv & 0xffffffff00000000l) >>> 32);
	}
	
	public static float extractWeight(long kv){
		int w = (int)(kv & 0xffffffff);
		return Float.intBitsToFloat(w);
	}
}
