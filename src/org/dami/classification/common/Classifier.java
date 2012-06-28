package org.dami.classification.common;

import java.util.Properties;

import org.dami.common.Vector;
import org.dami.common.io.DataReader;
import org.dami.common.io.VectorStorage;


public interface Classifier {

	public void loadData(VectorStorage data) throws Exception;
	
	public void train() throws Exception;
		
	public void setProperties(Properties prop);
	
	public Properties getProperties();
	
	public void predict(DataReader<Vector> data, String resultPath, Evaluator... evaluators) throws Exception;
	
	public void predict(Vector sample, double[] probabilities) throws Exception;
	
	public void saveModel(String filePath) throws Exception;
	
	public void loadModel(String modelPath) throws Exception;
	
	public void crossValidation(int fold, Evaluator... evaluators) throws Exception;
}
