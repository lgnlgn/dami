package org.lgn.recommend.common;


import java.util.Properties;

public interface Model {
	public void loadData(DataSet data) throws Exception;
	
	public void train() throws Exception;
		
	public void setProperties(Properties prop);
	
	public Properties getProperties();
	
	public double online(int userId, int itemId) throws Exception;
	
	public void saveModel(String filePath) throws Exception;
	
	public void loadModel(String modelPath) throws Exception;
	
	public void crossValidation(int fold) throws Exception;
}
