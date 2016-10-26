package sg.edu.nus.mda.simquery.knnjoin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

@SuppressWarnings("rawtypes")
public class ShuffleKey implements WritableComparable{
	/** from which file */
	public int pid;

	/** bound for KNN join */
	float bound;

	public ShuffleKey() {
		this(-1, -1);
	}

	public ShuffleKey(int pid, float ub) {
		super();
		this.pid = pid;
		bound = ub;
	}

	public void set(int pid, float ub) {
		this.pid = pid;
		bound = ub;
	}

	public void readFields(DataInput in) throws IOException {
		pid = in.readInt();
		bound = in.readFloat();
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(pid);
		out.writeFloat(bound);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ShuffleKey))
			return false;

		ShuffleKey other = (ShuffleKey) obj;
		return (this.pid == other.pid);
	}

	@Override
	public int hashCode() {
		return pid;
	}

	public String toString() {
		return Integer.toString(pid) + "," 
				+ Float.toString(bound);
	}

	@Override
	public int compareTo(Object obj) {
		ShuffleKey other = (ShuffleKey) obj;

		return (this.pid - other.pid);
//		if (this.pid > other.pid)
//			return 1;
//		else if (this.pid < other.pid)
//			return -1;
//
//		if (this.bound > other.bound)
//			return 1;
//		else if (this.bound < other.bound)
//			return -1;
//		else
//			return 0;
	}
}
