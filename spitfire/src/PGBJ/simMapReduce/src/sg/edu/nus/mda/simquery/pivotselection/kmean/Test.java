package sg.edu.nus.mda.simquery.pivotselection.kmean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Test {
	float[] query = { 3004, 128, 12, 67, 11, 2654, 239, 233, 122, 600 };

	public static class ASCPQ<T> implements Comparator<T> {
		public int compare(Object o1, Object o2) {
			float v1 = (Float) o1;
			float v2 = (Float) o2;

			if (v1 > v2)
				return -1;
			else if (v1 == v2)
				return 0;
			else
				return 1;
		}
	}

	public void answerKNNQuery(String input, int K) {
		PriorityQueue<Float> pq = new PriorityQueue<Float>(K,
				new ASCPQ<Float>());
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(input)));
			String tmp;
			String[] array;
			int i;
			float value;

			while ((tmp = br.readLine()) != null) {
				array = tmp.split(" ");
				value  = 0;
				for (i = 0; i < array.length; i++) {
					float dimValue = Float.valueOf(array[i]);
					value += (query[i] - dimValue) * (query[i] - dimValue);
				}
				value = (float)Math.sqrt(value);
				if (pq.size() < K) {
					pq.add(value);
				} else {
					if (pq.peek() > value) {
						pq.remove();
						pq.add(value);
					}
				}
			}
			br.close();
			while (pq.size() > 0){
				System.out.println(pq.remove());
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test test = new Test();
		test.answerKNNQuery("E:\\paper\\source code\\dataset\\vector\\10-1.data", 10);
	}

}
