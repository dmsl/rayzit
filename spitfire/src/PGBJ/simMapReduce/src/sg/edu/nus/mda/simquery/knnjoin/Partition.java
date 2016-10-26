package sg.edu.nus.mda.simquery.knnjoin;

import java.util.Vector;

public class Partition {
	/**
	 * Info: (0) fid: (1) pid; (2) lower_r; (3) upper_r; (4) size of the
	 * partition; (5) numOfObjects; (6) variable: distance info of the first K
	 * objects in this partition
	 */
	/** from which file */
	int fid;
	/** pivot */
	Object pivot;
	/** lower bound and upper bound of distance from objects to the pivot */
	float min_r, max_r;
	/** size of the partition */
	int sizeOfPartition;
	/** number of objects in this partition */
	int numOfObjects;
	/** lower and upper bound for each region */
	Vector<Float> distOfFirstKObjects;

	public Partition() {
		this(null);
	}

	public Partition(Object obj) {
		this(obj, 0, 0);
	}

	public Partition(Object obj, float min_r, float max_r) {
		pivot = obj;
		this.min_r = min_r;
		this.max_r = max_r;
		distOfFirstKObjects = new Vector<Float>();
	}

	public void setFileID(int fid) {
		this.fid = fid;
	}

	public int getFileID() {
		return this.fid;
	}

	public void setPivot(Object obj) {
		pivot = obj;
	}

	public Object getPivot() {
		return pivot;
	}

	public void setNumOfObjects(int numOfObjects) {
		this.numOfObjects = numOfObjects;
	}

	public int getNumOfObjects() {
		return this.numOfObjects;
	}

	public void setMinMaxRadius(float min_r, float max_r) {
		this.min_r = min_r;
		this.max_r = max_r;
	}

	public float getMinRadius() {
		return min_r;
	}

	public float getMaxRadius() {
		return max_r;
	}

	public void setSizeOfPartition(int sizeOfPartition) {
		this.sizeOfPartition = sizeOfPartition;
	}

	public int getSizeOfPartition() {
		return this.sizeOfPartition;
	}

	public void setDistOfFirstKObjects(Vector<Float> distOfFirstKObjects) {
		this.distOfFirstKObjects = distOfFirstKObjects;
	}

	public Vector<Float> getDistOfFirstKObjects() {
		return distOfFirstKObjects;
	}

	public static int setPivots(Partition[] parts, Vector<Object> pivots,
			int start) {
		if (pivots == null || pivots.size() < 1)
			return 0;

		int numOfPivots = pivots.size();
		for (int i = 0; i < numOfPivots; i++) {
			parts[i].setPivot(pivots.get(i));
		}
		return numOfPivots;
	}
}
