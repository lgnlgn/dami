package org.dami.common.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;

public class TestReader {

	public static void testlineReader() throws IOException{
		String input = "e:/data/mushrooms.txt";
		
		int i = 0;
		FileVectorReader fvr = FileVectorReader.normalClassificationFormatLineReader(input);
		fvr.open();
		Vector v = new Vector();
		for(fvr.next(v); v.featureSize > 0; fvr.next(v)){
			System.out.println(v);
		}
		fvr.close();
		System.out.println(i);
	}
	
	public static void test2() throws IOException, ClassNotFoundException{
		String serfile = "e:/data/pr.ser";
		FileVectorReader reader = FileVectorReader.getBytesReaderFromSta(serfile);
		System.out.println(reader);
	}
	
	public static void testIdReader() throws IOException{
		String input = "e:/data/pr2.ser";
		int i = 0;
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(input);
		fvr.open();
		Vector v = new Vector();
		for(fvr.next(v); v.featureSize > 0; fvr.next(v)){
			System.out.println(v);
		}
		fvr.close();
		System.out.println(i);
	}
	
	public static void testClassifyReader() throws IOException{
		Vector v = new Vector();
		FileVectorReader r1 = FileVectorReader.normalClassificationFormatLineReader("e:/data/mushrooms.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:/muv.txt"));
		r1.open();
		for(r1.next(v); v.featureSize >= 0; r1.next(v)){
			bw.write(v.label + " ");
			for(int i = 0 ; i < v.featureSize; i++){
				bw.write(String.format("%d:%.0f ", v.features[i], v.weights[i]));
			}
			bw.write("\n");
		}
		r1.close();
		bw.close();
	}
	
	public static void testStorage()throws IOException{
		Vector v = new Vector();
//		FileVectorReader r1 = FileVectorReader.normalClassificationFormatLineReader("d:/mu");
		FileVectorReader r1 = FileVectorReader.getBytesReaderFromSta("d:/mu");
		BufferedWriter bw = new BufferedWriter(new FileWriter("d:/muv.txt"));
		r1.open();
		VectorStorage vs = new VectorStorage.RAMCompactStorage(r1);
		vs.reOpenData();
		for(vs.next(v); v.featureSize >= 0; vs.next(v)){
			bw.write(v.label + " ");
			for(int i = 0 ; i < v.featureSize; i++){
				bw.write(String.format("%d:%.0f ", v.features[i], v.weights[i]));
			}
			bw.write("\n");
		}
		vs.close();
		bw.close();
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
//		testIdReader();
//		testClassifyReader();
		testStorage();
	}	

}
