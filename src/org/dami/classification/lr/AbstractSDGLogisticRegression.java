package org.dami.classification.lr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Map.Entry;

import org.dami.classification.common.Classifier;
import org.dami.classification.common.Evaluator;
import org.dami.common.Constants;
import org.dami.common.MemoryEstimater;
import org.dami.common.Utilities;
import org.dami.common.Vector;
import org.dami.common.VectorPool;
import org.dami.common.io.DataReader;
import org.dami.common.io.VectorStorage;

public abstract class AbstractSDGLogisticRegression implements Classifier, MemoryEstimater{
	final static double initWeight = 0;
	final static int LABELRANGEBASE = 32768;
	public final static double DEFAULT_STOP = 0.001;
	public final static int DEFAULT_LOOPS = 30;
	
	public double[] featureWeights = null;
	
	protected VectorStorage dataEntry= null;
	VectorPool pool = null;
	boolean usePool = false;
	
	protected int[][] dataInfo = null;
	
	
	protected Double alpha = null; // learning speed
	protected Double lambda = null;// regularization
	protected Double stop = null;
	protected boolean alphaSetted = false;
	protected boolean lambdaSetted = false;

	
	protected double minAlpha = 0.001;
	protected double minLambda = 0.01;
	
	protected Integer loops = DEFAULT_LOOPS;
	protected int fold = 5;
	
	protected int samples = 0; 
	protected int maxFeatureId = -1;
	 
	protected int biasLabel = 0; //  original label
	protected int biasWeightRound = 1;
	
	// for accuracy stop
	protected int minSamples = 0;  // #
	protected int maxSamples = 0;  // #

	@Override
	public void loadData(VectorStorage data) throws Exception {
		dataEntry = data;
		if (this.dataEntry.getDataSetInfo() == null){
			throw new RuntimeException("dataEntry must be set!");
		}else{
			maxFeatureId = Utilities.getIntFromProperties(dataEntry.getDataSetInfo(), Constants.MAXFEATUREID);
					
			String tmpInfo = Utilities.getStrFromProperties(dataEntry.getDataSetInfo(),Constants.DATASET_INFO);
			this.dataInfo = new int[LABELRANGEBASE * 2][];
			if (Utilities.getIntFromProperties(dataEntry.getDataSetInfo(),Constants.NUMBER_CLASSES) > 2){
				dataEntry.close();
				throw new RuntimeException("Data Set contains more than 2 classes");
			}
			_loadDataInfo(tmpInfo);
		}
	}
	
	private void _loadDataInfo(String infoString){
		String[] ll = infoString.split("\\s+");
		String[] classInfo1 = ll[0].split(":"); // orginal_label:converted_label:#num
		String[] classInfo2 = ll[1].split(":");
		
		int[] classInfo1Ints = new int[]{Integer.parseInt(classInfo1[0]), Integer.parseInt(classInfo1[1]), Integer.parseInt(classInfo1[2])};
		int[] classInfo2Ints = new int[]{Integer.parseInt(classInfo2[0]), Integer.parseInt(classInfo2[1]), Integer.parseInt(classInfo2[2])};
		
		this.dataInfo = new int[LABELRANGEBASE * 2][]; // original_LABEL -> index, #sample
		this.dataInfo[LABELRANGEBASE + classInfo1Ints[0]] = new int[]{classInfo1Ints[1], classInfo1Ints[2]};
		this.dataInfo[LABELRANGEBASE + classInfo2Ints[0]] = new int[]{classInfo2Ints[1], classInfo2Ints[2]};

		//  set bias automatically
		float ratio = classInfo2Ints[2] /(classInfo1Ints[2] + 0.0f);
		this.biasLabel = classInfo1Ints[0];
		this.minSamples  = classInfo1Ints[2];
		this.maxSamples  = classInfo2Ints[2];
		if (classInfo1Ints[2] > classInfo2Ints[2]){ // #(label 0) >  #(label 1)
			this.biasLabel = classInfo2Ints[0];
			ratio = classInfo1Ints[2] /(classInfo2Ints[2] + 0.0f);
			this.minSamples  = classInfo2Ints[2];
			this.maxSamples  = classInfo1Ints[2];
		}else{ //default
			;
		}
		this.biasWeightRound = Math.round(ratio);
		
		try{
			this.estimateParameter();
			if (this.stop == null){
				stop = DEFAULT_STOP;
			}
		}catch(NullPointerException e){
			// loading model
			System.out.println( " a model loading~");
		}
		
	}
	
	abstract protected void estimateParameter() throws NullPointerException;
	
	@Override
	public void train() throws Exception {
		this.init();
		this._train(Integer.MAX_VALUE, 0); // all samples for training
		this.dataEntry.close();
	}
	
	protected void init(){
		featureWeights = new double[maxFeatureId + 1];
		Arrays.fill(featureWeights, initWeight);
		if (dataEntry instanceof VectorStorage.FileStorage)
			pool = new VectorPool(dataEntry);

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
			Vector sample = new Vector();
			for(dataEntry.next(sample); sample.featureSize >= 0; dataEntry.next(sample)){
				if (c % fold == i){
					if (sample.featureSize == 0)
						continue;
					this.predict(sample, resultProbs);
					for(Evaluator e : evaluators){
						e.collect(dataInfo[LABELRANGEBASE + sample.label][0], resultProbs);
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
			stop = Utilities.getDoubleFromProperties(prop, Constants.STOPCRITERIA);
		}
		
		
		for(Entry<Object, Object> entry : prop.entrySet()){
			String  key= entry.getKey().toString();
			if (key.startsWith(Constants.BIAS_LABEL_ARG)){
				biasLabel = Integer.parseInt(key.substring(2));
				biasWeightRound = Integer.parseInt(entry.getValue().toString());
			}
		}
	}

	public void saveModel(String filePath) throws Exception {		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
		bw.write(this.dataEntry.getDataSetInfo().get(Constants.MAXFEATUREID) + Constants.ENDL);
		bw.write(this.dataEntry.getDataSetInfo().get(Constants.DATASET_INFO) + Constants.ENDL);
		for(int i = 0 ; i < this.featureWeights.length; i++){
			if (this.featureWeights[i] == initWeight)
				bw.write("0" + Constants.ENDL);
			else
				bw.write(String.format("%.6f" + Constants.ENDL, this.featureWeights[i]));
		}
		bw.close();
	}

	public void loadModel(String modelPath) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(modelPath));

		String line = br.readLine();
		this.featureWeights = new double[Integer.parseInt(line) + 1];
		line = br.readLine();
		this._loadDataInfo(line);
		int i = 0 ;
		for(line = br.readLine(); line != null; line = br.readLine()){
			this.featureWeights[i] = Double.parseDouble(line);
			i += 1 ;
		}
		br.close();

	}
	
	
	@Override
	public void predict(Vector sample, double[] probabilities) throws Exception{
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
	public void predict(DataReader<Vector> data, String resultPath, Evaluator... evaluators) throws Exception {
		if (this.featureWeights == null)
			throw new IOException("!Model haven't been initialized yet! :(");
		BufferedWriter bw = new BufferedWriter(new FileWriter(resultPath));
		double[] resultProbs = new double[2];
		int idx = -1;
		Vector sample = new Vector();
		for(data.next(sample); sample.featureSize >= 0; data.next(sample)){
			if (sample.featureSize == 0)
				continue;
			this.predict(sample, resultProbs);
			for(Evaluator e : evaluators){
				int tmp = dataInfo[LABELRANGEBASE + sample.label][0];
				e.collect(tmp, resultProbs);
			}
			idx = resultProbs[0] > resultProbs[1] ? 0 : 1;
			bw.write(String.format("%d\t%.4f" + Constants.ENDL, sample.label, resultProbs[idx]));
		}
		bw.close();
		for(Evaluator e : evaluators){
			System.out.println(e.resultString());
		}
	}
	
	public String toString(){
		return String.format("alpha:%.6f, lambda:%.9f, loops: %d, bias:%d on %d times", 
				this.alpha, this.lambda, this.loops, biasLabel, biasWeightRound);
	}

	@Override
	public Properties getProperties() {
		Properties p = new Properties();
		p.put(Constants.ALPHA, this.alpha);
		p.put(Constants.LOOPS, this.loops);
		p.put(Constants.LAMBDA, this.lambda);	
		return p;
	}
	
	protected abstract void _train(int fold, int remain) throws IOException;
	
	public abstract int estimate(Properties dataStatus, Properties parameters) ;
}
