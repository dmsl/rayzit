package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

public interface IMetricSpace {
	public Object readObject(String line, int dim);
	public void setMetric(IMetric metric);
	public float compDist(Object o1, Object o2) throws IOException ;
	public String outputObject(Object o);
	public int getID(Object o);
	public String outputDim(Object o);
}