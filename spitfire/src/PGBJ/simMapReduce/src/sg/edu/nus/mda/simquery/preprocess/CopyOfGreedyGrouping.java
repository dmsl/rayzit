package sg.edu.nus.mda.simquery.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;

/**
 * 
 * @author luwei
 * 
 */
public class CopyOfGreedyGrouping {
	private Vector<Object> pivots;
	private Vector<Integer> numOfObjectsInPivot;
	private Vector<Short>[] pivotsInGroup;
	private int[] numOfObjectsInGroup;
	private float[] minRadius;
	private float[] maxRadius;
	private Vector<Float>[] KNNObjects;
	private int numOfPivots;
	private int numOfGroups;
	private int K;
	private IMetricSpace metricSpace = null;
	private IMetric metric = null;
	Configuration conf;
	private int dim;

	@SuppressWarnings("unchecked")
	public CopyOfGreedyGrouping(Configuration conf) throws IOException {
		this.conf = conf;
		dim = conf.getInt(SQConfig.strDimExpression, 10);
		K = conf.getInt(SQConfig.strK, 10);
		readMetricAndMetricSpace(conf);
		readPivot(conf, conf.get(SQConfig.strPivotInput));
		readIndex(conf, conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
				+ "summary" + SQConfig.strIndexExpression2);
		// organize pivots into groups
		numOfGroups = conf.getInt(SQConfig.strNumOfGroups, 1);
		numOfPivots = pivots.size();
		// set the pivots for each group
		pivotsInGroup = new Vector[numOfGroups];
		// set the number of objects for each group
		numOfObjectsInGroup = new int[numOfGroups];
	}

	public void doGouping() throws IOException {
		if (numOfGroups > numOfPivots) {
			throw new IOException(
					"The number of Groups cannot exceed the number of pivots");
		}


		float[] ubKNNForPartsOfR = compKNNForPartsOfR();
		@SuppressWarnings({ "unchecked" })
		Vector<Short>[] toBeShuffled = new Vector[numOfPivots];

		/** compute the shuffled partitions from S for each partition from R */
		compShuffledPartitionS(toBeShuffled, ubKNNForPartsOfR);

		/** initialize the first partition for each group */
		Vector<Short> restOfPivots = new Vector<Short>(numOfPivots);
		short[] initSetOfParts = initPartsForGroups(restOfPivots);
		short pid, gid, id;
		boolean[][] hasShuffled = new boolean[numOfGroups][numOfPivots];
		// init hasShuffled
		for (short i = 0; i < numOfGroups; i ++){
			for (short j = 0; j < numOfPivots; j++){
				hasShuffled[i][j] = false;
			}
		}

		for (short i = 0; i < numOfGroups; i++) {
			pivotsInGroup[i] = new Vector<Short>();
			/** add the pid to the group */
			pid = initSetOfParts[i];
			pivotsInGroup[i].add(pid);
			/** set size to the number of objects in the partition */
			numOfObjectsInGroup[i] = numOfObjectsInPivot.get(pid);
			// set the partitions from S that need to be shuffled
			int size = toBeShuffled[pid].size();
			for (short j = 0; j < size; j++) {
				hasShuffled[i][toBeShuffled[pid].get(j)] = true;
			}
		}

		// start the iteration
		while (restOfPivots.size() > 0) {
			// select
			gid = selMinSizeOfGroup(numOfObjectsInGroup);
			id = findPivotWithMinimumIncObj(hasShuffled[gid], toBeShuffled,
					restOfPivots);
			pid = restOfPivots.get(id);
			// update
			restOfPivots.remove(id);
			pivotsInGroup[gid].add(pid);
			numOfObjectsInGroup[gid] += numOfObjectsInPivot.get(pid);
			// update hasShuffled[gid]
			int size = toBeShuffled[pid].size();
			for (short j = 0; j < size; j++) {
				hasShuffled[gid][toBeShuffled[pid].get(j)] = true;
			}
		}

	}

	private short findPivotWithMinimumIncObj(
			boolean[] hasShuffledForGroup,
			Vector<Short>[] toBeShuffled, Vector<Short> restOfPivots) {
		short id = -1;
		short pid, tmpPid;
		int size = restOfPivots.size(), tmpSize;
		short i, j;
		int minIncObj = Integer.MAX_VALUE, tmpMinIncObj;

		for (i = 0; i < size; i++) {
			pid = restOfPivots.get(i);
			tmpSize = toBeShuffled[pid].size();
			tmpMinIncObj = 0;

			for (j = 0; j < tmpSize; j++) {
				tmpPid = toBeShuffled[pid].get(j);
				if (!hasShuffledForGroup[tmpPid]) {
					tmpMinIncObj += numOfObjectsInPivot.get(tmpPid);
				}
			}
			if (tmpMinIncObj < minIncObj) {
				id = i;
				minIncObj = tmpMinIncObj;
			}
		}

		return id;
	}

	private short selMinSizeOfGroup(int[] sizeOfGroups) throws IOException {
		if (sizeOfGroups == null || sizeOfGroups.length == 0)
			throw new IOException("the input array cannot be null or empty!");

		short id = 0;
		int minSize = Integer.MAX_VALUE;

		for (short i = 0; i < sizeOfGroups.length; i++) {
			if (sizeOfGroups[i] < minSize) {
				minSize = sizeOfGroups[i];
				id = i;
			}
		}

		return id;
	}

	private short[] initPartsForGroups(Vector<Short> pids) throws IOException {
		short i, j, id;
		float dist, sumOfDist;
		short[] initSetOfParts = new short[numOfGroups];
		Vector<Float> distToSelPivots = new Vector<Float>(numOfPivots);

		for (i = 0; i < numOfPivots; i++)
			pids.add(i);

		/** select the first pid */
		id = selectFirstPId();
		initSetOfParts[0] = pids.get(id);
		/** compute the distance of pivots to pid */
		for (i = 0; i < numOfPivots; i++) {
			dist = metric.dist(pivots.get(i), pivots.get(id));
			distToSelPivots.add(dist);
		}
		/** remove the id */
		pids.remove(id);
		distToSelPivots.remove(id);
		for (i = 1; i < numOfGroups; i++) {
			id = selectNextPId(distToSelPivots);
			initSetOfParts[i] = pids.get(id);
			/** remove the id */
			pids.remove(id);
			distToSelPivots.remove(id);
			// update the distance of the unselected pivots
			for (j = 0; j < pids.size() && i < numOfGroups - 1; j++) {
				sumOfDist = distToSelPivots.get(j);
				dist = metric.dist(pivots.get(initSetOfParts[i]),
						pivots.get(pids.get(j)));
				distToSelPivots.set(j, sumOfDist + dist);
			}
		}

		return initSetOfParts;
	}

	/**
	 * select the pivots that are furthest to the rest of pivots
	 * 
	 * @return
	 * @throws IOException
	 */
	private short selectFirstPId() throws IOException {
		short maxSumOfPID = 0;
		float sumOfDist, maxSumOfDist = 0;
		float dist;

		for (short i = 0; i < numOfPivots; i++) {
			sumOfDist = 0.0f;
			for (int j = 0; j < numOfPivots; j++) {
				if (i == j)
					dist = 0.0f;
				else
					dist = metric.dist(pivots.get(i), pivots.get(j));

				sumOfDist += dist;
			}
			if (sumOfDist > maxSumOfDist) {
				maxSumOfPID = i;
				maxSumOfDist = sumOfDist;
			}
		}

		return maxSumOfPID;
	}

	/**
	 * select the pivot with the furthest distance to the selected pivots
	 * 
	 * @param sumOfDist
	 * @return the id (not the pid) in pids that indicates pids.get(id) with the
	 *         furthest distance
	 * @throws IOException
	 */
	private short selectNextPId(Vector<Float> distToSelPivots)
			throws IOException {
		short maxOfID = 0;
		float dist, maxDist = 0;

		assert (distToSelPivots != null);
		for (short i = 0; i < distToSelPivots.size(); i++) {
			dist = distToSelPivots.get(i);
			if (maxDist < dist) {
				maxDist = dist;
				maxOfID = i;
			}
		}

		return maxOfID;
	}

	private void compShuffledPartitionS(Vector<Short>[] toBeShuffled,
			float[] ubKNNForPartsOfR) throws IOException {
		short i, j;
		float distOfPiToPj;
		float lb;

		for (i = 0; i < numOfPivots; i++) {
			toBeShuffled[i] = new Vector<Short>();
			for (j = 0; j < numOfPivots; j++) {
				distOfPiToPj = metric.dist(pivots.get(i), pivots.get(j));
				lb = distOfPiToPj - maxRadius[i] - ubKNNForPartsOfR[i];
				if (lb < maxRadius[j]) {
					toBeShuffled[i].add(j);
				}
			}
		}
	}

	/**
	 * Used to sort the priority queue by the descending order of float value
	 * 
	 * @author luwei
	 * 
	 * @param <T>
	 */
	public static class ASCPQ<T> implements Comparator<T> {
		public int compare(Object o1, Object o2) {
			float v1 = (Float) o1;
			float v2 = (Float) o2;

			if (v1 > v2)
				return -1;
			else if (v1 == v2)
				return 0;
			else
				return 1;
		}
	}

	/**
	 * compute the upper bound distance of KNN for each partition of R
	 * 
	 * @return
	 * @throws IOException
	 */
	private float[] compKNNForPartsOfR() throws IOException {
		float[] ubKNN = new float[numOfPivots];
		int i, j;
		float distOfPiToPj;

		for (i = 0; i < numOfPivots; i++) {
			PriorityQueue<Float> knn = new PriorityQueue<Float>(K,
					new ASCPQ<Float>());
			for (j = 0; j < numOfPivots; j++) {
				if (i == j)
					distOfPiToPj = 0;
				else
					distOfPiToPj = metric.dist(pivots.get(i), pivots.get(j));

				// refine the ub(s, P^R_i)
				for (float d : KNNObjects[j]) {
					float ub_dist = maxRadius[i] + distOfPiToPj + d;
					if (knn.size() < K) {
						knn.add(ub_dist);
					} else if (ub_dist < knn.peek()) {
						knn.remove();
						knn.add(ub_dist);
					} else {
						break;
					}
				}
			}
			ubKNN[i] = knn.peek();
			knn.clear();
		}

		return ubKNN;
	}

	public void writeToHDFS() throws IOException {
		FileSystem fs = FileSystem.get(conf);
		FSDataOutputStream currentStream;
		String output = conf.get(SQConfig.strGroupOutput);
		currentStream = fs.create(new Path(output), true);
		int i, j;

		for (i = 0; i < numOfGroups; i++) {
			int pid = pivotsInGroup[i].get(0);
			currentStream.writeBytes(Integer.toString(pid));
			for (j = 1; j < pivotsInGroup[i].size(); j++) {
				pid = pivotsInGroup[i].get(j);
				currentStream.writeBytes(" " + pid);
			}
			currentStream.writeBytes("\n");
		}
		currentStream.close();

		System.out.println("output the group to path " + output);
		printGroupSummary();
	}

	private void printGroupSummary() {
		int i;
		double avg, dev = 0;
		int min = Integer.MAX_VALUE, max = 0, totNumOfObjects = 0;

		for (i = 0; i < numOfGroups; i++) {
			//System.out.println(numOfObjectsInGroup[i]);
			totNumOfObjects += numOfObjectsInGroup[i];
			if (min > numOfObjectsInGroup[i]) {
				min = numOfObjectsInGroup[i];
			}
			if (max < numOfObjectsInGroup[i]) {
				max = numOfObjectsInGroup[i];
			}
		}

		avg = 1.0d * totNumOfObjects / numOfGroups;

		for (i = 0; i < numOfGroups; i++) {
			dev += (numOfObjectsInGroup[i] - avg)
					* (numOfObjectsInGroup[i] - avg);
		}

		dev = Math.sqrt(1.0f * dev / numOfGroups);

		System.out.println("======summary of groups");
		System.out.println("min: " + min);
		System.out.println("max: " + max);
		System.out.println("avg: " + avg);
		System.out.println("dev: " + dev);
	}

	/**
	 * get MetricSpace and metric from configuration
	 * 
	 * @param conf
	 * @throws IOException
	 */
	private void readMetricAndMetricSpace(Configuration conf)
			throws IOException {
		try {
			metricSpace = MetricSpaceUtility.getMetricSpace(conf
					.get(SQConfig.strMetricSpace));
			metric = MetricSpaceUtility.getMetric(conf.get(SQConfig.strMetric));
			metricSpace.setMetric(metric);
		} catch (InstantiationException e) {
			throw new IOException("InstantiationException");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new IOException("IllegalAccessException");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException("ClassNotFoundException");
		}
	}

	/**
	 * read pivots from input
	 * 
	 * @param conf
	 */
	private void readPivot(Configuration conf, String pivotInput)
			throws IOException {
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream currentStream;
		BufferedReader currentReader;
		currentStream = fs.open(new Path(pivotInput));
		currentReader = new BufferedReader(new InputStreamReader(currentStream));
		String line;
		pivots = new Vector<Object>();
		while ((line = currentReader.readLine()) != null) {
			pivots.add(metricSpace.readObject(line, dim));
		}
		currentReader.close();
	}

	/**
	 * read pivots from input
	 * 
	 * @param conf
	 */
	@SuppressWarnings("unchecked")
	private void readIndex(Configuration conf, String indexInput)
			throws IOException {
		String sepStrForIndex = SQConfig.sepStrForIndex;
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream currentStream;
		BufferedReader currentReader;
		currentStream = fs.open(new Path(indexInput));
		currentReader = new BufferedReader(new InputStreamReader(currentStream));
		String line;
		String[] info;
		numOfObjectsInPivot = new Vector<Integer>(pivots.size());
		minRadius = new float[pivots.size()];
		maxRadius = new float[pivots.size()];
		KNNObjects = new Vector[pivots.size()];
		int i = 0, j;

		while ((line = currentReader.readLine()) != null) {
			info = line.split(sepStrForIndex);
			/**
			 * 0: f_id, 1: pid; 2: min_radius; 3: max_radius; 4: size; 5:num; 6
			 * <dist>
			 * */
			minRadius[i] = Float.valueOf(info[2]);
			maxRadius[i] = Float.valueOf(info[3]);
			numOfObjectsInPivot.add(Integer.valueOf(info[5]));
			KNNObjects[i] = new Vector<Float>();
			for (j = info.length - 1; j >= 6; j--) {
				KNNObjects[i].add(Float.valueOf(info[j]));
			}
			i++;
		}
		currentReader.close();
	}

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		Configuration conf = new Configuration();
		new GenericOptionsParser(conf, args).getRemainingArgs();
		try {
			CopyOfGreedyGrouping grouping = new CopyOfGreedyGrouping(conf);
			grouping.doGouping();
			grouping.writeToHDFS();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println("MergeSummary takes " + second + " seconds");
	}

}
