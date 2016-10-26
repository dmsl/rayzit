package sg.edu.nus.mda.simquery.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;

public class PartitionSummary {
	int numOfPivots;
	String input;

	public PartitionSummary(String input, int numOfPivots) {
		this.input = input;
		this.numOfPivots = numOfPivots;
	}

	public int[] readObjectSet() {
		// read objects for each partition
		int numOfObjects[] = new int[numOfPivots];
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);
			FSDataInputStream currentStream;
			BufferedReader currentReader;
			currentStream = fs.open(new Path(input));
			currentReader = new BufferedReader(new InputStreamReader(
					currentStream));
			String line;
			int i = 0;
			while ((line = currentReader.readLine()) != null) {
				if (line.isEmpty()) {
					throw new IOException("Record in the index cannot be empty");
				}
				String[] summary = line.split(SQConfig.sepStrForIndex);
				if (summary == null || i > numOfPivots) {
					throw new IOException("More number of pivots in the index!");
				}
				numOfObjects[i++] = Integer
						.valueOf(summary[summary.length - 1]);
			}

			currentReader.close();
			currentStream.close();
			fs.close();

			return numOfObjects;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * We output the summary of partitions: (1) minimum number of objects (2)
	 * maximum number of objects (3) avg. number of objects (4) dev.
	 */
	public void printSummary() {
		int minNumOfObjects = Integer.MAX_VALUE;
		int maxNumOfObjects = 0;
		int numOfObjects[] = readObjectSet();
		int i, sumOfObjects = 0;
		float avg, dev = 0f;

		for (i = 0; i < numOfPivots; i++) {
			if (minNumOfObjects > numOfObjects[i]) {
				minNumOfObjects = numOfObjects[i];
			}
			if (maxNumOfObjects < numOfObjects[i]) {
				maxNumOfObjects = numOfObjects[i];
			}
			sumOfObjects += numOfObjects[i];
		}
		avg = 1.0f * sumOfObjects / numOfPivots;

		for (i = 0; i < numOfPivots; i++) {
			dev += (numOfObjects[i] - avg) * (numOfObjects[i] - avg);
		}
		System.out.println("min: " + minNumOfObjects);
		System.out.println("max: " + maxNumOfObjects);
		System.out.println("sum:" + sumOfObjects + ", numOfPivots: "
				+ numOfPivots + ", avg: " + avg);
		System.out.println("dev: " + Math.sqrt(1.0f * dev / numOfPivots));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Configuration conf = new Configuration();
		new GenericOptionsParser(conf, args).getRemainingArgs();
		String input = conf.get(SQConfig.strMergeIndexOutput) + "summary.index1";
		int numOfPivots = conf.getInt(SQConfig.strNumOfPivots, 10000);
		/** split the input file into a set of file splits */
		long begin = System.currentTimeMillis();
		try {
			PartitionSummary ps = new PartitionSummary(input, numOfPivots);
			ps.printSummary();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println("MergeSummary takes " + second + " seconds");
	}
}
