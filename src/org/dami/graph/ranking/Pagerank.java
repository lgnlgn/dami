package org.dami.graph.ranking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Properties;

import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.Vector;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.VectorStorage;
import org.dami.graph.common.Ranking;

/**
 * we use Vector to represent node's information
 * id for node's id; feature for node's outlinks/inlinks; featureSize for node's outdegree/indegree
 * @author lgn
 *
 */
public class PageRank implements Ranking{

	
	
	final static String ALPHA = "alpha";
	final static String LOOPS = "loops";
	final static String TMPDIR = "tmpdir";
	
	public float[] outLinkDegreeSums = null; //how many outlinks dose an node have, fuzzy for weighted links
	public float[] pageranks = null;  //the pagerank of each node 
	
	private float initValue = 1.0f;
	
	int nodes = 0;
	VectorStorage dataEntry;
	Vector sample;
	
	float alpha = 0.85f;
	String tmpDir = ".";
	int loops = 10;
	File tmp;
	
	public void loadData(String outLinkSerFile, String inLinkSerFile) throws IOException{
		FileVectorReader outLinkReader = FileVectorReader.getBytesReaderFromSta(outLinkSerFile);
		FileVectorReader inLinkReader = FileVectorReader.getBytesReaderFromSta(inLinkSerFile);
		
		VectorStorage vs1 = new VectorStorage.FileStorage(outLinkReader);
		VectorStorage vs2 = new VectorStorage.FileStorage(inLinkReader);
		
		this.loadData(vs1, vs2);
	}
	
	public void loadData(VectorStorage outLinkData, VectorStorage inLinkData) throws IOException{
		int maxNodeId = Math.max(Utilities.getIntFromProperties(outLinkData.getDataSetInfo(), Constants.MAXFEATUREID), 
				Utilities.getIntFromProperties(inLinkData.getDataSetInfo(), Constants.MAXFEATUREID));
		dataEntry = inLinkData;
		outLinkDegreeSums = new float[maxNodeId + 1];
		pageranks = new float[maxNodeId + 1];
		sample = new Vector();
		Arrays.fill(outLinkDegreeSums, 0);
		Arrays.fill(pageranks, initValue);
		
		outLinkData.open();
		for(outLinkData.next(sample); sample.featureSize >= 0; outLinkData.next(sample)){
			for(int i = 0 ; i < sample.featureSize; i++)
				outLinkDegreeSums[sample.id] += sample.weights[i];
		}
		outLinkData.close();
		tmp = new File(tmpDir + "/tmp");
	}
	
	/**
	 * Three default parameters are :
	 *  "alpha"=0.85 ; "loops"=10 ; "tmpdir"="."
	 * 
	 * @param p
	 */
	public void setParameters(Properties p ){
		if (p.contains(ALPHA)){
			this.alpha = (float)Utilities.getDoubleFromProperties(p, ALPHA);
		}
		if (p.contains(LOOPS)){
			this.loops = Utilities.getIntFromProperties(p, LOOPS);
		}
		if (p.contains(TMPDIR)){
			this.tmpDir = Utilities.getStrFromProperties(p, TMPDIR);
			tmp = new File(tmpDir + "/tmp");
		}
	}

	
	/**
	 * 
	 * @return average of pagerank differences;
	 * @throws IOException
	 */
	private double updatePageranks() throws IOException{
		double diffs = 0;
		
		if (tmp.exists()){
			// update pageranks with last loops' values
			byte[] buff = new byte[1024 * 8];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tmp));
			for(int reads = bis.read(buff); reads != -1; reads = bis.read(buff)){
				ByteBuffer buffer = ByteBuffer.wrap(buff);
				for(int k = 0 ; k < reads; k+= 8){
					int nodeId = buffer.getInt(k);
					float weight = buffer.getFloat(k+4);
					float weight2update = (1.0f - alpha)  + weight;
					diffs += Math.abs(pageranks[nodeId] - weight2update);
					pageranks[nodeId] = weight2update;
				}
			}
			bis.close();
//			return diffs / nodes;
			return diffs / pageranks.length;  // approx, ( continues id )
		}else{
			// first loop. do nothing
			return 0;
		}
		
	}
	
	@Override
	public void rank() throws IOException {
		long timeStart = System.currentTimeMillis();
		long timeEnd = 0;
		if (outLinkDegreeSums == null)
			throw new RuntimeException("data set not loaded yet!");
		
		for(int l = 0 ; l < loops; l ++){
			dataEntry.reOpenData();
			
			double currentDiff = this.updatePageranks();
			timeEnd = System.currentTimeMillis();
			System.out.println(String.format("loop %d\ttime(ms):%d\tavg_diff:%.6f", l, (timeEnd-timeStart), currentDiff));
			timeStart = timeEnd;
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpDir + "/tmp"));
			//--------- core computation-------------
			
			for(dataEntry.next(sample); sample.featureSize >= 0; dataEntry.next(sample)){
				if (sample.featureSize == 0)
					continue;
//				if (outLinkDegreeSums[sample.id] <= 0) // prepare for counting nodes  
//					outLinkDegreeSums[sample.id] = 1.0f;

				float votedValue = 0.0f;
				// receive all neighbor votes' weight with average voting probability
				// sigma each node(i) {edge_weight_from(i) * pagerank(i) / out_degrees_total(i)}
				for(int i = 0 ; i < sample.featureSize; i++){
					votedValue +=  sample.weights[i] * pageranks[sample.features[i]] / outLinkDegreeSums[sample.features[i]] ;
				}
				votedValue *= alpha;
				
				bos.write(Utilities.int2outputbytes(sample.id));
				bos.write(Utilities.float2OutputBytes(votedValue));
				
			}
			bos.close();
//			if (nodes == 0){ //aren't count yet
//				for(int i = 0 ; i < outLinkDegreeSums.length; i++){
//					if (outLinkDegreeSums[i] > 0)
//						nodes += 1;
//				}
//			}
		}
		dataEntry.close();
		// last 
		double currentDiff = this.updatePageranks();
		timeEnd = System.currentTimeMillis();
		System.out.println(String.format("loop final\ttime(ms):%d\tavg_diff:%.6f", (timeStart-timeEnd), currentDiff));
		
		tmp.delete();

	}

	public void writeOut(String outPath) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
		for(int i = 0 ; i < pageranks.length; i++){
			if (pageranks[i] != initValue)
				bw.write(String.format("%d\t%.6f%s", i, pageranks[i], Constants.ENDL));
		}
		bw.close();
	}
	
}
