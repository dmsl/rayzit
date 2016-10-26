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

import sg.edu.nus.mda.simquery.preprocess.SQConfig;

public class KNNJoinMergeToOneFile {
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
		Text outputValue = new Text();
		Text outputKey = new Text();


		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text value : values) {
				outputKey.set(Integer.toString(key.get()));
				context.write(outputKey, value);
			}

			
		}
	}

	public void run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		new GenericOptionsParser(conf, args).getRemainingArgs();

		/** set job parameter */
		Job job = new Job(conf, "Merge results to a single file");
		job.setJarByClass(KNNJoinMergeToOneFile.class);
		job.setMapperClass(KNNMergeMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(KNNMergeReducer.class);
		job.setNumReduceTasks(1);
		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutputMergeToOne)));

		/** print job parameter */
		System.err
				.println("input path: " + conf.get(SQConfig.strKNNJoinOutput));
		System.err.println("output path: "
				+ conf.get(SQConfig.strKNNJoinOutputMergeToOne));


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
		KNNJoinMergeToOneFile merge = new KNNJoinMergeToOneFile();
		try {
			merge.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
