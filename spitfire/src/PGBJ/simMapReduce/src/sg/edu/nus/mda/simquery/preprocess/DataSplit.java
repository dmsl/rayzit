package sg.edu.nus.mda.simquery.preprocess;

import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricKey;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;

/**
 * This class is used to: (1) split the input dataset into partition according
 * to the pivots (2) compute the summary for each partition over each reducer.
 * Hence, we need to another step to compute the summary for the whole partition
 * across all reducers.
 * 
 * @author luwei
 * 
 */
public class DataSplit {
	public static class DataSplitMapper extends
			Mapper<Object, Text, MetricKey, Text> {
		/**
		 * VectorSpace and L1 are set as the default metric space and metric.
		 */
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		private int dim;
		/** store pivots */
		private int numOfPivots;
		private Vector<Object> pivots;
		private Vector<Integer> numOfObjects = new Vector<Integer>();
		/**
		 * R * S join the following information will be written to a summary
		 * file
		 * */
		private float[] min_R, max_R;
		private float[] min_S, max_S;
		private int[] numOfObjects_R, sizeOfObjects_R;
		private int[] numOfObjects_S, sizeOfObjects_S;
		private PriorityQueue<Float>[] KNNObjectsToPivots_S;
		/** KNN */
		int K;
		/** intermediate key */
		private MetricKey interKey;

		/** number of object pairs to be computed */
		static enum Counters {
			sum
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

		/**
		 * read pivots from input
		 * 
		 * @param conf
		 */
		private void readPivot(Configuration conf) throws IOException {
			IndexUtility indexUtility = new IndexUtility();
			Path[] pivotFiles = new Path[1];

//			pivotFiles = DistributedCache.getLocalCacheFiles(conf);
			pivotFiles[0]= new Path("/mda/pivots/openStreetMap/50K-2-200-random.pivot");
//			System.out.println("pivot-conf:" + conf);
//			System.out.println("pivotFiles:" + pivotFiles);

			if (pivotFiles == null || pivotFiles.length < 1)
				throw new IOException("No pivots are provided!");

			for (Path path : pivotFiles) {
				String filename = path.toString();
				if (filename.endsWith(SQConfig.strPivotExpression))
					pivots = indexUtility.readPivotFromFile(filename,
							metricSpace, dim);
			}
		}

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

		@SuppressWarnings("unchecked")
		private void initSummaryInfo() {
			min_R = new float[numOfPivots];
			max_R = new float[numOfPivots];
			min_S = new float[numOfPivots];
			max_S = new float[numOfPivots];
			numOfObjects_R = new int[numOfPivots];
			numOfObjects_S = new int[numOfPivots];
			sizeOfObjects_R = new int[numOfPivots];
			sizeOfObjects_S = new int[numOfPivots];
			KNNObjectsToPivots_S = new PriorityQueue[numOfPivots];

			for (int i = 0; i < numOfPivots; i++) {
				min_S[i] = min_R[i] = Float.MAX_VALUE;
				max_S[i] = max_R[i] = 0;
				numOfObjects_R[i] = numOfObjects_S[i] = 0;
				sizeOfObjects_R[i] = sizeOfObjects_S[i] = 0;
				KNNObjectsToPivots_S[i] = new PriorityQueue<Float>(K,
						new ASCPQ<Float>());
				numOfObjects.add(0);
			}
		}

		/**
		 * Called once at the beginning of the task. In setup, we (1) init
		 * metric and metric space (2) read pivots (3) init matrix
		 */
		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			// conf.addResource(new
			// Path("/usr/local/hadoop/conf/core-site.xml"));
			// conf.addResource(new
			// Path("/usr/local/hadoop/conf/hdfs-site.xml"));
			// conf.addResource(new Path(
			// "/home/cs246/Desktop/source code of mapjoin/conf/openStreetMap-50K-2-50K-random.conf"));

			/** get K */
			K = Integer.valueOf(conf.get(SQConfig.strK, "1"));
			/** get dim */
			dim = conf.getInt(SQConfig.strDimExpression, 10);
			/** read metric */
			readMetricAndMetricSpace(conf);
			/** read pivot set */
			readPivot(conf);
			numOfPivots = pivots.size();
			initSummaryInfo();
		}

		/**
		 * @param o
		 *            : find the closest pivot for the input object
		 * @return: pivot id + distance (Bytes.SIZEOF_INT + Bytes.SIZEOF_float)
		 * @throws IOException
		 */
		private MetricKey getKey(Object o, int which_file) throws IOException {
			int i, closestPivot = 0;
			float closestDist = Float.MAX_VALUE;
			float tmpDist;

			/** find the closest pivot */
			for (i = 0; i < numOfPivots; i++) {
				tmpDist = metric.dist(pivots.get(i), o);
				if (tmpDist < closestDist) {
					closestDist = tmpDist;
					closestPivot = i;
				} else if (closestDist == tmpDist
						&& numOfObjects.get(closestPivot) > numOfObjects.get(i)) {
					closestPivot = i;
				}
			}

			/** set intermediate key */
			MetricKey metricKey = new MetricKey();
			metricKey.dist = closestDist;
			metricKey.pid = closestPivot;
			metricKey.whichFile = which_file;

			// plus 1 for the number of objects in the partition (pivotId)
			numOfObjects.set(closestPivot, numOfObjects.get(closestPivot) + 1);
			return metricKey;
		}

		private void updateSummaryInfo(int whichFile, MetricKey interKey,
				int len) {
			int pid = interKey.pid;
			float dist = interKey.dist;

			if (whichFile == 0) {
				// R
				if (dist < min_R[pid])
					min_R[pid] = dist;

				if (dist > max_R[pid])
					max_R[pid] = dist;

				numOfObjects_R[pid]++;
				sizeOfObjects_R[pid] += len;
			} else {
				// S
				if (dist < min_S[pid])
					min_S[pid] = dist;

				if (dist > max_S[pid])
					max_S[pid] = dist;

				numOfObjects_S[pid]++;
				sizeOfObjects_S[pid] += len;
				if (KNNObjectsToPivots_S[pid].size() < K) {
					KNNObjectsToPivots_S[pid].add(dist);
				} else {
					if (dist < KNNObjectsToPivots_S[pid].peek()) {
						KNNObjectsToPivots_S[pid].remove();
						KNNObjectsToPivots_S[pid].add(dist);
					}
				}
			}
		}

		/**
		 * We generate an intermediate key and value (1) key: the combination of
		 * the pivot id (int) and distance (float) (2) value: the input object
		 * 
		 * @key: offset of the source file
		 * @value: a single object in the metric space
		 */
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String lineOfObject;
			int pos = line.indexOf(",");
			int whichFile = 0;
			try {
				whichFile = Integer.valueOf(line.substring(0, pos));
			} catch (Exception ex) {
				System.out.println(line);
				return;
			}
			// int whichFile = Integer.valueOf(line.substring(0, pos));
			/** parse the object */
			lineOfObject = line.substring(pos + 1, line.length());
			Object o = metricSpace.readObject(lineOfObject, dim);
			/** generate the intermediate key */
			interKey = getKey(o, whichFile);
			/** update the summary info */
			updateSummaryInfo(whichFile, interKey, lineOfObject.length());
			context.write(interKey, new Text(lineOfObject));
		}

		/**
		 * output summary infomration
		 */
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			/** output the summary of R and S */
			outputSummary(context);
		}

		private void outputSummary(Context context) throws IOException {
			FileSystem fs;
			FSDataOutputStream currentStream = null;
			Configuration conf = context.getConfiguration();
			// conf.addResource(new
			// Path("/usr/local/hadoop/conf/core-site.xml"));
			// conf.addResource(new
			// Path("/usr/local/hadoop/conf/hdfs-site.xml"));
			// conf.addResource(new Path(
			// "/home/cs246/Desktop/source code of mapjoin/conf/openStreetMap-50K-2-50K-random.conf"));
			//
			/** create output name */
			String summaryOutput = conf.get(SQConfig.strIndexOutput);
			int partition = context.getConfiguration().getInt(
					"mapred.task.partition", -1);
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMinimumIntegerDigits(5);
			numberFormat.setGroupingUsed(false);
			summaryOutput = summaryOutput + "/summary" + "-m-"
					+ numberFormat.format(partition);

			fs = FileSystem.get(conf);
			Path path = new Path(summaryOutput);
			currentStream = fs.create(path, true);
			String line;

			/** writhe the summary information for R */
			for (int i = 0; i < numOfPivots; i++) {
				if (numOfObjects_R[i] == 0)
					continue;
				line = Integer.toString(0) + "," + Integer.toString(i) + ","
						+ min_R[i] + "," + max_R[i] + "," + sizeOfObjects_R[i]
						+ "," + numOfObjects_R[i] + "\n";
				currentStream.writeBytes(line);
			}
			/** write the summary information for S */
			for (int i = 0; i < numOfPivots; i++) {
				if (numOfObjects_S[i] == 0)
					continue;
				line = Integer.toString(1) + "," + i + "," + min_S[i] + ","
						+ max_S[i] + "," + sizeOfObjects_S[i] + ","
						+ numOfObjects_S[i];
				context.getCounter(Counters.sum).increment(numOfObjects_S[i]);
				PriorityQueue<Float> pq = KNNObjectsToPivots_S[i];
				while (pq.size() > 0) {
					line += "," + pq.remove();
				}
				line += "\n";
				currentStream.writeBytes(line);
			}
			currentStream.close();
		}
	}

	public void run(String[] args) throws Exception {
		Configuration conf = new Configuration();
	    conf.addResource(new Path(SQConfig.configPath1));
	    conf.addResource(new Path(SQConfig.configPath2));
	    conf.addResource(new Path(SQConfig.configPath3));

		new GenericOptionsParser(conf, args).getRemainingArgs();
		String strFSName = conf.get("fs.default.name");

		/** print job parameter */
		System.err.println("input path: " + conf.get(SQConfig.dataSplitInput));
		System.err
				.println("output path: " + conf.get(SQConfig.strKNNJoinInput));
		System.err.println("pivot path: " + conf.get(SQConfig.strPivotInput));
		System.err.println("dataspace: " + conf.get(SQConfig.strMetricSpace));
		System.err.println("metric: " + conf.get(SQConfig.strMetric));
		System.err.println("index out: " + conf.get(SQConfig.strIndexOutput));
		System.err.println("value of K: " + conf.get(SQConfig.strK));
		System.err.println("# of dim: "
				+ conf.getInt(SQConfig.strDimExpression, -1));

		/** set job parameter */
		Job job = new Job(conf, "data splitting");
		job.setJarByClass(DataSplit.class);
		job.setMapperClass(DataSplitMapper.class);
		job.setOutputKeyClass(MetricKey.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setNumReduceTasks(0);

		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.dataSplitInput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinInput)));

		URI uri = new URI(strFSName + conf.get(SQConfig.strPivotInput));
		
		System.out.println("uri(" + uri+ ")");
		DistributedCache.addCacheFile(uri, job.getConfiguration());

		long begin = System.currentTimeMillis();
		job.waitForCompletion(true);
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println(job.getJobName() + " takes " + second + " seconds");
	}

	public static void main(String[] args) throws Exception {
		DataSplit DataSplit = new DataSplit();
		DataSplit.run(args);
	}
}
