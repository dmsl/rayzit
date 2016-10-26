package sg.edu.nus.mda.simquery.knnjoin;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

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

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;
import sg.edu.nus.mda.simquery.preprocess.SQConfig;

import com.infomatiq.jsi.PriorityQueue;

public class BaselineNL {
	public static class BaselineMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		private static int numOfReducers;
		private static int sqrNumOfReducers;
		Random rRand;
		Random sRand;

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
			int pos = line.indexOf(SQConfig.sepStrForRecord);
			int whichFile = Integer.valueOf(line.substring(0, pos));

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

				System.out.println("sqrNumOfReducers: " + sqrNumOfReducers);

				for (int i = 0; i < sqrNumOfReducers; i++) {
					squareId = randId + i * sqrNumOfReducers;
					context.write(new IntWritable(squareId), value);
				}
			}
		}
	}

	public static class BaselineReducer extends
			Reducer<IntWritable, Text, Text, Text> {
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		int dim;
		int K;
		Text outputValue = new Text();
		Text outputKey = new Text();

		/** number of object pairs to be computed */
		static enum Counters {
			PairsOne, PairsTwo, PairsThree, PairsFour
		}

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
			K = Integer.valueOf(conf.get(SQConfig.strK, "1"));
		}

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Vector<Object> R = new Vector<Object>();
			Vector<Object> S = new Vector<Object>();

			for (Text value : values) {
				// parse objects
				String line = value.toString();
				int pos = line.indexOf(SQConfig.sepStrForRecord);
				int whichFile = Integer.valueOf(line.substring(0, pos));
				/** parse the object */
				String lineOfObject = line.substring(pos + 1, line.length());
				Object o = metricSpace.readObject(lineOfObject, dim);
				if (whichFile == 0)
					R.add(o);
				else
					S.add(o);
			}

			System.out.println("R.size: " + R.size());
			System.out.println("S.size: " + S.size());

			// sequentially find knn for each r in R
			float dist;
			for (Object r : R) {
				PriorityQueue pq = new PriorityQueue(
						PriorityQueue.SORT_ORDER_DESCENDING);
				for (Object s : S) {
					dist = metric.dist(r, s);
					if (pq.size() < K) {
						pq.insert(metricSpace.getID(s), dist);
					} else if (pq.getPriority() > dist) {
						pq.pop();
						pq.insert(metricSpace.getID(s), dist);
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
				outputKey.set(Integer.toString(metricSpace.getID(r)));
				context.write(outputKey, outputValue);
			}
			context.getCounter(Counters.PairsOne)
					.increment(R.size() * S.size());
		}
	}

	public void run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));
		
		new GenericOptionsParser(conf, args).getRemainingArgs();

		/** set job parameter */
		Job job = new Job(conf, "Baseline for KNN Join");
		job.setJarByClass(BaselineNL.class);
		job.setMapperClass(BaselineMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(BaselineReducer.class);
		job.setNumReduceTasks(conf.getInt(SQConfig.strNumOfGroups, 1));
		FileInputFormat.addInputPath(job,
				new Path(conf.get(SQConfig.dataSplitInput)));
		FileOutputFormat.setOutputPath(job,
				new Path(conf.get(SQConfig.strKNNJoinOutput)));

		/** print job parameter */
		System.err.println("input path: " + conf.get(SQConfig.dataSplitInput));
		System.err.println("output path: "
				+ conf.get(SQConfig.strKNNJoinOutput));
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BaselineNL rs = new BaselineNL();
		try {
			rs.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

}
