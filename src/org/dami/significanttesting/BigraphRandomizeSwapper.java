package org.dami.significanttesting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Arrays;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;

import org.dami.common.collection.IntArray;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;

/**
 * self-loop approach for ARISTIDES GIONIS's binary swap randomization
 * see "Accessing data mining result via swap randomization"
 * @author lgn
 *
 */
public class BigraphRandomizeSwapper extends RandomSwapper{
	final static String outputSuffix = ".dat";
	HashSet<Long> graph = new HashSet<Long>();
	
	IntArray jref ; 
	IntArray iref ;

	Random r = new Random();
	int n = 0; // total # of elements
	
	public BigraphRandomizeSwapper(FileVectorReader reader,
			FileVectorWriter writer, int iterlen, int loops) {
		super(reader, writer, iterlen, loops);
		
	}

//	ArrayList<Integer> jref = new ArrayList<Integer>();
//	ArrayList<Integer> iref = new ArrayList<Integer>();

	public int swap(){
		int a = r.nextInt(n);
		int b = r.nextInt(n);
		int aj = jref.get(a);
		int ai = iref.get(a);
		int bj = jref.get(b);
		int bi = iref.get(b);
		if (!graph.contains(  ((long)aj << 32)|(long)(bi) ) && !graph.contains( ((long)bj << 32)|(long)(ai))){
			graph.remove(((long)aj << 32)|(long)(ai));
			graph.remove(((long)bj << 32)|(long)(bi));
			graph.add(((long)aj << 32)|(long)(bi));
			graph.add(((long)bj << 32)|(long)(ai));
			iref.set(a, bi);
			iref.set(b, ai);
			return 1;
		}
		return 0;
	}
	
	@Override
	public void runSwap() throws IOException {
		int attributes = 1024 ; //for the capacity of IntegerArray initialization
		long t = 0;
		FileVectorReader input = (FileVectorReader)reader;
		File stat = new File(input.getFilePath() + Constants.STAT_SUFFIX);
		if (stat.exists() && stat.isFile()){ 
			// if there exist a '.sta' file
			// good capacity can avoid multiple array enlargements  
			Properties p = new Properties();
			Reader statReader  = new FileReader(stat);
			p.load(statReader);
			statReader.close();
			try{
				attributes = Math.max(attributes, Utilities.getIntFromProperties(p, Constants.TOTAL_FEATURES));
			}catch(NullPointerException e){
				;
			}
		}
		
		jref = new IntArray(attributes);
		iref = new IntArray(attributes);
		
		int rowc = 0;
		int maxfeatureSize = 0;
		System.out.println("loading data~");
		reader.open();
		Vector sample = new Vector();
		for(reader.next(sample); sample.featureSize >= 0; reader.next(sample)){
			for( int i = 0 ; i < sample.featureSize; i++){
				jref.add(rowc);
				iref.add(sample.features[i]);
				graph.add(((long)rowc << 32) | ((long)sample.features[i]));
				n += 1;
			}
			rowc += 1;
			maxfeatureSize = Math.max(maxfeatureSize, sample.featureSize);
		}
		reader.close();
		System.out.println("loading data finished!  " );
		
		Vector row = sample;
		int swaps = 0;
		int size = itersPerLoop * loops;
		String outPrefix = writer.getOutPrefix();
		// starting swap & output
		for(int i = 0 ; i < size; i++){
			if (i % itersPerLoop == 0){ 
				int k = 0; //rowid
				writer.setOutPrefix(String.format("%s.%d%s", outPrefix, (i/itersPerLoop), outputSuffix));
				writer.open(); // start
				int idx = 0;
				for (int l = 0; l<= n; l++){
					if (l < n && (k == jref.get(l))){
						row.features[idx] = iref.get(l); // feature id
						idx ++ ;
					}else{
						row.featureSize = idx;
						Arrays.sort(row.features, 0, idx);
						writer.write(row); // write out a vector
						if (l < n){
							idx = 0;
							row.features[idx++] = iref.get(l);
							k = jref.get(l);
						}
					}
				}
				writer.close();
				
				if (i > 0){
					long t2 = System.currentTimeMillis();
					
					System.out.println(String.format("%.1f\t%d\t%d\t%.4f\t%.5f", 
							(t2-t)/1000.0, i, swaps, (swaps + 0.0)/i, (swaps + 0.0)/n));
					t = t2;
				}else{
					//first time 
					System.out.println("time(s)\titerlen\tswapped\t%/loop\t%/all");
					System.out.println("0.0\t0\t0\t0.0\t0.0");
					t = System.currentTimeMillis();
				}
			}
			swaps += swap();
		}
		
	}
	
	/*
	public void job(String dbfile, String prefix, int iterlen, int loop) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(dbfile));
		int rowc = 0;
		for(String line = br.readLine(); line!= null ; line = br.readLine()){
			String[] items = line.split("\\s+");
			for (String item : items){
				int itemid = new Integer(item);
				jref.add(rowc);
				iref.add(itemid);
				graph.add(((long)rowc << 32) | (long)itemid);
				n++;
			}
			rowc++ ;
		}
		br.close();
		int maxitem = Collections.max(iref);
		int swaps = 0;
		int size = iterlen * loop;
		for(int i = 0 ; i < size; i++){
			if (i % iterlen == 0){
				
				int k = 0;
				int[] row = new int[maxitem + 1];
				for(int p = 0 ; p < row.length ; p++) 
					row[p] = -1;
				BufferedWriter bw = new BufferedWriter(new FileWriter(prefix + "." + (i/iterlen) + ".dat"));
				int idx = 0;
				for (int l = 0; l<= n; l++){
					if (l < n && (k == jref.get(l))){
						row[idx] = iref.get(l);
						idx ++ ;

					}else{
						Arrays.sort(row, 0, idx);

						for(int ii = 0 ; ii < idx ; ii++){
							bw.write(row[ii] + " ");
						}
						bw.write(endl);
						if (l < n){
							for(int p = 0 ;p <= idx ; p++){
								row[p] = -1;
							}
							idx = 0;
							row[idx] = iref.get(l);
							k = jref.get(l);
							idx +=1;
						}
					}
				}
				bw.close();
				if (i > 0){
					System.out.println(i + "\t" + swaps + "\t" + ((swaps + 0.0)/i) + "\t" + ((swaps + 0.0)/n));
				}else{
					System.out.println("0  0  0  0");
				}
			}
			swaps += swap();
		}
		
	}
	*/
	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub

	}




}
