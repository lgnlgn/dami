package org.dami.common.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

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

		ArrayList<DataStatistic> statistics = new ArrayList<DataStatistic>(2);

		
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
				for(String pair = stat.getNextStat(); pair != null; pair = stat.getNextStat())
					bw.write(pair + Constants.ENDL); 
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

	public static FileVectorConverter classificationFormatConverter(String filePath, String outPrefix){
		FileVectorReader reader = new FileVectorReader.LabelFeatureWeightLineReader(filePath);
		FileVectorWriter writer = new FileVectorWriter.LabelFeatureWeightBytesWriter(outPrefix);
		
		return new FileVectorConverter(reader, writer, 
				new DataStatistic.NormalStatistic(), new DataStatistic.LabelStatistic());
	}
	
	
	
	
	@Override
	public void convert() throws IOException {
		reader.open();
		writer.open();
		for(Vector sample = reader.next(); sample != null; sample = reader.next()){ //parsed
			writer.write(sample);
			
			if (dsl.toStat()){
				dsl.collect(sample);
			}
		}
		reader.close();
		writer.close();
		if (dsl.toStat()){
			dsl.outputStat(outPrefix);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.outPrefix + Constants.STAT_SUFFIX, true));
			bw.write(String.format("%s=%s" + Constants.ENDL, Constants.DATADESERIALIZER, this.writer.getDeserClass().getName()));
			bw.close();
		}
	}
	
	public String toString(){
		return String.format("input:%s\noutput:%s\nparser:%s\nserializer:%s", input, 
				outPrefix, this.reader.getClass().getName(), this.writer.getClass().getName());
	}

}
