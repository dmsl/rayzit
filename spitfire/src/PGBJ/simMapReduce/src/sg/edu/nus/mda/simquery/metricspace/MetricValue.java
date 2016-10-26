package sg.edu.nus.mda.simquery.metricspace;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;


/**
 * This class is only used as the value. Not suitable for key
 * 
 * @author luwei
 * 
 */
@SuppressWarnings("rawtypes")
public class MetricValue implements WritableComparable {
	public int bufferLen;
	String bufferOfObjects;

	public MetricValue() {
		bufferLen = 0;
	}

	public int length() {
		return bufferLen;
	}

	public void set(String buffer) {
		bufferOfObjects = buffer;
		bufferLen = buffer.length();
	}

	public String toString() {
		return bufferOfObjects;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBytes(bufferOfObjects);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
