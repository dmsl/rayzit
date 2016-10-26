package sg.edu.nus.mda.simquery.metricspace;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

import sg.edu.nus.mda.simquery.preprocess.SQConfig;

public class MetricDataInputFormat extends
		FileInputFormat<MetricKey, MetricValue> {
	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}

	@Override
	public RecordReader<MetricKey, MetricValue> createRecordReader(
			InputSplit inputsplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new MetricRecordReader();
	}

	public static class MetricRecordReader extends
			RecordReader<MetricKey, MetricValue> {
		private LineReader in;
		private Text line;
		public MetricKey metricKey;
		public MetricValue metricValue;
		private long pos;
		private long end;

		public void initialize(InputSplit genericSplit,
				TaskAttemptContext context) throws IOException {
			FileSplit split = (FileSplit) genericSplit;
			Configuration job = context.getConfiguration();
			Path file = split.getPath();
			FileSystem fs = file.getFileSystem(job);
			FSDataInputStream filein = fs.open(file);
			in = new LineReader(filein, job);
			line = new Text();
			metricKey = new MetricKey();
			metricValue = new MetricValue();
			pos = 0;
			end = split.getLength();
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			int linesize = in.readLine(line);
			if (linesize == 0)
				return false;

			String strLine = line.toString();
			int startPosOfValue = strLine.indexOf(SQConfig.sepStrForKeyValue);
			String strNextKey = strLine.substring(0, startPosOfValue);
			String[] strANextKey = strNextKey.split(SQConfig.sepStrForIndex);
			metricKey.whichFile = Integer.valueOf(strANextKey[0]);
			metricKey.pid = Integer.valueOf(strANextKey[1]);
			metricKey.dist = Float.valueOf(strANextKey[2]);
			metricValue.set(strLine.substring(startPosOfValue + 1));
			pos += linesize;

			return true;
		}

		@Override
		public MetricKey getCurrentKey() throws IOException,
				InterruptedException {
			return metricKey;
		}

		@Override
		public MetricValue getCurrentValue() throws IOException,
				InterruptedException {
			return metricValue;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return Math.min(1.0f, 1.0f * pos / end);
		}

		@Override
		public void close() throws IOException {
			if (in != null) {
				in.close();
			}
		}
	}

}
