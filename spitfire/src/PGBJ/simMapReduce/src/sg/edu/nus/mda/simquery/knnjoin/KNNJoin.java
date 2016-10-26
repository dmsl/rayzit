package sg.edu.nus.mda.simquery.knnjoin;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.infomatiq.jsi.PriorityQueue;

import sg.edu.nus.mda.io.BytesRefArrayWritable;
import sg.edu.nus.mda.io.BytesRefWritable;

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricDataInputFormat;
import sg.edu.nus.mda.simquery.metricspace.MetricKey;
import sg.edu.nus.mda.simquery.metricspace.MetricObject;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;
import sg.edu.nus.mda.simquery.metricspace.MetricValue;
import sg.edu.nus.mda.simquery.preprocess.IndexUtility;
import sg.edu.nus.mda.simquery.preprocess.SQConfig;
import sg.edu.nus.mda.simquery.util.Bytes;
import sg.edu.nus.mda.simquery.util.SortByDist;

/**
 * Process KNN join for R * S (1) at setup of map, read pivots, groups, indexes
 * from the dataset (2) in map function, read an object, get the groups it
 * belong, shuffle it with keys and values (3) in reduce function, compute the
 * knn for each object in the partition for R.
 * 
 * @author luwei
 * 
 */
public class KNNJoin {
	private static int dim;
    public static long start=0;

	public static class KNNJoinMapper extends
			Mapper<MetricKey, MetricValue, IntWritable, BytesRefArrayWritable> {
		
		/** matrix space: text, vector */
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		/** value of K */
		int K;
		/** number of pivots */
		int numOfPivots;
		/** number of groups */
		int numOfGroups;
		/** maintain pivots by the order of ids */
		private Vector<Object> pivots;
		/** maintain partitions in R */
		private Vector<Partition> partR;
		/** maintain partitions in S */
		private Vector<Partition> partS;
		/** grouping partitions in R together */
		private Vector<Integer>[] groups;
		private int[] partitionRToGroup;
		// maintain the lower bound for each partition from S with respect to
		// each group of R
		private Vector<SortByDist>[] lbOfPartitionSToGroups;
		private IntWritable interKey;
		float[] gUpperBoundForR;

		/** number of object pairs to be computed */
		static enum Counters {
			MapCount, ReplicationOfS
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
				metric = MetricSpaceUtility.getMetric(conf
						.get(SQConfig.strMetric));
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

		/** read files from local disk */
		private void readCache(IndexUtility indexUtility, Configuration conf) {

			/** parse files in the cache */
			try {

				String strFSName = conf.get("fs.default.name");
				Path[] cacheFiles = new Path[4];
				cacheFiles[0] = new Path(strFSName
						+ conf.get(SQConfig.strPivotInput));
				cacheFiles[1] = new Path(strFSName
						+ conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
						+ "summary" + SQConfig.strIndexExpression1);
				cacheFiles[2] = new Path(strFSName
						+ conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
						+ "summary" + SQConfig.strIndexExpression2);
				cacheFiles[3] = new Path(strFSName
						+ conf.get(SQConfig.strGroupOutput));

				// cacheFiles = DistributedCache.getLocalCacheFiles(conf);

				if (cacheFiles == null || cacheFiles.length < 1)
					return;
				for (Path path : cacheFiles) {
					String filename = path.toString();
					if (filename.endsWith(SQConfig.strPivotExpression)) {
						pivots = indexUtility.readPivotFromFile(filename,
								metricSpace, dim);
					} else if (filename.endsWith(SQConfig.strIndexExpression1)) {
						partR = indexUtility.readIndexFromFile(filename);
					} else if (filename.endsWith(SQConfig.strIndexExpression2)) {
						partS = indexUtility.readIndexFromFile(filename);
					} else if (filename.endsWith(SQConfig.strGroupExpression)) {
						groups = indexUtility.readGroupsFromFile(filename,
								numOfGroups);
					}
				}
			} catch (IOException ioe) {
				System.err
						.println("Caught exception while getting cached files");
			}
		}

		/**
		 * (1) read pivot (2) read index (3) generate candidate pairs
		 */
		@SuppressWarnings("unchecked")
		protected void setup(Context context) throws IOException,
				InterruptedException {
			int i, j, k;
			Configuration conf = context.getConfiguration();
			numOfGroups = conf.getInt(SQConfig.strNumOfGroups, -1);
			dim = conf.getInt(SQConfig.strDimExpression, -1);
			K = conf.getInt(SQConfig.strK, 1);
			IndexUtility indexUtility = new IndexUtility();
			readMetricAndMetricSpace(conf);
			readCache(indexUtility, conf);
			numOfPivots = pivots.size();
			/** assign partitions in R to groups */
			partitionRToGroup = new int[numOfPivots];
			gUpperBoundForR = new float[numOfPivots];
			/** init the bound */
			lbOfPartitionSToGroups = new Vector[numOfPivots];
			for (i = 0; i < numOfPivots; i++) {
				lbOfPartitionSToGroups[i] = new Vector<SortByDist>();
			}

			for (i = 0; i < numOfGroups; i++) {
				Vector<Integer> group = groups[i];
				// space complexity: group.size * numOfPivots
				float[][] distMatrix = new float[group.size()][numOfPivots];
				// compute the matrix
				int pidInR, pidInS;
				for (j = 0; j < group.size(); j++) {
					pidInR = group.get(j);
					partitionRToGroup[pidInR] = i;
					for (k = 0; k < numOfPivots; k++) {
						pidInS = k;
						distMatrix[j][k] = metric.dist(pivots.get(pidInR),
								pivots.get(pidInS));
					}
				}
				float[] upperBoundForR = indexUtility.getUpperBound(partR,
						partS, group, distMatrix, K);
				initLBOfPartitionS(i, group, distMatrix, upperBoundForR);
				for (j = 0; j < group.size(); j++) {
					int tmpPid = group.get(j);
					gUpperBoundForR[tmpPid] = upperBoundForR[j];
				}
			}

			for (i = 0; i < numOfPivots; i++) {
				Collections.sort(lbOfPartitionSToGroups[i]);
			}
		}

		public void map(MetricKey key, MetricValue value, Context context)
				throws IOException, InterruptedException {

		
			int whichFile = key.whichFile;
			int pid = key.pid;
			BytesRefArrayWritable interValue = new BytesRefArrayWritable(1);
			String strValue = value.toString();
			byte[] bValue = strValue.getBytes();
			byte[] bytes = new byte[key.length() + Bytes.SIZEOF_INT
					+ bValue.length];
			int pos = 0;
			// ugly
			int vLen = bValue.length;
			pos = Bytes.putInt(bytes, pos, key.whichFile);
			pos = Bytes.putInt(bytes, pos, key.pid);
			pos = Bytes.putFloat(bytes, pos, key.dist);
			pos = Bytes.putInt(bytes, pos, vLen);
			// set object
			pos = Bytes.putBytes(bytes, pos, bValue, 0, bValue.length);
			BytesRefWritable tmp = new BytesRefWritable();
			tmp.set(bytes, 0, bytes.length);
			interValue.set(0, tmp);

			if (whichFile == 0) { // R
				int gid = partitionRToGroup[pid];
				interKey = new IntWritable(gid);
				context.write(interKey, interValue);
				context.getCounter(Counters.MapCount).increment(1);
			} else {
				Vector<SortByDist> lbs = lbOfPartitionSToGroups[pid];
				for (SortByDist obj : lbs) {
					if (obj.dist <= key.dist) {
						interKey.set(obj.id);
						context.write(interKey, interValue);
						context.getCounter(Counters.MapCount).increment(1);
						context.getCounter(Counters.ReplicationOfS)
								.increment(1);
					} else
						break;
				}

			}
		}

		private void initLBOfPartitionS(int gid, Vector<Integer> group,
				float[][] distMatrix, float[] upperBoundForR) {
			int i, j, pidInS, pidInR;
			float dist, lb, minLB;

			for (i = 0; i < numOfPivots; i++) {
				pidInS = i;
				minLB = partS.get(pidInS).max_r + 1;
				for (j = 0; j < group.size(); j++) {
					pidInR = group.get(j);
					dist = distMatrix[j][i];
					lb = dist - partR.get(pidInR).max_r - upperBoundForR[j];
					if (lb < partS.get(pidInS).min_r) {
						minLB = partS.get(pidInS).min_r;
						break;
					} else if (lb < partR.get(pidInS).max_r && lb < minLB) {
						minLB = lb;
					}
				}
				SortByDist obj = new SortByDist(gid, minLB);
				lbOfPartitionSToGroups[pidInS].add(obj);
			}
		}
	}

	public static class KNNJoinCombiner
			extends
			Reducer<IntWritable, BytesRefArrayWritable, IntWritable, BytesRefArrayWritable> {
		int numOfObjects = 0;

		static enum Counters {
			CombinerCount
		}

		/** combine values with the same key together */
		public void reduce(IntWritable key,
				Iterable<BytesRefArrayWritable> values, Context context)
				throws IOException, InterruptedException {
			int i = 0;
			Vector<byte[]> bytes = new Vector<byte[]>();

			for (BytesRefArrayWritable entry : values) {
				for (i = 0; i < entry.size(); i++) {
					context.getCounter(Counters.CombinerCount).increment(1);
					byte[] tmp = new byte[entry.get(i).getLength()];
					System.arraycopy(entry.get(i).getData(), 0, tmp, 0,
							tmp.length);
					bytes.add(tmp);
				}
			}

			BytesRefArrayWritable array = new BytesRefArrayWritable(
					bytes.size());
			for (i = 0; i < bytes.size(); i++) {
				BytesRefWritable tmp = array.unCheckedGet(i);
				byte[] tmpBytes = bytes.get(i);
				tmp.set(tmpBytes, 0, tmpBytes.length);
			}
			array.resetValid(bytes.size());
			context.write(key, array);
		}
	}

	public static class KNNJoinReducer extends
			Reducer<IntWritable, BytesRefArrayWritable, IntWritable, Text> {
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		int K;
		int numOfPivots;
		private Vector<Object> pivots;
		private Vector<Partition> partitionsOfS;

		/** number of object pairs to be computed */
		static enum Counters {
			pairwise, s
		}

		IntWritable outputKey = new IntWritable();
		Text outputValue = new Text();

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
				metric = MetricSpaceUtility.getMetric(conf
						.get(SQConfig.strMetric));
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

		private void readCache(IndexUtility indexUtility, Configuration conf) {

			/** parse files in the cache */
			try {
				String strFSName = conf.get("fs.default.name");
				Path[] cacheFiles = new Path[4];
				cacheFiles[0] = new Path(strFSName
						+ conf.get(SQConfig.strPivotInput));
				cacheFiles[1] = new Path(strFSName
						+ conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
						+ "summary" + SQConfig.strIndexExpression1);
				cacheFiles[2] = new Path(strFSName
						+ conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
						+ "summary" + SQConfig.strIndexExpression2);
				cacheFiles[3] = new Path(strFSName
						+ conf.get(SQConfig.strGroupOutput));
				// cacheFiles = DistributedCache.getLocalCacheFiles(conf);

				if (cacheFiles == null || cacheFiles.length < 1)
					return;
				for (Path path : cacheFiles) {
					String filename = path.toString();
					if (filename.endsWith(SQConfig.strPivotExpression)) {
						/** this is pivot file */
						pivots = indexUtility.readPivotFromFile(filename,
								metricSpace, dim);
					} else if (filename.endsWith(SQConfig.strIndexExpression2)) {
						partitionsOfS = indexUtility
								.readIndexFromFile(filename);
					}
				}
			} catch (IOException ioe) {
				System.err
						.println("Caught exception while getting cached files");
			}
		}

		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			dim = conf.getInt(SQConfig.strDimExpression, -1);
			IndexUtility indexUtility = new IndexUtility();
			readMetricAndMetricSpace(conf);
			readCache(indexUtility, conf);
			numOfPivots = pivots.size();
			K = Integer.valueOf(conf.get(SQConfig.strK, "1"));
		}

		private MetricObject parseObject(byte[] bytes) {
			int whichFile = Bytes.toInt(bytes, 0);
			int pid = Bytes.toInt(bytes, Bytes.SIZEOF_INT);
			float dist = Bytes.toFloat(bytes, 2 * Bytes.SIZEOF_INT);
			int offset = 2 * Bytes.SIZEOF_INT + Bytes.SIZEOF_FLOAT;
			int vLen = Bytes.toInt(bytes, offset);
			offset += Bytes.SIZEOF_INT;
			String strLine = Bytes.toString(bytes, offset, vLen);
			Object obj = metricSpace.readObject(strLine, dim);

			return new MetricObject(whichFile, pid, dist, obj);
		}

		/**
		 * find knn for each string in the key.pid format of each value in
		 * values
		 * 
		 */
		@SuppressWarnings("unchecked")
		public void reduce(IntWritable key,
				Iterable<BytesRefArrayWritable> values, Context context)
				throws IOException, InterruptedException {
			long stop = System.currentTimeMillis();
			
			System.out.println("Map finish \t"+(stop-start));
			
			int i, size;
			Vector<MetricObject>[] partR = new Vector[numOfPivots];
			Vector<MetricObject>[] partS = new Vector[numOfPivots];
			for (BytesRefArrayWritable value : values) {
				size = value.size();
				for (i = 0; i < size; i++) {
					BytesRefWritable object = value.get(i);
					MetricObject mo = parseObject(object.getData());
					if (mo.fid == 0) {
						if (partR[mo.pid] == null) {
							partR[mo.pid] = new Vector<MetricObject>();
						}
						partR[mo.pid].add(mo);
					} else {
						if (partS[mo.pid] == null) {
							partS[mo.pid] = new Vector<MetricObject>();

						}
						partS[mo.pid].add(mo);
						context.getCounter(Counters.s).increment(1);
					}
				}
			}

			/**
			 * sort objects based on the descending order of distance in each
			 * partition of S
			 */
			for (Vector<MetricObject> part : partS)
				if (part != null)
					Collections.sort(part);
			long begin = System.currentTimeMillis();
			compKNN(context, partR, partS);
			long end = System.currentTimeMillis();
			long second = (end - begin) / 1000;
			System.err.println("computation time " + " takes " + second
					+ " seconds");

			context.getCounter(Counters.pairwise).increment(
					metric.getNumOfDistComp());
		}

		private void compKNN(Context context, Vector<MetricObject>[] partR,
				Vector<MetricObject>[] partS) throws IOException,
				InterruptedException {
			for (int i = 0; i < numOfPivots; i++) {
				if (partR[i] != null) {
					findKNNForPartition(context, i, partR[i], partS);
				}
			}
		}

		/**
		 * find knn for objects in partFromR
		 * 
		 * @param context
		 * @param partFromR
		 * @param partS
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void findKNNForPartition(Context context, int pid,
				Vector<MetricObject> partFromR, Vector<MetricObject>[] partS)
				throws IOException, InterruptedException {
			SortByDist[] sortByDist = new SortByDist[numOfPivots];
			Object pivot = pivots.get(pid);
			float[] distBetweenPivots = new float[numOfPivots];
			for (int i = 0; i < numOfPivots; i++) {
				if (partS[i] != null) {
					distBetweenPivots[i] = metric.dist(pivot, pivots.get(i));
				} else {
					distBetweenPivots[i] = Float.MAX_VALUE;
				}
				SortByDist sort = new SortByDist(i, distBetweenPivots[i]);
				sortByDist[i] = sort;
			}
			Arrays.sort(sortByDist);

			for (MetricObject obj : partFromR) {
				findKNNForSingleObject(context, obj, partS, sortByDist,
						distBetweenPivots);
			}
		}

		private float getUpperBound(MetricObject o_R, float[] distToPivot,
				SortByDist[] sortByDist) throws IOException {
			PriorityQueue pq = new PriorityQueue(
					PriorityQueue.SORT_ORDER_DESCENDING);
			Partition part;
			float max_dist;

			for (int j = 0; j < numOfPivots; j++) {
				int i = sortByDist[j].id;

				Object otherPivot = pivots.get(i);
				distToPivot[i] = metric.dist(o_R.obj, otherPivot);
				part = partitionsOfS.get(i);
				Vector<Float> distOfFirstKObjects = part.distOfFirstKObjects;
				for (float radius : distOfFirstKObjects) {
					max_dist = radius + distToPivot[i];
					if (pq.size() < K) {
						pq.insert(-1, max_dist);
					} else {
						if (max_dist < pq.getPriority()) {
							pq.pop();
							pq.insert(-1, max_dist);
						} else
							break;
					}
				}
			}

			return pq.getPriority();
		}

		/**
		 * need optimization
		 * 
		 * @throws InterruptedException
		 */
		private void findKNNForSingleObject(Context context, MetricObject o_R,
				Vector<MetricObject>[] partS, SortByDist[] sortByDist,
				float[] distBetweenPivots) throws IOException,
				InterruptedException {
			int pid;
			float dist;
			PriorityQueue pq = new PriorityQueue(
					PriorityQueue.SORT_ORDER_DESCENDING);
			float[] distToPivot = new float[numOfPivots];
			float theta = getUpperBound(o_R, distToPivot, sortByDist);

			for (SortByDist sort : sortByDist) {
				pid = sort.id;
				if (sort.dist == Float.MAX_VALUE)
					break;

				/** compute and update */
				Vector<MetricObject> partFromS = partS[pid];
				// compute the distance to HP(p_i,p_j)
				float distToHP = (distToPivot[pid] * distToPivot[pid] - o_R.distToPivot
						* o_R.distToPivot)
						/ (2 * distBetweenPivots[pid]);
				if (distToHP > theta)
					continue;

				for (MetricObject o_S : partFromS) {
					if (distToPivot[pid] > o_S.distToPivot + theta) {
						// continue;
						break;
					}

					dist = metric.dist(o_R.obj, o_S.obj);
					// context.getCounter(Counters.pairwise).increment(1);
					if (dist <= theta) {
						if (pq.size() < K) {
							pq.insert(metricSpace.getID(o_S.obj), dist);
						} else if (dist != theta) {
							pq.pop();
							pq.insert(metricSpace.getID(o_S.obj), dist);
							theta = pq.getPriority();
						}
					}
				}
			}
			String line = "";
			if (pq.size() > 0) {
				line += pq.getValue() + SQConfig.sepStrForIDDist
						+ pq.getPriority();
				pq.pop();
			}
			while (pq.size() > 0) {
				line += SQConfig.sepStrForKeyValue + pq.getValue()
						+ SQConfig.sepStrForIDDist + pq.getPriority();
				pq.pop();
			}
			outputValue.set(line);
			outputKey.set(metricSpace.getID(o_R.obj));
			context.write(outputKey, outputValue);
		}
	}

	public void run(String[] args) throws Exception {
		
		start = System.currentTimeMillis();
		
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));

		new GenericOptionsParser(conf, args).getRemainingArgs();
		/** set job parameter */
		Job job = new Job(conf, "KNN Join Queries");
		String strFSName = conf.get("fs.default.name");

		job.setJarByClass(KNNJoin.class);
		job.setMapperClass(KNNJoinMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(BytesRefArrayWritable.class);
		job.setCombinerClass(KNNJoinCombiner.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setInputFormatClass(MetricDataInputFormat.class);
		job.setReducerClass(KNNJoinReducer.class);
		job.setNumReduceTasks(conf.getInt(SQConfig.strNumOfGroups, 1));
		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinInput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutput)));
		DistributedCache.addCacheFile(
				new URI(strFSName + conf.get(SQConfig.strPivotInput)),
				job.getConfiguration());
		DistributedCache
				.addCacheFile(
						new URI(strFSName + conf.get(SQConfig.strIndexOutput)
								+ Path.SEPARATOR + "summary"
								+ SQConfig.strIndexExpression1),
						job.getConfiguration());
		DistributedCache
				.addCacheFile(
						new URI(strFSName + conf.get(SQConfig.strIndexOutput)
								+ Path.SEPARATOR + "summary"
								+ SQConfig.strIndexExpression2),
						job.getConfiguration());
		DistributedCache.addCacheFile(
				new URI(strFSName + conf.get(SQConfig.strGroupOutput)),
				job.getConfiguration());

		/** print job parameter */
		System.err.println("input path: " + conf.get(SQConfig.strKNNJoinInput));
		System.err.println("output path: "
				+ conf.get(SQConfig.strKNNJoinOutput));
		System.err.println("pivot file: " + conf.get(SQConfig.strPivotInput));
		System.err.println("index file 1: " + conf.get(SQConfig.strIndexOutput)
				+ Path.SEPARATOR + "summary" + SQConfig.strIndexExpression1);
		System.err.println("index file 2: " + conf.get(SQConfig.strIndexOutput)
				+ Path.SEPARATOR + "summary" + SQConfig.strIndexExpression2);
		System.err.println("group file: " + conf.get(SQConfig.strGroupOutput));
		System.err.println("dataspace: " + conf.get(SQConfig.strMetricSpace));
		System.err.println("metric: " + conf.get(SQConfig.strMetric));
		System.err.println("value of K: " + conf.get(SQConfig.strK));
		System.err.println("# of groups: " + conf.get(SQConfig.strNumOfGroups));
		System.err.println("# of dim: "
				+ conf.getInt(SQConfig.strDimExpression, 10));
		
		long begin = System.currentTimeMillis();
		job.waitForCompletion(true);
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		
		System.err.println(job.getJobName() + " takes " + second + " seconds");
	}

	public static void main(String[] args) throws Exception {
		KNNJoin rs = new KNNJoin();
		rs.run(args);
	}
}
