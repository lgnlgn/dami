package org.dami.significanttesting;

import java.io.IOException;

import org.dami.common.io.FileVectorReader;
import org.dami.common.io.FileVectorWriter;


public abstract class RandomSwapper {

	FileVectorReader reader ;
	FileVectorWriter writer ;
	
	int itersPerLoop;
	int loops;
	
	public RandomSwapper(FileVectorReader reader , FileVectorWriter writer,int itersPerLoop, int loops){
		this.reader = reader;
		this.writer = writer;
		this.itersPerLoop = itersPerLoop;
		this.loops = loops;
	}
	
	abstract public void runSwap()throws IOException;
}
