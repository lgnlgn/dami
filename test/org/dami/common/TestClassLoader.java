package org.dami.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.dami.common.DataSerializer;

public class TestClassLoader {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		// TODO Auto-generated method stub
//		Class c= Class.forName("org.dami.common.DataSerializer$LabelFeatureWeightData");
//		Constructor<DataSerializer> cc = c.getConstructor(String.class);
//		DataSerializer ds = cc.newInstance("e:/data/mushrooms.txt");
//		System.out.println(ds.toString());
		String a = "aaaa aaaa bbbbb rrrr";
		String[] r = a.split("\\s+", 2);
		System.out.println(r[0]);
	}

}
