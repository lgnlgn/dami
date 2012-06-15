package org.dami.common;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class TestSerDe {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		// TODO Auto-generated method stub
		String input = "e:/data/real-sim";
		String out = "e:/data/covtype";
		DataSerializer ds = new DataSerializer.LabelFeatureWeightData(input, input);
		ds.transform();
		System.out.println(ds);
		
		Properties p = new Properties();
		p.load(new FileReader(out + ".sta"));
		Class c= Class.forName(p.getProperty(Constants.DATADESERIALIZER));
		Constructor<DataStorage> cc = c.getConstructor(String.class);
		DataStorage dde = cc.newInstance(out);
//		for(Sample s = dde.next(); s!= null; s = dde.next()){
//			System.out.println(s);
//		}
		dde.close();
	}

}
