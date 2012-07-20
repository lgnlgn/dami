package org.dami.common.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.dami.common.collection.ByteArray;
import org.dami.common.collection.NumericTokenizer;
import org.dami.common.collection.SortingBytesArray;

/**
 * 
 * aggregate(group) file by a column
 * use {@link #aggregate(String, String, int)}
 * @author lgn
 *
 */
public class TupleAggregator {

	
/*	
	public void splitData(String filePath,  int aggColumn, String outPrefix, int blocks) throws NumberFormatException, IOException{
		long t = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		BufferedWriter[] bws = new BufferedWriter[blocks];
		for(int i = 0 ; i < blocks; i++){
			bws[i] = new BufferedWriter(new FileWriter(outPrefix + "." + i), 1024 * 1024 * 10);
		}
		int l = 0;
		for(String line = br.readLine(); line != null; line = br.readLine()){
			StringTokenizer st = new StringTokenizer(line);
			String a = null;
			for(int i = 0 ; i < aggColumn; i++)
				a = st.nextToken();
			int value = Integer.parseInt(a);
			BufferedWriter bw = bws[value % blocks];
			bw.write(line + "\n");
			l ++;
			if (l % 1000000 == 0){
				System.out.print("!");
			}
		}
		br.close();
		for(int i = 0 ; i < blocks; i ++){
			bws[i].flush();
			bws[i].close();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("time spend(ms) : " + (t2 -t));
		
	}
	*/
	public void splitData(String filePath,  int aggColumn, String outPrefix, int blocks) throws NumberFormatException, IOException{
		long t = System.currentTimeMillis();
		FileBytesReader br = new FileBytesReader(filePath);
		BufferedOutputStream[] bws = new BufferedOutputStream[blocks];
		for(int i = 0 ; i < blocks; i++){
			bws[i] = new BufferedOutputStream(new FileOutputStream(outPrefix + "." + i), 1024 * 1024 * 300 / blocks);
		}
		NumericTokenizer dt = new NumericTokenizer();
		int l = 0;
		for(ByteArray line = br.readLine(); line != null; line = br.readLine()){
			dt.load(line);
//			System.out.print(line);
			Object aggc = null;
			for(int i = 0 ; i < aggColumn; i++){
				aggc = dt.nextNumber();
			}
//			try{
			if (aggc != null){
				int value = (Integer)aggc;
				BufferedOutputStream bw = bws[value % blocks];
				bw.write(line.array, line.startIdx, line.size());
//				bw.write(line.toString().getBytes());
			}else{
				System.out.println(line);
			}
//			}catch (NullPointerException e){
//				System.out.println(line.toString());
//			}

			l ++;
			if (l % 1000000 == 0){
				System.out.print("!");
			}
		}
		br.close();
		for(int i = 0 ; i < blocks; i ++){
			bws[i].flush();
			bws[i].close();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("time spend(ms) : " + (t2 -t));
		
	}
	
	public void merge(String inDir, String output, int aggColumn) throws IOException{
		File dir = new File(inDir);
	
		if (dir.exists() && dir.isDirectory()){
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.close();
			SortingBytesArray sba = new SortingBytesArray();
			for(File f : dir.listFiles()){
				sba.clear();
				long t1 = System.currentTimeMillis();
				System.out.print("loading... ");
				sba.readIn(f, aggColumn);
				long t2 = System.currentTimeMillis();
				System.out.print((t2-t1) +" ms \n sorting...");
				sba.sort();
				long t3 = System.currentTimeMillis();
				System.out.print((t3-t2) +" ms \n writing...");
				sba.writeOut(output, true);
				long t4 = System.currentTimeMillis();
				System.out.println((t4-t3) +" ms finish");
			}
		}else{
			throw new IOException("directory NOT FOUND! : " + inDir);
		}
	}
	
	public void aggregate(String input, String output, int aggColumn) throws IOException{
		long t = System.currentTimeMillis();
		File tmpDir = new File( output + "." + t);
		long fileSize = new File(input).length();
		long blocks = fileSize / 200000000;
		if (blocks > 0){
			if (!tmpDir.mkdir())
				throw new IOException("<DIRECTORY PATH> of the file output path  " + output+ " IS NOT EXIST !" );
			splitData(input, aggColumn, tmpDir.getAbsolutePath() + "/tmp", (int)blocks);
			this.merge(tmpDir.getAbsolutePath(), output, aggColumn);
			// remove temp dir and file
			for(File tmp : tmpDir.listFiles()){
				tmp.delete();
			}
			tmpDir.delete();
		}else{
			File f = new File(input);
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.close();
			SortingBytesArray sba = new SortingBytesArray();
			long t1 = System.currentTimeMillis();
			System.out.print("loading... ");
			sba.readIn(f, aggColumn);
			long t2 = System.currentTimeMillis();
			System.out.print((t2-t1) +" ms \n sorting...");
			sba.sort();
			long t3 = System.currentTimeMillis();
			System.out.print((t3-t2) +" ms \n writing...");
			sba.writeOut(output, true);
			long t4 = System.currentTimeMillis();
			System.out.println((t4-t3) +" ms finish");
		}
		
	}


}
