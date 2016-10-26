package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

public class Record implements Comparable<Object> {
	private int rid;
	private float[] value;
	private String desc = "";

	public Record(int rid, float[] coord) {
		this(rid, coord, "");
	}

	public Record(int rid, float[] coord, String desc) {
		this.rid = rid;
		this.value = new float[coord.length];
		System.arraycopy(coord, 0, value, 0, coord.length);
		this.desc = desc;
	}

	public int getRId() {
		return rid;
	}

	public float[] getValue() {
		return value;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return this.desc;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(rid);
		for (int i = 0; i < value.length; i++) {
			sb.append("," + value[i]);
		}
		sb.append("," + desc);

		return sb.toString();
	}

	public String dimToString() {
		String strDim = Float.toString(value[0]);

		for (int i = 1; i < value.length; i++) {
			strDim += "," + Float.toString(value[i]);
		}

		return strDim;
	}
	
	public int compareTo(Object o) {
		Record other = (Record) o;
		if (other.rid > this.rid)
			return -1;
		else if (other.rid < this.rid)
			return 1;
		else
			return 0;
	}

	public static float compL1Dist(Object o1, Object o2) throws IOException {
		if (o1 instanceof Record && o2 instanceof Record) {
			Record r1 = (Record) o1;
			Record r2 = (Record) o2;
			float dist = 0f;
			float[] f1 = r1.getValue();
			float[] f2 = r2.getValue();

			for (int i = 0; i < f1.length; i++) {
				dist += Math.abs(f1[i] - f2[i]);
			}

			return dist;
		} else {
			throw new IOException(
					"The input objects must be the type of Record!");
		}

	}

	public static float compL2Dist(Object o1, Object o2) throws IOException {
		if (o1 instanceof Record && o2 instanceof Record) {
			Record r1 = (Record) o1;
			Record r2 = (Record) o2;
			float dist = 0f;
			float[] f1 = r1.getValue();
			float[] f2 = r2.getValue();

			for (int i = 0; i < f1.length; i++) {
				dist += (f1[i] - f2[i]) * (f1[i] - f2[i]);
			}

			return (float) Math.sqrt(dist);
		} else {
			throw new IOException(
					"The input objects must be the type of Record!");
		}
	}
}
