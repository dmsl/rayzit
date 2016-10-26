package sg.edu.nus.mda.simquery.metricspace;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import sg.edu.nus.mda.simquery.util.Bytes;

@SuppressWarnings("rawtypes")
public class MetricKey implements WritableComparable {
	/** from which file */
	public int whichFile;
	/** Partition ID */
	public int pid;
	/** distance from objects to the pivot */
	public float dist;

	public MetricKey() {
		whichFile = 0;
		pid = 0;
		dist = 0.5f;
	}

	public int length() {
		return Bytes.SIZEOF_INT + Bytes.SIZEOF_INT + Bytes.SIZEOF_FLOAT;
	}

	public MetricKey(int pid, float dist, int which_file) {
		super();
		set(pid, dist, which_file);
	}

	public void set(int pid, float dist, int which_file) {
		this.pid = pid;
		this.dist = dist;
		this.whichFile = which_file;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		whichFile = in.readInt();
		pid = in.readInt();
		dist = in.readFloat();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(whichFile);
		out.writeInt(pid);
		out.writeFloat(dist);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MetricKey))
			return false;

		MetricKey other = (MetricKey) obj;
		return (this.whichFile == other.whichFile) && (this.pid == other.pid)
				&& (this.dist == other.dist);
	}

	@Override
	public int hashCode() {
		return pid;
	}

	public String toString() {
		return Integer.toString(whichFile) + "," + Integer.toString(pid) + ","
				+ Float.toString(dist);
	}

	@Override
	public int compareTo(Object obj) {
		MetricKey other = (MetricKey) obj;
		if (this.whichFile > other.whichFile)
			return 1;
		else if (this.whichFile < other.whichFile)
			return -1;

		if (this.pid > other.pid)
			return 1;
		else if (this.pid < other.pid)
			return -1;

		if (this.dist > other.dist)
			return 1;
		else if (this.dist < other.dist)
			return -1;
		else
			return 0;
	}
}
