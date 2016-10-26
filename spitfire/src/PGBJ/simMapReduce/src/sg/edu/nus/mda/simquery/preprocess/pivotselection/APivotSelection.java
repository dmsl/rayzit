package sg.edu.nus.mda.simquery.preprocess.pivotselection;

import java.io.IOException;

import sg.edu.nus.mda.simquery.metricspace.IMetric;

public abstract class APivotSelection {
	/**
	 * Duplicated are not allowed in the sampling.
	 * @param path: path of source file
	 * @param numOfObjects: sampling size
	 * @param totNumOfObjects: number of objects in the source file
	 * @param ss: selection strategy
	 * @return: -1: failed; 1: successful 
	 */
	public abstract int read(String path, int numOfObjects, int totNumOfObjects);
	public abstract void selectPivots(SelStrategy ss, IMetric metric)  throws IOException ; 
	public abstract void output(String path);
	
	public static enum SelStrategy {
		random,
		furthest,
		greedy
	}
}
