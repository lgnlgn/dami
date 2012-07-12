package org.dami.common.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.dami.common.Constants;
import org.dami.common.DataStatistic;
import org.dami.common.Vector;

/**
 * Reads vectors from a file, write them to another file.
 * Construct object with FileVectorReader & Writer 
 * 
 * @author lgn
 *
 */
public class FileVectorConverter extends DataConverter<Vector, Vector>{

	public static class DataStatisticListener {

		ArrayList<DataStatistic> statistics = new ArrayList<DataStatistic>(3);

		
		public void addListener(DataStatistic... stats){
			for(DataStatistic stati : stats)
			this.statistics.add(stati);
		}
		
		public boolean toStat(){
			if (this.statistics.isEmpty())
				return false;
			else
				return true;
		}
		
		public void collect(Vector sample){
			for(DataStatistic stat : statistics){ // overall statistic
				stat.sampleInfoStat(sample);
			}
			for(int i = 0 ; i < sample.featureSize; i++){ //each feature
				for(DataStatistic stat : statistics){
					stat.featureStat(sample, i);
				}
			}
		}
		
		public void outputStat(String pathPrefix) throws IOException{
			HashSet<String> statChecker = new HashSet<String>();
			for(DataStatistic stat : this.statistics){
				BufferedWriter bw = null;
				if (statChecker.add(stat.getOutputSuffix())){// if not contain
					bw = new BufferedWriter(new FileWriter(pathPrefix + stat.getOutputSuffix()));
				}else{
					bw = new BufferedWriter(new FileWriter(pathPrefix + stat.getOutputSuffix(), true));
				}
				
				Iterator<String> iter = stat.getStatIter();
				while(iter.hasNext()){
					bw.write(iter.next() + Constants.ENDL);
				}
				bw.close();
			}
		}
	}
	
	String input ;
	String outPrefix;
	DataStatisticListener dsl;
	
	public FileVectorConverter(FileVectorReader reader, FileVectorWriter writer,
			DataStatistic... stats) {
		this.reader = reader;
		this.writer = writer;
		this.input = reader.getFilePath();
		this.outPrefix = writer.getOutPrefix();
		dsl = new DataStatisticListener();
		dsl.addListener(stats);
	}

		
	public void convert(int initVectorSize)throws IOException{
		reader.open();
		writer.open();
		Vector sample = new Vector(initVectorSize);
		for(reader.next(sample); sample.featureSize > 0;  reader.next(sample)){ //parsed
			
			if (sample.featureSize == 0)
				continue;
			
			writer.write(sample);
			
			if (dsl.toStat()){
				dsl.collect(sample);
			}
		}
		reader.close();
		writer.close();
		
		dsl.outputStat(outPrefix);

		BufferedWriter bw = new BufferedWriter(new FileWriter(this.outPrefix + Constants.STAT_SUFFIX, true));
		bw.write(String.format("%s=%s" + Constants.ENDL, Constants.DATADESERIALIZER, this.writer.getDeserClass().getName()));
		bw.write(String.format("%s=%d" + Constants.ENDL, Constants.VESTOC_STATUS, 
				((FileVectorWriter)this.writer).getVectorStatus().getVectorParameter()));
		bw.close();
	}
	
	@Override
	public void convert() throws IOException {
		this.convert(500000);
		
	}
	
	public String toString(){
		return String.format("input:%s\noutput:%s\nparser:%s\nserializer:%s", input, 
				outPrefix, this.reader.getClass().getName(), this.writer.getClass().getName());
	}
	
	public static FileVectorConverter normalclassificationFormatConverter(String filePath, String outPrefix){
		FileVectorReader reader = new FileVectorReader.LineReader(filePath, " ", ":", Vector.normalClassificationFormat());
		FileVectorWriter writer = new FileVectorWriter.BytesWriter(outPrefix,  Vector.normalClassificationFormat());
		
		return new FileVectorConverter(reader, writer, 
				new DataStatistic.CommonStatistic(), new DataStatistic.LabelStatistic());
	}

	
	/**
	 * you must make sure the file input is ordered by the aggregate column
	 * @param filePath
	 * @param output
	 * @return
	 */
	public static FileVectorConverter graphWithoutWeightConverter(String filePath, String output, int aggrColIdx){
		return graphWithoutWeightConverter(filePath, output, aggrColIdx, Vector.idOnlyFormat());
	}
	
	public static FileVectorConverter graphWithoutWeightConverter(String filePath, String output, int aggrColIdx, Vector.Status vs){
		FileVectorReader reader = new FileVectorReader.TupleReader(filePath, "\\s+", aggrColIdx, 1);
		FileVectorWriter writer = new FileVectorWriter.BytesWriter(output,  vs);
		return new FileVectorConverter(reader, writer, new DataStatistic.CommonStatistic());
	}
}
