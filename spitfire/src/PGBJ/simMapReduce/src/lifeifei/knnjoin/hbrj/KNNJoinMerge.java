package lifeifei.knnjoin.hbrj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.hadoop.util.ToolRunner;

import com.infomatiq.jsi.PriorityQueue;

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
		int dim;
		int K;
		Text outputValue = new Text();
		Text outputKey = new Text();

		protected void setup(Context context) throws IOException,
				InterruptedException {
			Configuration conf = context.getConfiguration();
			dim = conf.getInt("dimension", 10);
			K = Integer.valueOf(conf.get("knn"));
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

	private void printUsage() {
		System.out
				.println("NPhase1 "	+ "[-d <dimension>] [-k <knn>] "
						+ "-i <input> -o <output>");
		ToolRunner.printGenericCommandUsage(System.out);
		System.out.println(-1);
	}
	
	public void run(String[] args) throws Exception {
		int dim;
		int k;
		int numOfReducers = 1;
		Configuration conf = new Configuration();
		List<String> other_args = new ArrayList<String>();

		/** set job parameter */
		Job job = new Job(conf, "Merge for KNN Join");
		job.setJarByClass(KNNJoinMerge.class);
		job.setMapperClass(KNNMergeMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(KNNMergeReducer.class);

		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-r".equals(args[i])) {
					numOfReducers = Integer.parseInt(args[++i]);
					job.getConfiguration().set("numOfReducers", Integer.toString(numOfReducers));
					System.out.println("reducer: " + numOfReducers);
				} else if ("-d".equals(args[i])) {
					dim = Integer.parseInt(args[++i]);
					job.getConfiguration().set("dimension", Integer.toString(dim));
					System.out.println("dimension: " + conf.getInt("dimension", 3));
				} else if ("-k".equals(args[i])) {
					k = Integer.parseInt(args[++i]);
					job.getConfiguration().set("knn", Integer.toString(k));
					System.out.println("knn: " + k);
				} else if ("-i".equals(args[i])) {
					other_args.add(args[++i]);
					System.out.println("input path: " + other_args.get(other_args.size() - 1));
				} else if ("-o".equals(args[i])) {
					other_args.add(args[++i]);
					System.out.println("output path: " + other_args.get(other_args.size() - 1));
				} else {
					printUsage();
				}
			} catch (NumberFormatException except) {
				System.out.println("ERROR: Integer expected instead of "
						+ args[i]);
				printUsage();
			} catch (ArrayIndexOutOfBoundsException except) {
				System.out.println("ERROR: Required parameter missing from "
						+ args[i - 1]);
				printUsage();
			}

		}

		job.setNumReduceTasks(numOfReducers);
		if (other_args.size() != 2) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ other_args.size() + " instead of 2.");
			printUsage();
		}

		FileInputFormat.setInputPaths(job, new Path(other_args.get(0)));
		FileOutputFormat.setOutputPath(job, new Path(other_args.get(1)));

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
