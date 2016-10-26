package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

public abstract class IMetric {
	public long numOfDistComp = 0;
	public abstract float dist(Object o1, Object o2)  throws IOException ;
	public abstract long getNumOfDistComp();
}
