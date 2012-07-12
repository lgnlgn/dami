package org.dami.recommendation.common;

import java.io.IOException;

import org.dami.common.DataStatistic;
import org.dami.common.Utilities;
import org.dami.common.Vector;
import org.dami.common.io.FileVectorConverter;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;

public class CFDataConverter {
	/**
	 * a special BytesWriter for star type CF data
	 * 
	 * @author lgn
	 *
	 */
//	public static class StarsBytesWriter extends FileVectorWriter.BytesWriter{
//
//		public StarsBytesWriter(String db) {
//			super(db, Vector.idOnlyFormat());
//
//		}
//
//		public void write(Vector sample) throws IOException {
//			ostream.write(Utilities.int2outputbytes(sample.featureSize));
//			ostream.write(Utilities.int2outputbytes(sample.id));
//			for(int i = 0 ; i < sample.featureSize; i++){
//				int ratingHead = (int)sample.weights[i];
//				int ratingTail = ((int)(sample.weights[i] * 10)) % 10;
//				ostream.write(Utilities.int2outputbytes(
//						(ratingHead << 28) |
//						(ratingTail << 24) |
//						sample.features[i]));
//			}
//
//		}
//	}
	
	public static FileVectorConverter starsRatingDataConverter(String filePath, String output, String split){
		FileVectorReader reader = new FileVectorReader.TupleReader(filePath, split, 0, 3);
		FileVectorWriter writer = new FileVectorWriter.BytesWriter(output, new Vector.Status(0x1 + 0x8));
		return new FileVectorConverter(reader, writer, new DataStatistic.CommonStatistic());
	}

	public static void main(String[] args) throws IOException{
		String input = "e:/data/ml-10M100K/movielens.txt.train";
		String output = "e:/data/ml-10M100K/movielens.train";
		FileVectorConverter fvc = starsRatingDataConverter(input, output, "\\s");
		fvc.convert(500000);
	}
	
}
