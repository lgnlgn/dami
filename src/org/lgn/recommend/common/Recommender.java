package org.lgn.recommend.common;

import java.util.List;

public interface Recommender {
	public double[] online(List<Pair<Integer, Float>> items);
	
	public double[] offline(int userId) throws Exception;
}
