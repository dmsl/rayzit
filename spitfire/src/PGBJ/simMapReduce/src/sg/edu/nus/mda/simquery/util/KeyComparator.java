package sg.edu.nus.mda.simquery.util;

import java.util.Comparator;

public class KeyComparator implements Comparator<String> {

	@Override
	/**
	 * Compare partition, and distance
	 * 
	 * */
	public int compare(String s1, String s2){
		int p1, p2;
		double d1, d2;

		String[] tmp1 = s1.split(",");
		String[] tmp2 = s2.split(",");
		
		p1 = Integer.valueOf(tmp1[0]);
		p2 = Integer.valueOf(tmp2[0]);

		if (p1 > p2) 
			return 1;
		else if (p1 < p2)
			return -1;

		d1 = Double.valueOf(tmp1[1]);
		d2 = Double.valueOf(tmp2[1]);

		if (d1 > d2)
			return 1;
		else if (d1 < d2)
			return -1;
		else return 0;
	}
}
