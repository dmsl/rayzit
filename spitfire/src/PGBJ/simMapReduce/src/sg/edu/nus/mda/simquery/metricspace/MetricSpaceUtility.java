package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

import sg.edu.nus.mda.simquery.metricspace.MetricFactory.L1Metric;
import sg.edu.nus.mda.simquery.metricspace.MetricFactory.L2Metric;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceFactory.VectorSpace;

public class MetricSpaceUtility {
	public static IMetricSpace getMetricSpace(String strMetricSpace)
			throws ClassNotFoundException, IOException, InstantiationException,
			IllegalAccessException {
		Class<? extends IMetricSpace> cMetricSpace = VectorSpace.class;
		if (0 == strMetricSpace.compareToIgnoreCase("vector")) {
			cMetricSpace = VectorSpace.class;
		} else {
			throw new IOException("MetricSpace " + strMetricSpace
					+ " is not found");
		}
		return cMetricSpace.newInstance();
	}

	public static IMetric getMetric(String strMetric)
			throws ClassNotFoundException, IOException, InstantiationException,
			IllegalAccessException {
		Class<? extends IMetric> cMetric = IMetric.class;
		if (0 == strMetric.compareToIgnoreCase("L1Metric")) {
			cMetric = L1Metric.class;
		} else if (0 == strMetric.compareToIgnoreCase("L2Metric")) {
			cMetric = L2Metric.class;
		} else {
			throw new IOException("Metric " + strMetric + " is not found");
		}
		
		return cMetric.newInstance();
	}
}
