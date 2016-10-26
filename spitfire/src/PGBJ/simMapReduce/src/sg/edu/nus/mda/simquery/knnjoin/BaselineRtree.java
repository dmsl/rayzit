package sg.edu.nus.mda.simquery.knnjoin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
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

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.SpatialIndexFactory;
import com.infomatiq.jsi.PriorityQueue;

import sg.edu.nus.mda.simquery.metricspace.IMetric;
import sg.edu.nus.mda.simquery.metricspace.IMetricSpace;
import sg.edu.nus.mda.simquery.metricspace.MetricSpaceUtility;
import sg.edu.nus.mda.simquery.metricspace.Record;
import sg.edu.nus.mda.simquery.preprocess.SQConfig;

public class BaselineRtree {
	private static int numOfReducers;
	private static int sqrNumOfReducers;

	public static class BaselineRtreeMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		Random rRand;
		Random sRand;

		protected void setup(Context context) throws IOException,
				InterruptedException {
			numOfReducers = context.getNumReduceTasks();
			sqrNumOfReducers = (int) Math.sqrt(numOfReducers);
			if (sqrNumOfReducers * sqrNumOfReducers != numOfReducers) {
				throw new IOException(
						"Number of reducers must be the square of an integer");
			}
			rRand = new Random();
			sRand = new Random();
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
			} else { // S
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

	public static class BaselineRtreeReducer extends
			Reducer<IntWritable, Text, Text, Text> {
		private IMetricSpace metricSpace = null;
		private IMetric metric = null;
		// for rtree
		Properties p;
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
			K = Integer.valueOf(conf.get(SQConfig.strK, "1"));
			p = new Properties();
			p.setProperty("MinNodeEntries", conf.get(SQConfig.strMinNodeEntries));
			p.setProperty("MaxNodeEntries", conf.get(SQConfig.strMaxNodeEntries));
		}

		private Rectangle getRect(Object o) throws IOException {
			Rectangle rect = null;
			if (o instanceof Record) {
				Record r = (Record) o;
				rect = new Rectangle(r.getValue(), r.getValue());
			} else {
				throw new IOException("Object is not a type of Record!");
			}
			return rect;
		}

		private Point getPoint(Object o) throws IOException {
			Point p = null;
			if (o instanceof Record) {
				Record r = (Record) o;
				System.out.println(r.toString());
				System.out.println(Arrays.toString(r.getValue()));
				p = new Point(r.getValue());
			} else {
				throw new IOException("Object is not a type of Record!");
			}
			return p;
		}

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Vector<Object> R = new Vector<Object>();
			SpatialIndex si;
			si = SpatialIndexFactory.newInstance("rtree.RTree", p);

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
				else {
					Rectangle rect = getRect(o);
					si.add(rect, metricSpace.getID(o));
				}
			}

			// sequentially find knn for each r in R
			for (Object r : R) {
				Point query = getPoint(r);
				PriorityQueue pq = si.nearestN(query, K, Float.MAX_VALUE);
				String line = "";
				if (pq.size() > 0) {
					line += pq.getValue() + SQConfig.sepStrForIDDist
							+ (float) Math.sqrt(pq.getPriority());
					pq.pop();
				}
				while (pq.size() > 0) {
					line += SQConfig.sepStrForKeyValue + pq.getValue()
							+ SQConfig.sepStrForIDDist
							+ (float) Math.sqrt(pq.getPriority());
					pq.pop();
				}
				outputValue.set(line);
				outputKey.set(Integer.toString(metricSpace.getID(r)));
				context.write(outputKey, outputValue);
			}
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
		job.setJarByClass(BaselineRtree.class);
		job.setMapperClass(BaselineRtreeMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(BaselineRtreeReducer.class);
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
		BaselineRtree rs = new BaselineRtree();
		try {
			rs.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
