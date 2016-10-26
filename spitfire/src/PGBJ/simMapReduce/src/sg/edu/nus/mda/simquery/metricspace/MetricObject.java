package sg.edu.nus.mda.simquery.metricspace;

import java.util.Collections;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class MetricObject implements Comparable {
	public int fid;
	public int pid;
	public float distToPivot;
	public Object obj;

	public MetricObject() {
	}

	public MetricObject(int fid, int pid, float dist, Object obj) {
		this.fid = fid;
		this.pid = pid;
		this.distToPivot = dist;
		this.obj = obj;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(); 
		Record r = (Record) obj;

		sb.append("fid:" + fid);
		sb.append(",pid: " + pid);
		sb.append(",distToPivot:" + distToPivot);
		sb.append("," + r.getRId());
		float[] value = r.getValue();

		for (float v : value) {
			sb.append("," + v);
		}

		return sb.toString();
	}
	
	/**
	 * sort by the descending order
	 */
	@Override
	public int compareTo(Object o) {
		MetricObject other = (MetricObject) o;
		if (other.distToPivot > this.distToPivot)
			return 1;
		else if (other.distToPivot < this.distToPivot)
			return -1;
		else
			return 0;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Vector<Integer> v = new Vector<Integer>();
		MetricObject o1 = new MetricObject(1, 1, 0.2f, (Object)v);
		MetricObject o2 = new MetricObject(1, 1, 1.2f, (Object)v);
		MetricObject o3 = new MetricObject(1, 1, 0.1f, (Object)v);
		Vector<MetricObject> list = new Vector<MetricObject>();
		
		list.add(o1);
		list.add(o2);
		list.add(o3);
		Collections.sort(list);
		
		System.out.println(list.get(0).distToPivot);
		System.out.println(list.get(1).distToPivot);
		System.out.println(list.get(2).distToPivot);
	}
	
}
