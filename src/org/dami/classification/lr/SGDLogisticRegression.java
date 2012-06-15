package org.dami.classification.lr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import org.dami.classification.common.Classifier;
import org.dami.classification.common.Evaluator;
import org.dami.common.DataStorage;
import org.dami.common.Constants;
import org.dami.common.Sample;
import org.dami.common.Utilities;


public class SGDLogisticRegression implements Classifier{
	
	final static double initWeight = 0;
	final static int LABELRANGEBASE = 256;
	
	public double[] featureWeights = null;
	
	protected DataStorage dataEntry= null;
	protected int[][] dataInfo = null;
	protected int[] labels = null;
	
	protected Double alpha = null;
	protected Double lambda = null;
	protected Double stop = null;
	private boolean alphaSetted = false;
	private boolean lambdaSetted = false;
	private boolean stopSetted = false;
	
	protected double minAlpha = 0.00001;
	protected double minLambda = 0.00000001;
	
	protected Integer loops = 30;
	protected int fold = 5;
	
	protected int samples = 0; 
	protected int maxFeatureId = -1;
	 
	protected int biasLabel = 0; // 0 or 1, is an transformed label
	protected int biasWeightRound = 1;
	// for accuracy stop
	protected int minLabel = 0;  // 0 or 1, same as biasLabel
	
	
	@Override
	public void loadData(DataStorage data) throws Exception {
		dataEntry = data;
		if (this.dataEntry.getDataSetInfo() == null){
			maxFeatureId = 65535;
		}else{
			maxFeatureId = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(), Constants.MAXFEATUREID);
					
			String tmpInfo = Utilities.getStrFromProperties(dataEntry.getDataSetInfo(),Constants.DATASET_INFO);
			this.dataInfo = new int[LABELRANGEBASE * 2][];
			if (Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.NUMBERS_CLASS) > 2){
				dataEntry.close();
				throw new RuntimeException("Data Set contains more than 2 classes");
			}
			_loadDataInfo(tmpInfo);
		}
	}

	@Override
	public void train() throws Exception {
		this.init();
		this._train(Integer.MAX_VALUE, 0); // all samples for training
		this.dataEntry.close();
	}
	
	private void init(){
		featureWeights = new double[maxFeatureId + 1];
		for(int i = 0 ; i< featureWeights.length; i++){
			featureWeights[i] = initWeight;
		}
	}
	
	private double gradientDescend(Sample sample){
		
		double weightSum = 0;
		
		for(int i = 0 ; i < sample.featureSize; i++){
			weightSum+= featureWeights[sample.features[i]] * sample.weights[i];
		}
		double tmp = Math.pow(Math.E, -weightSum); //e^-sigma(x)
		double error = sample.label - (1/ (1+tmp)); 
		double partialDerivation =  tmp  / (tmp * tmp + 2 * tmp + 1) ;

		for(int i = 0 ; i < sample.featureSize; i++){
			featureWeights[sample.features[i]] += 
					alpha * (error * sample.weights[i] * partialDerivation) * sample.weights[i] ;
		}
		return error;
	}
	
	private void _train(int fold, int remain) throws IOException{

		double avge = 99999.9;
		double lastAVGE = Double.MAX_VALUE;
		
		double corrects  = 0;
		double lastCorrects = -1;
		
		
		double multi = (biasWeightRound * dataInfo[LABELRANGEBASE + labels[minLabel]][1] + dataInfo[LABELRANGEBASE + labels[minLabel ^ 1]][1] + 0.0)
				/ (dataInfo[LABELRANGEBASE + labels[minLabel]][1] + dataInfo[LABELRANGEBASE + labels[minLabel ^ 1]][1]);
		
		for(int l = 0 ; l < Math.min(10, loops) || l < loops && (Math.abs(1- avge/ lastAVGE) > stop || Math.abs(1- corrects/ lastCorrects) > stop * 0.1); l++){
			lastAVGE = avge;
			lastCorrects = corrects;
			dataEntry.reOpenData();
			long timeStart = System.currentTimeMillis();
			
			int c =1; //for n-fold cv
			double error = 0;
			double sume = 0;
			corrects = 0;
			int cc = 0;
			
			for(Sample sample = dataEntry.next(); sample != null; sample = dataEntry.next(), c+=1){
				if (c % fold == remain){ // no train
					;
				}else{ //train
					if (sample.label == this.biasLabel){ //bias; sequentially compute #(bias - 1) times
						for(int bw = 1 ; bw < this.biasWeightRound; bw++){ //bias
							for(int i = 0 ; i < sample.count; i++)
								this.gradientDescend(sample);
						}
					}
					for(int i = 0 ; i < sample.count; i++){
						error = gradientDescend(sample);
						if (Math.abs(error) < 0.5)//accuracy
							if (sample.label == this.biasLabel)
								corrects += this.biasWeightRound;
							else
								corrects += 1; 
						cc += 1;
						sume += Math.abs(error);
					}
				}
			}

			avge = sume / cc;
			
			long timeEnd = System.currentTimeMillis();
			double acc = corrects / (cc * multi) * 100;
			
			if (corrects  < lastCorrects ){ //
				if (!alphaSetted){
					this.alpha *= 0.1;
					if (alpha < minAlpha)
						alpha = minAlpha;
				}
				if (!lambdaSetted){
					this.lambda *= 0.1;
					if (lambda < minLambda)
						lambda = minLambda;
				}
				if (alphaSetted && !lambdaSetted && lambda < alpha * (minLambda/minAlpha)){
					lambda = alpha * (minLambda/minAlpha);
				}else if (!alphaSetted && lambdaSetted && alpha < lambda * (minAlpha / minLambda)){
					alpha = lambda * (minAlpha / minLambda);
				}
			}
			System.out.println(String.format("#%d loop%d\ttime:%d(ms)\tacc: %.3f(approx)\tavg_error:%.6f", cc, l, (timeEnd - timeStart), acc , avge));
		}
		
	}



	public void saveModel(String filePath) throws Exception {		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
		bw.write(this.dataEntry.getDataSetInfo().get(Constants.DATASET_INFO) + "\n");
		for(int i = 1 ; i < this.featureWeights.length; i++){
			if (this.featureWeights[i] == initWeight)
				bw.write("0.0\n");
			else
				bw.write(String.format("%.6f\n", this.featureWeights[i]));
		}
		bw.close();
	}

	@Override
	public void loadModel(String modelPath) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(modelPath));
		
		ArrayList<String> modelList = new ArrayList<String>();
		for(String line = br.readLine(); line != null; line = br.readLine()){
			modelList.add(line);
		}
		br.close();
		this.featureWeights = new double[modelList.size() + 1];
		
		this._loadDataInfo(modelList.get(0));
		
		for(int i = 1; i < modelList.size(); i++){
			this.featureWeights[i] = Double.parseDouble(modelList.get(i));
		}
	}

	private void _loadDataInfo(String infoString){
		String[] ll = infoString.split("\\s+");
		String[] classInfo1 = ll[0].split(":");
		String[] classInfo2 = ll[1].split(":");
		
		int[] classInfo1Ints = new int[]{Integer.parseInt(classInfo1[0]), Integer.parseInt(classInfo1[1]), Integer.parseInt(classInfo1[2])};
		int[] classInfo2Ints = new int[]{Integer.parseInt(classInfo2[0]), Integer.parseInt(classInfo2[1]), Integer.parseInt(classInfo2[2])};
				
		this.labels = new int[2]; // 2 classes  index -> orginal_LABEL
		this.labels[classInfo1Ints[1]] = classInfo1Ints[0];
		this.labels[classInfo2Ints[1]] = classInfo2Ints[0];
		
		this.dataInfo = new int[LABELRANGEBASE * 2][]; // original_LABEL -> index, #sample
		this.dataInfo[LABELRANGEBASE + classInfo1Ints[0]] = new int[]{classInfo1Ints[1], classInfo1Ints[2]};
		this.dataInfo[LABELRANGEBASE + classInfo2Ints[0]] = new int[]{classInfo2Ints[1], classInfo2Ints[2]};

		//  set bias automatically
		float ratio = classInfo2Ints[2] /(classInfo1Ints[2] + 0.0f);
		this.biasLabel = classInfo1Ints[1];
		this.minLabel  = classInfo1Ints[1];

		if (classInfo1Ints[2] > classInfo2Ints[2]){ // #(label 0) >  #(label 1)
			this.biasLabel = classInfo2Ints[1];
			ratio = classInfo1Ints[2] /(classInfo2Ints[2] + 0.0f);
			this.minLabel =  classInfo2Ints[1];
		}else{ //default
			;
		}
		this.biasWeightRound = Math.round(ratio);
		this.samples = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.NUMBERS_SAMPLE);

		double rate = Math.log(2 + samples /((1+biasWeightRound)/(biasWeightRound*2.0)) /( this.maxFeatureId + 0.0));
		if (rate < 0.5)
			rate = 0.5;

		if (alpha == null){
			alpha = 1.0 / rate;
			minAlpha = alpha * 0.1 / Math.pow(1+rate, 1.8);
		}
		if (this.lambda == null){
			lambda = 0.00005 / rate;
			minLambda = lambda * 0.1 / Math.pow(1+rate, 1.8);
		}
		if (this.stop == null){
			stop = 0.001;
		}
		
	}
	
	@Override
	public void crossValidation(int fold, Evaluator... evaluators) throws Exception {
		
		for(int i = 0 ; i < fold; i++){
			this.init();
			System.out.println("----cross validation loop " + i);
			this._train(fold, i);
			//-------------test-------
			this.dataEntry.reOpenData();
			int c = 1;
			double[] resultProbs = new double[2];
			for(Sample sample = dataEntry.next(); sample != null; sample = dataEntry.next(), c+=1){
				if (c % fold == i){
					this.predict(sample, resultProbs);
					for(Evaluator e : evaluators){
						e.collect(sample.label, resultProbs);
					}
				}
			}
		}
	}

	@Override
	public void setProperties(Properties prop) {
		if (prop.getProperty(Constants.LOOPS) != null){
			loops = Utilities.getIntFromProperties(prop, Constants.LOOPS);
		}
		if (prop.getProperty(Constants.ALPHA) != null){
			alpha = Utilities.getDoubleFromProperties(prop, Constants.ALPHA);
			alphaSetted = true;
		}
		if (prop.getProperty(Constants.LAMBDA) != null){
			this.lambda = Utilities.getDoubleFromProperties(prop, Constants.LAMBDA);
			lambdaSetted = true;
		}
		if (prop.getProperty(Constants.STOPCRITERIA) != null){
			this.stop = Utilities.getDoubleFromProperties(prop, Constants.STOPCRITERIA);
			stopSetted = true;
		}
		
		for(Entry<Object, Object> entry : prop.entrySet()){
			String  key= entry.getKey().toString();
			if (key.startsWith(Constants.BIAS_LABEL_ORIGINAL)){ // bias original label
				biasLabel = dataInfo[Integer.parseInt(key.substring(3)) + LABELRANGEBASE][1];
				biasWeightRound = Integer.parseInt(entry.getValue().toString());
			}else if (key.startsWith(Constants.BIAS_LABEL_TRANSFORMED)){ //bias transformed label
				biasLabel = Integer.parseInt(key.substring(3));
				biasWeightRound = Integer.parseInt(entry.getValue().toString());
			}
		}
	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void predict(Sample  sample, double[] probabilities) throws Exception{
		double weigtSum = 0 ; 
		for(int i = 0 ; i < sample.featureSize; i++){
			weigtSum += this.featureWeights[sample.features[i]] * sample.weights[i];
		}
		double probability = 1/(1+Math.pow(Math.E, -weigtSum));
		probabilities[0] =  1- probability;
		probabilities[1] =  probability;
	}

	/**
	 * you should make sure the LABEL of data is the same as that of training set. 
	 * Otherwise use {@link #predict(String, String, Evaluator...)} instead.
	 */
	@Override
	public void predict(DataStorage data, String resultPath, Evaluator... evaluators) throws Exception {
		if (this.featureWeights == null)
			throw new IOException("!Model haven't been initialized yet! :(");
		BufferedWriter bw = new BufferedWriter(new FileWriter(resultPath));
		double[] resultProbs = new double[2];
		int idx = -1;
		for(Sample current = data.next(); current != null; current = data.next()){
			this.predict(current, resultProbs);
			for(Evaluator e : evaluators){
				e.collect(current.label, resultProbs);
			}
			idx = resultProbs[0] > resultProbs[1] ? 0 : 1;
			bw.write(String.format("%d\t%.4f\n", labels[idx], resultProbs[idx]));
		}
		bw.close();
		for(Evaluator e : evaluators){
			System.out.println(e.resultString());
		}
	}
	

	
	@Override
	public void predict(String predictFile, String resultPath, Evaluator... evaluators) throws Exception {
		if (this.featureWeights == null)
			throw new IOException("!Model haven't been initialized yet! :(");
		BufferedWriter bw = new BufferedWriter(new FileWriter(resultPath));
		BufferedReader br = new BufferedReader(new FileReader(predictFile));
		Sample current = new Sample(this.featureWeights.length);
		double[] resultProbs = new double[2];
		int idx = -1;
		for(String line = br.readLine(); line != null; line = br.readLine()){
			String[] info = line.split("\\s+");
			try{
				int label = Integer.parseInt(info[0].startsWith("+") ? info[0].substring(1):info[0]);
				int[] dataInfoForThisLabel = this.dataInfo[LABELRANGEBASE + label] ;
				if (dataInfoForThisLabel != null){ // with label
					for(int i = 1 ; i < info.length; i++){
						String[] fidAndWeight = info[i].split(":");
						current.features[i] =   Integer.parseInt(fidAndWeight[0]);
						current.weights[i]  = Double.parseDouble(fidAndWeight[1]);
						current.featureSize = info.length;
						current.label = dataInfoForThisLabel[0];
					}
				}else{
					br.close();
					bw.close();
					throw new RuntimeException("Label not in training set!");
				}
				this.predict(current, resultProbs);
				for(Evaluator e : evaluators){
					e.collect(current.label, resultProbs);
				}
				idx = resultProbs[0] > resultProbs[1] ? 0 : 1;
				bw.write(String.format("%d\t%d\t%.4f\n", labels[current.label], labels[idx], resultProbs[idx]));
			}catch(NumberFormatException e){
				br.close();
				bw.close();
				throw new NumberFormatException("Data Format Error : first column must be a number of label");
			}


		}
		br.close();
		bw.close();
		for(Evaluator e : evaluators){
			System.out.println(e.resultString());
		}
		

	}

	public String toString(){
		return String.format("alpha:%.6f, lambda:%.9f, loops: %d, bias:%d on %d times", 
				this.alpha, this.lambda, this.loops, biasLabel, biasWeightRound);
	}
}
