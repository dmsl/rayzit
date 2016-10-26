package sg.edu.nus.mda.simquery.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * transform the record format We assume the record format before transformation
 * is: dim1 dim2 dim3 ... dimN We transform it to : rid,dim1,dim2,dim3,..,dimN
 * 
 * @input: the path of the input file
 * 
 *         no output parameter
 * 
 * @author luwei
 * 
 */
public class AddRId {
	private final static int START_ID = 1;
	String input;

	public AddRId(String input) {
		this.input = input;
	}

	public void excute() throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path(SQConfig.configPath1));
		conf.addResource(new Path(SQConfig.configPath2));
		conf.addResource(new Path(SQConfig.configPath3));
	   
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(input);
//		Path working= new Path("hdfs://localhost:54310/");
//		fs.setWorkingDirectory(working);
		
		System.out.println(fs.getWorkingDirectory());
		
		if (!fs.exists(path))
			  printAndExit("Input file not found: "+path.toString());
			if (!fs.isFile(path))
			  printAndExit("Input should be a file: "+path.toString());
		
		
		FSDataInputStream is = fs.open(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String tmpOutput = path.getParent() + Path.SEPARATOR + "tmp-0";
		FSDataOutputStream os = fs.create(new Path(tmpOutput), true);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		int rid = START_ID;
		String line;

		while ((line = br.readLine()) != null) {
			// replace space by comma
			line = line.trim();
			line = line.replaceAll(" ", ",");
			bw.write(/*rid + "," + */line + "\n");
			rid++;
		}
		bw.close();
		os.close();
		br.close();
		is.close();
		fs.close();

		// overwrite input by tmpOutput
		Process p;
		// EDITED String cmd = "hadoop fs -rm -f " + input;

		String cmd = "/usr/local/hadoop/bin/hadoop fs -rm " + input;
		p = Runtime.getRuntime().exec(cmd);
		try {
			// wait for the process to finish excuting
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cmd = "/usr/local/hadoop/bin/hadoop fs -mv " + tmpOutput + " " + input;
		Runtime.getRuntime().exec(cmd);
	}

	private void printAndExit(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		if (args.length != 1) {
			System.out.println("format: file_path");
			System.exit(-1);
		}
		AddRId addRId = new AddRId(args[0]);
		try {
			addRId.excute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
	}

}
