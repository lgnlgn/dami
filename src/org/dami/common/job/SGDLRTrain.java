package org.dami.common.job;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.dami.classification.common.Evaluator;
import org.dami.classification.lr.SGDLogisticRegression;
import org.dami.common.Constants;
import org.dami.common.Utilities;
import org.dami.common.io.FileVectorReader;
import org.dami.common.io.VectorStorage;

public class SGDLRTrain {
	
	private Options buildOptions(){
		Options opts = new Options();
		Option weight = OptionBuilder.withArgName("label weight").withValueSeparator(' ').hasArgs(2)
				.withDescription("boost a label for training balance. For example: -w1 2 ." +
						"No need to specify if the DB contains a 'dataset' property").create("w");
		Option learningSpeed = OptionBuilder.withArgName("learning speed").hasOptionalArg()
				.withDescription("gradient speed. A default value is decided by the algorithm.").create("a");
		Option regularization = OptionBuilder.withArgName("regularization").hasOptionalArg()
				.withDescription("regularization for learning. A default value is decided by the algorithm.").create("r");

		
		opts.addOption(DamiOptionBuilder.input())
			.addOption(DamiOptionBuilder.output("path for LR model"))
			.addOption(DamiOptionBuilder.fileCache())
			.addOption(DamiOptionBuilder.cvfold())
			.addOption(DamiOptionBuilder.estimation())
			.addOption(DamiOptionBuilder.stop(SGDLogisticRegression.DEFAULT_STOP))
			.addOption(DamiOptionBuilder.loops(SGDLogisticRegression.DEFAULT_LOOPS))
			.addOption(learningSpeed).addOption(regularization).addOption(weight);
		
		return opts;
	}
	
	public void runJob(String[] args) throws Exception{
		Options opts = buildOptions();
		CommandLineParser parser = new org.apache.commons.cli.GnuParser();
		CommandLine cmd = parser.parse(opts, args);
		
		if (args.length < 1){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("exe", opts);
			System.exit(0);
		}
		String path = cmd.getOptionValue("i");
		if (path == null){
			System.out.println("You must specify DB input");
			System.exit(0);
		}else if ( !new File(path).exists() || !new File(path + ".sta").exists()){
			System.out.println("DB file not found, or DB format not correct!");
			System.exit(0);
		}
		Properties prop = new Properties();
		Properties weight = cmd.getOptionProperties("w");
		if (weight.size() != 0){
			String label = weight.keys().nextElement().toString();
			prop.put("-w" + label, weight.get(label).toString());
		}
		String loops = cmd.getOptionValue("l");
		if (loops != null){
			prop.put(Constants.LOOPS, loops);
		}
		String learningSpeed = cmd.getOptionValue("a");
		if (learningSpeed != null){
			prop.put(Constants.ALPHA, learningSpeed);
		}
		String regularization = cmd.getOptionValue("r");
		if (regularization != null){
			prop.put(Constants.LAMBDA, regularization);
		}
		String stop = cmd.getOptionValue("s");
		if (stop != null){
			prop.put(Constants.STOPCRITERIA, stop);
		}
		SGDLogisticRegression lr = new SGDLogisticRegression();

		if (cmd.hasOption("e")){
			System.out.println(Utilities.RAMEstimation(cmd, prop, lr, path));
			System.exit(0);
		}
		
		FileVectorReader fvr = FileVectorReader.getBytesReaderFromSta(path);
		VectorStorage db = null;
		if (cmd.hasOption("f")){
			db = new VectorStorage.FileStorage(fvr);
		}else{
			db = new VectorStorage.RAMCompactStorage(fvr);
		}

		String fold = cmd.getOptionValue("v");
		String outPath = cmd.getOptionValue("o");
		if (fold != null){ //cross validation
			try{
				int v = Integer.parseInt(fold);		
				lr.loadData(db);
				lr.setProperties(prop);
				Evaluator acc = new Evaluator.BinaryAccuracy();
				lr.crossValidation(v, acc);
				System.out.println(acc);
				
			}catch (NumberFormatException e) {
				throw new NumberFormatException("cross validation parameters error");
			}
		}else if(outPath!= null){
			lr.loadData(db);
			lr.setProperties(prop);
			lr.train();
			lr.saveModel(outPath);
		}else{
			System.out.println("Output path not found!");
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		SGDLRTrain lr = new SGDLRTrain();
		lr.	runJob(args);
	}
}
