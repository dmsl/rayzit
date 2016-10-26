package sg.edu.nus.mda.simquery.preprocess;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import sg.edu.nus.mda.simquery.knnjoin.Partition;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
//import sg.edu.nus.mda.simquery.grouping.Graph;
import sg.edu.nus.mda.simquery.preprocess.DataSplit.DataSplitMapper.ASCPQ;

public class IndexUtility {

	public IndexUtility() {
	}

	/**
	 * read pivots from a single file
	 * 
	 * @param pivotFile
	 * @return
	 * @throws IOException
	 */
	public Vector<Object> readPivotFromFile(String pivotFile,
			IMetricSpace metricSpace, int dim) throws IOException {
		Vector<Object> pivots = new Vector<Object>();
		BufferedReader fis = null;
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));

		FileSystem fileSystem = FileSystem.get(conf);

		try {
			InputStreamReader isr = new InputStreamReader(
					fileSystem.open(new Path(pivotFile)));
			fis = new BufferedReader(isr);
			String line;
			while ((line = fis.readLine()) != null) {
				pivots.add(metricSpace.readObject(line, dim));
			}
			return pivots;
		} catch (IOException ioe) {
			System.err
					.println("Caught exception while parsing the cached file '"
							+ pivotFile + "'");
			return null;
		} finally {

			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * read pivots from a single file
	 * 
	 * @param pivotFile
	 * @return
	 * @throws IOException
	 */
	public Vector<Object> readQueryFromFile(String queryFile,
			IMetricSpace metricSpace, int dim) throws IOException {
		return readPivotFromFile(queryFile, metricSpace, dim);
	}

	public Vector<Partition> readIndexFromFile(String indexFile)
			throws IOException {
		Vector<Partition> partSet = new Vector<Partition>();
		BufferedReader fis = null;
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));

		FileSystem fileSystem = FileSystem.get(conf);

		try {
			String line;
			String[] info;
			int i;
			int total_sum = 0;

			InputStreamReader isr = new InputStreamReader(
					fileSystem.open(new Path(indexFile)));
			fis = new BufferedReader(isr);
			// fis = new BufferedReader(new FileReader(indexFile));
			while ((line = fis.readLine()) != null) {
				info = line.split(SQConfig.sepStrForIndex);
				/**
				 * 0: f_id, 1: pid; 2: min_radius; 3: max_radius; 4: size;
				 * 5:num; 6 <dist>
				 * */
				Partition part = new Partition();
				part.setMinMaxRadius(Float.valueOf(info[2]),
						Float.valueOf(info[3]));
				part.setSizeOfPartition(Integer.valueOf(info[4]));
				part.setNumOfObjects(Integer.valueOf(info[5]));
				total_sum += Integer.valueOf(info[5]);
				Vector<Float> distSet = new Vector<Float>();
				for (i = info.length - 1; i > 5; i--) {
					distSet.add(Float.valueOf(info[i]));
				}
				part.setDistOfFirstKObjects(distSet);
				partSet.add(part);
			}

			System.out.println("total_sum: " + total_sum);
			return partSet;
		} catch (IOException ioe) {
			System.err
					.println("Caught exception while parsing the cached file '"
							+ indexFile + "'");
			return null;
		} finally {

			if (fis != null) {
				fis.close();
			}

		}
	}

	public Vector<Integer>[] readGroupsFromFile(String indexFile,
			int numOfGroups) throws IOException {
		@SuppressWarnings("unchecked")
		Vector<Integer>[] groups = new Vector[numOfGroups];

		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));
		FileSystem fileSystem = FileSystem.get(conf);

		BufferedReader fis = null;
		try {
			String line;
			String[] info;
			int i = 0, j;

			InputStreamReader isr = new InputStreamReader(
					fileSystem.open(new Path(indexFile)));
			fis = new BufferedReader(isr);
			while ((line = fis.readLine()) != null) {
				info = line.split(SQConfig.sepSplitForGroupIDs);
				groups[i] = new Vector<Integer>();
				for (j = 0; j < info.length; j++) {
					groups[i].add(Integer.valueOf(info[j]));
				}
				i++;
			}
			return groups;
		} catch (IOException ioe) {
			System.err
					.println("Caught exception while parsing the cached file '"
							+ indexFile + "'");
			return null;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public float[][] readLMatrixFromFile(String filename, int numOfPivots)
			throws IOException {
		DataInputStream fis = null;
		float[][] lMatrix = new float[numOfPivots][numOfPivots];
		try {
			int i, j;
			fis = new DataInputStream(new FileInputStream(filename));
			for (i = 0; i < numOfPivots; i++) {
				for (j = 0; j < numOfPivots; j++) {
					lMatrix[i][j] = fis.readFloat();
				}
			}
			return lMatrix;
		} catch (IOException ioe) {
			System.err
					.println("Caught exception while parsing the cached file '"
							+ filename + "'");
			return null;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * get upper bound for each parttition in pSet1 from pSet2 Assumption:
	 * pivots in pSet1 and pSet2 are the same set
	 * 
	 * @param pSet1
	 *            : pivot, min_r, max_r
	 * @param pSet2
	 *            : pivot, min_r, max_r, dist1, dist2, ....,
	 * @return
	 */
	public float[] getUpperBound(Vector<Partition> pSet1,
			Vector<Partition> pSet2, Vector<Integer> group,
			float[][] distMatrix, int K) throws IOException {
		// int numOfPart1 = pSet1.size();
		int numOfPart1 = group.size();
		int numOfPart2 = pSet2.size();
		float[] ub = new float[numOfPart1];
		int i, j;
		int pidInR;
		float dist;
		// float tmp;
		for (i = 0; i < numOfPart1; i++) {
			pidInR = group.get(i);
			Partition p1 = pSet1.get(pidInR);
			PriorityQueue<Float> knn = new PriorityQueue<Float>(K,
					new ASCPQ<Float>());
			for (j = 0; j < numOfPart2; j++) {
				Partition p2 = pSet2.get(j);
				dist = distMatrix[i][j];
				for (float d : p2.getDistOfFirstKObjects()) {
					float max_dist = d + dist + p1.getMaxRadius();
					;
					if (knn.size() < K) {
						knn.add(max_dist);
					} else if (max_dist < knn.peek()) {
						knn.remove();
						knn.add(max_dist);
					} else {
						break;
					}
				}
			}
			ub[i] = knn.peek();
		}
		return ub;
	}
}
