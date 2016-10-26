/*
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER
 * PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS
 * TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM
 * PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR
 * OR CORRECTION.
 */

package lifeifei.knnjoin.hbrj;

import java.io.*;
import java.util.*;

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

import sg.edu.nus.mda.simquery.preprocess.SQConfig;

import de.lmu.ifi.dbs.elki.data.FloatVector;
import de.lmu.ifi.dbs.elki.database.DistanceResultPair;
import de.lmu.ifi.dbs.elki.distance.DoubleDistance;
import de.lmu.ifi.dbs.elki.distance.distancefunction.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTree;
import de.lmu.ifi.dbs.elki.index.tree.spatial.*;
import de.lmu.ifi.dbs.elki.index.tree.TreeIndex;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Phase1 of Hadoop Block R*-tree KNN Join (H-BRJ).
 */

public class BaselineRtree {
	public static final int MB = 1024 * 1024;
	public static final int KB = 1024;
	public static final int scale = 1000;
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
			System.out.println("============="
					+ context.getConfiguration().get("knn"));
		}

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			int pos = line.indexOf(SQConfig.sepStrForRecord);
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

	/**
	 * Perform R*-tree based KNN Join for each partition/bucket.
	 */
	public static class BaselineRtreeReducer extends
			Reducer<IntWritable, Text, Text, Text> {
		private int dimension;
		private int knn;

		private Configuration jobinfo;
		Text outputValue = new Text();
		Text outputKey = new Text();

		/** number of object pairs to be computed */
		static enum Counters {
			pairwise
		}

		protected void setup(Context context) throws IOException,
				InterruptedException {
			jobinfo = context.getConfiguration();
			dimension = jobinfo.getInt("dimension", 2);
			System.out.println("=============" + jobinfo.get("knn"));
			knn = jobinfo.getInt("knn", 3);
			System.out.println("dimension: " + dimension);
		}

		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Vector<FloatVector> R = new Vector<FloatVector>();
			int blockSize = 128 * KB;
			int cacheSize = 1024 * MB;
			ListParameterization spatparams = new ListParameterization();
			spatparams.addParameter(TreeIndex.CACHE_SIZE_ID, cacheSize);
			spatparams.addParameter(TreeIndex.PAGE_SIZE_ID, blockSize);
			// FloatVector is used for RStarTree
			RStarTree<FloatVector> rt = new RStarTree<FloatVector>(spatparams);

			// Save data to local file
			for (Text value : values) {
				// <fid, rid, coords, others>
				/** parse the object */
				String line = value.toString();
				int prepos, pos = line.indexOf(SQConfig.sepStrForRecord);
				int whichFile = Integer.valueOf(line.substring(0, pos));
				prepos = pos;
				pos = line.indexOf(SQConfig.sepStrForRecord, prepos + 1);
				// rid;
				int ridOfR = Integer.valueOf(line.substring(prepos + 1, pos));
				float[] coordsOfR = new float[dimension];
				// coordsOfR
				for (int i = 0; i < dimension; i++) {
					prepos = pos;
					pos = line.indexOf(SQConfig.sepStrForRecord, prepos + 1);
					if (pos == -1)
						pos = line.length();

					coordsOfR[i] = Float.valueOf(line
							.substring(prepos + 1, pos));
				}
				FloatVector o = null; // need a way to make it generic
				o = new FloatVector(coordsOfR);
				o.setID(ridOfR);

				if (whichFile == 0) {
					R.add(o);
				} else if (whichFile == 1) {
					rt.insert(o);
				} else {
					System.out.println("unknow file number");
					System.exit(-1);
				}
			}

			EuclideanDistanceFunction<FloatVector> dist = new EuclideanDistanceFunction<FloatVector>();
			for (FloatVector o : R) {
				List<DistanceResultPair<DoubleDistance>> ids = rt
						.kNNQuery(
								o,
								knn,
								(SpatialDistanceFunction<FloatVector, DoubleDistance>) dist);
				String lineOfOutput = "";
				int cnt = 0;
				for (DistanceResultPair<DoubleDistance> res : ids) {
					if (cnt == 0) {
						lineOfOutput += res.getID() + SQConfig.sepStrForIDDist
								+ res.getDistance().toString();
					} else {
						lineOfOutput += SQConfig.sepStrForKeyValue
								+ res.getID() + SQConfig.sepStrForIDDist
								+ res.getDistance().toString();
					}
					// limit the number of candidates to knn
					cnt++;
					if (cnt == knn)
						break;
				}
				outputValue.set(lineOfOutput);
				outputKey.set(Integer.toString(o.getID()));
				context.write(outputKey, outputValue);
			}
			context.getCounter(Counters.pairwise).increment(rt.distanceCalcs);
		} // reduce

	} // Reducer

	private void printUsage() {
		System.out.println("NPhase1 [-p <numberOfPartitions>] "
				+ "[-d <dimension>] [-k <knn>] " + "-i <input> -o <output>");
		ToolRunner.printGenericCommandUsage(System.out);
		System.out.println(-1);
	}

	/**
	 * The main driver for phase1 of H-BRJ algorithm. Invoke this method to
	 * submit the map/reduce job.
	 * 
	 * @throws IOException
	 *             When there is communication problems with the job tracker.
	 */
	public void run(String[] args) throws Exception {
		Configuration conf = new Configuration();

		/** set job parameter */
		Job job = new Job(conf, "BaselineRTree for KNN Join");
		job.setJarByClass(BaselineRtree.class);
		job.setMapperClass(BaselineRtreeMapper.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setReducerClass(BaselineRtreeReducer.class);

		int numberOfPartition = 2;
		int dim;
		int k;

		List<String> other_args = new ArrayList<String>();
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-p".equals(args[i])) {
					numberOfPartition = Integer.parseInt(args[++i]);
					job.getConfiguration().set("numberOfPartition",
							Integer.toString(numberOfPartition));
					System.out.println("numOfPartition: " + numberOfPartition);
				} else if ("-d".equals(args[i])) {
					dim = Integer.parseInt(args[++i]);
					job.getConfiguration().set("dimension",
							Integer.toString(dim));
					System.out.println("dimension: " + dim);
				} else if ("-k".equals(args[i])) {
					k = Integer.parseInt(args[++i]);
					job.getConfiguration().set("knn", Integer.toString(k));
					System.out.println("knn: " + k);
				} else if ("-i".equals(args[i])) {
					other_args.add(args[++i]);
					System.out.println("input path: "
							+ other_args.get(other_args.size() - 1));
				} else if ("-o".equals(args[i])) {
					other_args.add(args[++i]);
					System.out.println("output path: "
							+ other_args.get(other_args.size() - 1));
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

		job.setNumReduceTasks(numberOfPartition * numberOfPartition);
		if (other_args.size() != 2) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ other_args.size() + " instead of 2.");
			printUsage();
		}

		FileInputFormat.setInputPaths(job, new Path(other_args.get(0)));
		System.out.println("Add R and S to the input path");
		FileOutputFormat.setOutputPath(job, new Path(other_args.get(1)));

		long begin = System.currentTimeMillis();
		job.waitForCompletion(true);
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println(job.getJobName() + " takes " + second + " seconds");
	}

	public static void main(String[] args) throws Exception {
		BaselineRtree rs = new BaselineRtree();
		try {
			rs.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
} // RPhase1
