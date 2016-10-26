package sg.edu.nus.mda.simquery.preprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * partition the input director (including two files) into a set of file splits specified by a output directory
 * 
 * @input: input directory
 * @input: number of file splits
 * @output: output directory
 * 
 * @author luwei
 * 
 */
public class MixJoinData {
	int numOfSplit;
	int dim;

	public MixJoinData(int numOfSplit, int dim) {
		this.numOfSplit = numOfSplit;
		this.dim = dim;
	}

	public void splitFile(String input, String output_dir) throws IOException {
		Configuration conf = new Configuration();
	    conf.addResource(new Path(SQConfig.configPath1));
	    conf.addResource(new Path(SQConfig.configPath2));
	    conf.addResource(new Path(SQConfig.configPath3));	    
   
		FileSystem fs = FileSystem.get(conf);
		FileStatus[] status = fs.listStatus(new Path(input));
		if (status == null || status.length != 2) {
			throw new IOException("Only two input files are supported!");
		}
		FSDataInputStream[] inputStream = new FSDataInputStream[2];
		BufferedReader[] currentReader = new BufferedReader[2];

		/** read input dir */
		inputStream[0] = fs.open(status[0].getPath());
		inputStream[1] = fs.open(status[1].getPath());
		currentReader[0] = new BufferedReader(new InputStreamReader(
				inputStream[0]));
		currentReader[1] = new BufferedReader(new InputStreamReader(
				inputStream[1]));
		FSDataOutputStream[] outputStream = new FSDataOutputStream[numOfSplit];

		for (int i = 0; i < numOfSplit; i++) {
			String filename = output_dir + "/" + i;
			Path path = new Path(filename);
			outputStream[i] = fs.create(path, true);
		}

		for (int k = 0; k < 2; k++) {
			String line;
			int iterator;
			Random random = new Random();
			while ((line = currentReader[k].readLine()) != null) {
				try{
					int pos = line.indexOf(SQConfig.sepStrForRecord, 0);
					for (int l = 0; l < dim; l ++) {
						pos = line.indexOf(SQConfig.sepStrForRecord, pos + 1);
					}
					if (pos == -1)
						pos = line.length();
					iterator = random.nextInt(numOfSplit);
					outputStream[iterator].writeBytes(k + "," + line.substring(0, pos) + "\n");
				} catch(Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			currentReader[k].close();
		}
		for (int i = 0; i < numOfSplit; i++) {
			outputStream[i].close();
		}
		fs.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Configuration conf = new Configuration();
	    conf.addResource(new Path(SQConfig.configPath1));
	    conf.addResource(new Path(SQConfig.configPath2));
	    conf.addResource(new Path(SQConfig.configPath3));
	    

		new GenericOptionsParser(conf, args).getRemainingArgs();
		String input = conf.get(SQConfig.dataset);
		String output = conf.get(SQConfig.dataSplitInput);
		/** split the input file into a set of file splits */
		int numOfSplit = conf.getInt(SQConfig.strNumOfGroups, 1);
		int dim = conf.getInt(SQConfig.strDimExpression, 2);
		MixJoinData fs = new MixJoinData(numOfSplit, dim);
		long begin = System.currentTimeMillis();
		try {
			fs.splitFile(input, output);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long second = (end - begin) / 1000;
		System.err.println("MixJoinData takes " + second + " seconds");
	}
}
