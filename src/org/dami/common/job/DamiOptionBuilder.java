package org.dami.common.job;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class DamiOptionBuilder {
	public static Option input(){
		return OptionBuilder.withArgName("input").hasArg()
				.withDescription("path for DB input.").create("i");
	}
	
	public static Option output(String description){
		return  OptionBuilder.withArgName("output").hasOptionalArg()
				.withDescription(description).create("o");
	}
	
	public static Option trainFile(){
		return OptionBuilder.withArgName("train").hasArg()
				.withDescription("path for training data.").create("train");
	}
	
	public static Option testFile(){
		return OptionBuilder.withArgName("test").hasArg()
				.withDescription("path for testing data.").create("test");
	}
	
	
	public static Option fileCache(){
		return OptionBuilder.hasArg(false).withDescription("use file input. Default data is loaded into RAM.").create("f");

	}
	
	public static Option loops(int defaultLoops){
		return OptionBuilder.withArgName("loops").hasOptionalArg()
				.withDescription("number of loops for iteration stop. Default = " + defaultLoops).create("l");
	}
	
	public static Option cvfold(){
		return OptionBuilder.withArgName("fold").hasOptionalArg()
				.withDescription("cross validation. If the value is setted, model will not be outputed.").create("v");
	}
	
	public static Option stop(double stop){
		return OptionBuilder.withArgName("stop").hasOptionalArg()
				.withDescription("convergence criteria for iteration stop. Default = " + stop).create("s");
	}
	
	public static Option estimation(){
		return OptionBuilder.hasArg(false)
				.withDescription("estimate the memory space for the algorithm under your parameters").create("e");
	}
}	
