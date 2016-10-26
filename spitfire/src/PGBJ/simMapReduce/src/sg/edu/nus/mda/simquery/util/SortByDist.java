package sg.edu.nus.mda.simquery.util;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("rawtypes")
public class SortByDist implements Comparable {
	public int id;
	public float dist;

	public SortByDist(int pid, float dist) {
		this.id = pid;
		this.dist = dist;
	}

	public String toString() {
		return "[pid = " + id + ", dist = " + dist + "]";
	}

	@Override
	public int compareTo(Object o) {
		SortByDist other = (SortByDist) o;
		if (other.dist > this.dist)
			return -1;
		else if (other.dist < this.dist)
			return 1;
		else
			return 0;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		ArrayList<SortByDist> l = new ArrayList<SortByDist>();

		l.add(new SortByDist(3, 2.0f));

		l.add(new SortByDist(3, 1.0f));

		l.add(new SortByDist(3, 4.0f));

		System.out.println("before sort");

		for (int i = 0; i < l.size(); i++) {

			System.out.println(l.get(i));

		}

		Collections.sort(l);

		System.out.println("after sort");

		for (int i = 0; i < l.size(); i++) {
			System.out.println(l.get(i));

		}

	}
}
