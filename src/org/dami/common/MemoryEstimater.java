package org.dami.common;

import java.util.Properties;

public interface MemoryEstimater {
	
	/**
	 * Estimate space for an algorithm
	 * Implemented by algorithms for estimation.
	 * @param dataStatus dataset properties
	 * @param parameters algorithms parameters;
	 * @return
	 */
	public int estimate(Properties dataStatus, Properties parameters);
}
