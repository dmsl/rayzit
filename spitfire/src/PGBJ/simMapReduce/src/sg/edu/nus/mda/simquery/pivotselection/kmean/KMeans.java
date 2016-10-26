package sg.edu.nus.mda.simquery.pivotselection.kmean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import sg.edu.nus.mda.simquery.preprocess.SQConfig;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;


public class KMeans {
	private final static String sepStr = SQConfig.sepStrForRecord;
	double[][] samples;
	double[][] cluster;
	int dataSize;
	int sampleSize;
	int numOfClusters;
	int dim;
	boolean showDetails = true;
	KmeansCluster kc;

	public KMeans(int numOfClusters, int samplesSize, int dataSize, int dim) {
		this.numOfClusters = numOfClusters;
		this.sampleSize = samplesSize;
		this.dataSize = dataSize;
		this.dim = dim;
		samples = new double[samplesSize][dim];
		cluster = new double[numOfClusters][dim];
		if (sampleSize == dataSize) {
			showDetails = true;
		} else
			showDetails = false;
	}

	private Map<Integer, Boolean> getSampleIndex() {
		Map<Integer, Boolean> sampleIndex = new TreeMap<Integer, Boolean>();
		int i = 0;
		Random randomGen = new Random();
		int lineID;
		while (i < sampleSize) {
			lineID = randomGen.nextInt(dataSize);
			while (sampleIndex.containsKey(lineID)) {
				lineID = randomGen.nextInt(dataSize);
			}
			sampleIndex.put(lineID, false);
			i++;
		}
		return sampleIndex;
	}

	private void readData(String input) {
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);
			FSDataInputStream inputStream = fs.open(new Path(input));
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(inputStream));
			String tmp = null;
			int sampleID = 0;
			while ((tmp = br.readLine()) != null) {
				String[] tmpStr = new String[dim];
				tmpStr = tmp.split(sepStr);
				for (int d = 0; d < dim; d++) {
					samples[sampleID][d] = Double.parseDouble(tmpStr[d + 1]);
				}
				sampleID++;
			}
			br.close();
			inputStream.close();
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readData(String input, Map<Integer, Boolean> sampleIndex) {
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);
			FSDataInputStream inputStream = fs.open(new Path(input));
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(inputStream));
			String tmp = null;
			int lineID = 0;
			int sampleID = 0;
			while ((tmp = br.readLine()) != null) {
				if (!sampleIndex.containsKey(lineID)) {
					lineID++;
					continue;
				}
				String[] tmpStr = new String[dim];
				tmpStr = tmp.split(sepStr);
				for (int d = 0; d < dim; d++) {
					samples[sampleID][d] = Float.parseFloat(tmpStr[d+1]);
				}
				lineID++;
				sampleID++;
			}
			br.close();
			inputStream.close();
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(String input) {
		if (sampleSize == dataSize) {
			readData(input);
		} else if (sampleSize < dataSize) {
			Map<Integer, Boolean> sampleIndex = getSampleIndex();
			readData(input, sampleIndex);
		} else {
			System.err.println("sample size cannot be larger than data size");
			System.exit(-1);
		}
	}

	public void doClustering() {
		kc = new KmeansCluster(samples, numOfClusters);
		kc.train();
		cluster = kc.getCenters();
	}

	public void printCenter(String output) {
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);
			FSDataOutputStream outputStream = fs.create(new Path(output), true);
			BufferedWriter bw = null;
			bw = new BufferedWriter(new OutputStreamWriter(outputStream));

			int i, d;
			DecimalFormat df = new DecimalFormat("0.0");
			for (i = 0; i < numOfClusters; i++) {
				// we just add an invalid id for each pivot for the consistency
				String tmp = i + ",";
				for (d = 0; d < dim - 1; d++) {
					tmp += df.format(cluster[i][d]) + sepStr;
				}
				tmp += df.format(cluster[i][dim - 1]) + "\n";
				bw.write(tmp);
			}
			bw.close();
			outputStream.close();
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printCluster(String output) {
		Configuration conf = new Configuration();

		try {
			FileSystem fs = FileSystem.get(conf);
			FSDataOutputStream outputStream = fs.create(new Path(output), true);
			BufferedWriter bw = null;
			bw = new BufferedWriter(new OutputStreamWriter(outputStream));

			int[] clusterIDForObjects = kc.clusters;
			@SuppressWarnings("unchecked")
			Vector<Integer>[] clusterInfo = new Vector[numOfClusters];
			int i, j;
			for (i = 0; i < clusterIDForObjects.length; i++) {
				int clusterID = clusterIDForObjects[i];
				if (clusterInfo[clusterID] == null) {
					clusterInfo[clusterID] = new Vector<Integer>();
				}
				clusterInfo[clusterID].add(i);
			}
			for (i = 0; i < numOfClusters; i++) {
				Vector<Integer> v = clusterInfo[i];
				if (v == null)
					continue;
				int len = v.size();
				for (j = 0; j < len - 1; j++) {
					bw.write(v.get(j) + sepStr);
				}
				bw.write(v.get(len - 1) + "\n");

			}
			bw.close();
			outputStream.close();
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void print(String output_center, String output_cluster_info) {
		/** write out the clusters */
		printCenter(output_center);
		if (showDetails) {
			printCluster(output_cluster_info);
		}
	}

	public static void main(String[] args) {
		String input;
		String out_center;
		String out_cluster_details;
		int numOfClusters;
		int sampleSize;
		int dataSize;
		int dim;

		Configuration conf = new Configuration();
		new GenericOptionsParser(conf, args).getRemainingArgs();

		input = conf.get(SQConfig.dataset) + Path.SEPARATOR + "0.data";
		out_center = conf.get(SQConfig.strPivotInput);
		numOfClusters = conf.getInt(SQConfig.strNumOfPivots, -1);
		sampleSize = conf.getInt(SQConfig.strSampleSize, -1);
		dataSize = conf.getInt(SQConfig.strDatasetSize, -1);
		dim = conf.getInt(SQConfig.strDimExpression, -1);
		out_cluster_details = out_center + ".detail";
		
		
		long begin = System.currentTimeMillis();
		KMeans kmeans = new KMeans(numOfClusters, sampleSize, dataSize, dim);
		kmeans.init(input);
		kmeans.doClustering();
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println("PivotSelection takes " + second + " seconds");
		kmeans.print(out_center, out_cluster_details);
	}
}
