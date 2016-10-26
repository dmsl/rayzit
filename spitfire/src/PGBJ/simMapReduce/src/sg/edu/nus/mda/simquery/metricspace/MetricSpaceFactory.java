package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

public class MetricSpaceFactory {
	public static class VectorSpace implements IMetricSpace {
		private static final String regex = ",";
		private IMetric metric;

		public VectorSpace() {

		}

		public VectorSpace(IMetric metric) {
			this.metric = metric;
		}

		public void setMetric(IMetric metric) {
			this.metric = metric;
		}

		public int getID(Object o) {
			Record r = (Record) o;

			return r.getRId();
		}
		
		@Override
		/**
		 * parse a vector object from a given line
		 */
		public Object readObject(String line, int dim) {
			if (line == null || line == "") {
				return null;
			}

			/**
			 * format of the line: rid,dim1,dim2,...,dimN,desc
			 */
			String[] strVector = line.split(regex);
			int rid = Integer.valueOf(strVector[0]);
			float[] dVector = new float[dim];
			for (int i = 0; i < dim; i++) {
				dVector[i] = Float.valueOf(strVector[i + 1]);
			}
			String desc = strVector[strVector.length - 1];

			return new Record(rid, dVector, desc);
		}

		@Override
		public float compDist(Object o1, Object o2) throws IOException {
			return metric.dist(o1, o2);
		}

		@Override
		public String outputObject(Object o) {
			Record r = (Record) o;

			return r.toString();
		}

		@Override
		public String outputDim(Object o) {
			Record r = (Record) o;

			return r.dimToString();
		}

	}

}
