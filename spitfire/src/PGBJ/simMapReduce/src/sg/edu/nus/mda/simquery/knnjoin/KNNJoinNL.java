package sg.edu.nus.mda.simquery.knnjoin;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.infomatiq.jsi.PriorityQueue;

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricObject;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;
import sg.edu.nus.mda.simquery.preprocess.IndexUtility;
import sg.edu.nus.mda.simquery.preprocess.SQConfig;
import sg.edu.nus.mda.simquery.util.SortByDist;

public class KNNJoinNL {
	private static int dim;

	public static class KNNJoinNLMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		private static int numOfReducers;
		private static int sqrNumOfReducers;
		Random rRand;
		Random sRand;

		/**
		 * (1) read pivot (2) read index (3) generate candidate pairs
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException {
			rRand = new Random();
			sRand = new Random();
			numOfReducers = context.getNumReduceTasks();
			sqrNumOfReducers = (int) Math.sqrt(numOfReducers);
			if (sqrNumOfReducers * sqrNumOfReducers != numOfReducers) {
				throw new IOException(
						"Number of reducers must be the square of an integer");
			}
		}

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			int pos = line.indexOf(",");

			int whichFile = 0;
			try {
				whichFile = Integer.valueOf(line.substring(0, pos));
			} catch (Exception ex) {
				System.out.println(line);
				return;
			}

			if (whichFile == 0) { // R
				/** generate a random value */
				int randId = rRand.nextInt(sqrNumOfReducers);
				int beginOfRId = randId * sqrNumOfReducers;
				int squareId;

				for (int i = 0; i < sqrNumOfReducers; i++) {
					squareId = beginOfRId + i;
					context.write(new IntWritable(squareId), value);
				}

			} else {
				/** generate a random value */
				int randId = sRand.nextInt(sqrNumOfReducers);
				int squareId;

				for (int i = 0; i < sqrNumOfReducers; i++) {
					squareId = randId + i * sqrNumOfReducers;
					context.write(new IntWritable(squareId), value);
				}

			}
		}
	}

	public static class KNNJoinNLReducer extends
			Reducer<IntWritable, Text, IntWritable, Text> {
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		int K;
		int numOfPivots;
		private Vector<Object> pivots;
		private Vector<Partition> partitionsOfS;
		IntWritable outputKey = new IntWritable();
		Text outputValue = new Text();

		/** number of object pairs to be computed */
		static enum Counters {
			pairwise
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

		private void readCache(IndexUtility indexUtility, Configuration conf) {
			/** parse files in the cache */
			try {
				Path[] cacheFiles = new Path[2];
				String strFSName = conf.get("fs.default.name");

				// EDITED
				cacheFiles[0] = new Path(strFSName
						+ conf.get(SQConfig.strPivotInput));
				cacheFiles[1] = new Path(strFSName
						+ conf.get(SQConfig.strIndexOutput) + Path.SEPARATOR
						+ "summary" + SQConfig.strIndexExpression2);
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

		/**
		 * find knn for each string in the key.pid format of each value in
		 * values
		 * 
		 */
		@SuppressWarnings("unchecked")
		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Vector<MetricObject>[] partR = new Vector[numOfPivots];
			Vector<MetricObject>[] partS = new Vector[numOfPivots];

			for (Text value : values) {
				String line = value.toString();
				int pos = line.indexOf(SQConfig.sepStrForKeyValue);
				String infoOfPart = line.substring(0, pos);
				String[] part = infoOfPart.split(SQConfig.sepStrForIndex);
				int fid = Integer.valueOf(part[0]);
				int pid = Integer.valueOf(part[1]);
				float dist = Float.valueOf(part[2]);
				Object o = metricSpace.readObject(line.substring(pos + 1), dim);
				MetricObject mo = new MetricObject(fid, pid, dist, o);

				if (fid == 0) { // R
					if (partR[pid] == null) {
						partR[pid] = new Vector<MetricObject>();
					}
					partR[pid].add(mo);
				} else { // S
					if (partS[pid] == null) {
						partS[pid] = new Vector<MetricObject>();
					}
					partS[pid].add(mo);
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
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));

		new GenericOptionsParser(conf, args).getRemainingArgs();
		/** set job parameter */
		Job job = new Job(conf, "KNN Join Queries");
		String strFSName = conf.get("fs.default.name");
		job.setJarByClass(KNNJoinNL.class);
		job.setMapperClass(KNNJoinNLMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(KNNJoinNLReducer.class);
		job.setNumReduceTasks(conf.getInt(SQConfig.strNumOfGroups, -1));
		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinInput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutput)));
		DistributedCache.addCacheFile(
				new URI(strFSName + conf.get(SQConfig.strPivotInput)),
				job.getConfiguration());
		// index2
		DistributedCache
				.addCacheFile(
						new URI(strFSName + conf.get(SQConfig.strIndexOutput)
								+ Path.SEPARATOR + "summary"
								+ SQConfig.strIndexExpression2),
						job.getConfiguration());
		/** print job parameter */
		System.err.println("input path: " + conf.get(SQConfig.strKNNJoinInput));
		System.err.println("output path: "
				+ conf.get(SQConfig.strKNNJoinOutput));
		System.err.println("pivot file: " + conf.get(SQConfig.strPivotInput));
		System.err.println("index file 2: " + conf.get(SQConfig.strIndexOutput)
				+ Path.SEPARATOR + "summary" + SQConfig.strIndexExpression2);
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
		KNNJoinNL rs = new KNNJoinNL();
		rs.run(args);
	}
}
