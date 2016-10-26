package sg.edu.nus.mda.simquery.knnjoin;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
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
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;
import sg.edu.nus.mda.simquery.preprocess.SQConfig;

public class KNNJoinMerge {
	public static class KNNMergeMapper extends
			Mapper<Object, Text, IntWritable, Text> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			int pos = line.indexOf(SQConfig.sepStrForKeyValue);
			int rid = Integer.valueOf(line.substring(0, pos));

			context.write(new IntWritable(rid), value);
		}
	}

	public static class KNNMergeReducer extends
			Reducer<IntWritable, Text, Text, Text> {
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		int dim;
		int K;
		Text outputValue = new Text();
		Text outputKey = new Text();

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

		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			readMetricAndMetricSpace(conf);
			dim = conf.getInt(SQConfig.strDimExpression, 10);
			K = Integer.valueOf(conf.get(SQConfig.strK));
		}

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			PriorityQueue pq = new PriorityQueue(
					PriorityQueue.SORT_ORDER_DESCENDING);

			for (Text value : values) {
				// parse objects
				String line = value.toString();
				System.out.println(line);
				String[] items = line.split(SQConfig.sepStrForKeyValue);

				for (int i = 1; i < items.length; i++) {
					String[] id_dist = items[i].split(SQConfig.sepSplitForIDDist);
					int id = Integer.valueOf(id_dist[0]);
					float dist = Float.valueOf(id_dist[1]);
					if (pq.size() < K) {
						pq.insert(id, dist);
					} else if (pq.getPriority() > dist) {
						pq.pop();
						pq.insert(id, dist);
					}
				}
			}

			String output = "";
			while (pq.size() > 0) {
				output += SQConfig.sepStrForKeyValue + pq.getValue()
						+ SQConfig.sepStrForIDDist + pq.getPriority();
				pq.pop();
			}
			outputValue.set(output);
			outputKey.set(Integer.toString(key.get()));
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
		Job job = new Job(conf, "Merge for KNN Join");
		job.setJarByClass(KNNJoinMerge.class);
		job.setMapperClass(KNNMergeMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(KNNMergeReducer.class);
		job.setNumReduceTasks(conf.getInt(SQConfig.strNumOfGroups, -1));
		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutputMerge)));

		/** print job parameter */
		System.err
				.println("input path: " + conf.get(SQConfig.strKNNJoinOutput));
		System.err.println("output path: "
				+ conf.get(SQConfig.strKNNJoinOutputMerge));
		System.err.println("dataspace: " + conf.get(SQConfig.strMetricSpace));
		System.err.println("metric: " + conf.get(SQConfig.strMetric));
		System.err.println("value of K: " + conf.get(SQConfig.strK));
		System.err.println("# of dim: "
				+ conf.getInt(SQConfig.strDimExpression, 10));

		long begin = System.currentTimeMillis();
		job.waitForCompletion(true);
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println(job.getJobName() + " takes " + second + " seconds");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KNNJoinMerge merge = new KNNJoinMerge();
		try {
			merge.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
