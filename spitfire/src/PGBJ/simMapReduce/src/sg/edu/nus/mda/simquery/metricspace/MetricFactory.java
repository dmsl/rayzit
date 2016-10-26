package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

public class MetricFactory {

	public static class L1Metric extends IMetric {
		
		/**
		 * L1 distance: the sum of |v1[i]-v2[i]| for any i 
		 */
		public float dist(Object o1, Object o2) throws IOException {
			numOfDistComp = numOfDistComp + 1;
			return Record.compL1Dist(o1, o2);
		}

		public long getNumOfDistComp() {
			return numOfDistComp;
		}
	}

	public static class L2Metric extends IMetric {

		
		/**
		 * L2 distance: the sqrt of sum (v1[i]-v2[i])*(v1[i]-v2[i]) for any i 
		 */
		public float dist(Object o1, Object o2) throws IOException {
			numOfDistComp = numOfDistComp + 1;
			return Record.compL2Dist(o1, o2);
		}
		
		public long getNumOfDistComp() {
			return numOfDistComp;
		}
	}

}
