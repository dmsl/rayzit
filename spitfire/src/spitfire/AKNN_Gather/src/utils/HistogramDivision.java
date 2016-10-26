/**
 * Copyright (c) 2016 Data Management Systems Laboratory, University of Cyprus
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/
/**
 * Created by Constantinos Costa.
 * Copyright (c) 2014 DMSL. All rights reserved
 */
package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import couchbase.connector.CouchBaseData;

import edu.rit.util.Range;

import proximity.datastructures.User;

public class HistogramDivision {

	// Maximum division equality
	public static final int lonStride = 1;
	public static final int latStride = 1;
	public static final int LAT_BOUND = 180;// -90:90 + 1 for the border
											// candidates
	public static final int LON_BOUND = 360;// -180:180 + 1 for the border
											// candidates
	public static Range[] r_ranges;
	public static Range[] c_ranges;

	public static int[] y_sum_buckets;
	public static int[] x_sum_buckets;

	public static void createEquidepthGrid(List<User> users, int servers) {

		int N = users.size();
		int x_partitions = (int) Math.floor(Math.sqrt(servers));
		int y_partitions = (int) Math.ceil(Math.sqrt(servers));

		//Create the partition for 3 Servers
		if (servers == 3) {
			x_partitions = 3;
			y_partitions = 1;
		}

		int[][] grid;

		// Initialize the grid
		grid = new int[LAT_BOUND + 1][LON_BOUND + 1];

		// Initialize ranges
		c_ranges = new Range[x_partitions];
		r_ranges = new Range[x_partitions * y_partitions];

		// Initialize the partial sums for column and rows
		x_sum_buckets = new int[x_partitions];
		y_sum_buckets = new int[x_partitions * y_partitions];

		// #X-partitions
		// #Y-partitions

		// Add all users into the cell
		for (User user : users) {
			grid[(int) user.lat][(int) user.lon]++;
		}

		// ///////////////////////////////////////
		// Phase 1
		// ///////////////////////////////////////
		// Compute histogram on column buckets
		// ///////////////////////////////////////

		// Loop until i < #X-partitions do
		/*
		 * While part_sum of bucket< N/#X-partitions do 
		 * add another bucket 
		 * done
		 * 
		 * if part_sum N/#X-partitions + 1/2 last bucket then
		 * remove last bucket
		 * fi
		 */
		// save the partition column ranges + index + partial/bucket sum
		// done

		// initialize lower and upper bound
		int lb = 0;
		int ub = 0;

		double N_per_Partition = (double) N / (double) x_partitions;

		for (int p = 0; p < x_partitions - 1; p++) {

			int bucket = 0;

			for (int j = lb; j < LON_BOUND
					&& x_sum_buckets[p] < N_per_Partition; j++, ub++) {
				bucket = 0;
				for (int i = 0; i < LAT_BOUND; i++) {
					bucket += grid[i][j];
				}
				x_sum_buckets[p] += bucket;

			}
			// Remove last bucket
			if (x_sum_buckets[p] > (N_per_Partition + 0.5 * bucket)) {
				x_sum_buckets[p] -= bucket;
				ub--;
			}

			c_ranges[p] = new Range(lb, ub);

			lb = ub;
		}

		// Last partition should get remaining cells
		int bucket;

		for (int j = lb; j < LON_BOUND; j++, ub++) {
			bucket = 0;
			for (int i = 0; i < LAT_BOUND; i++) {
				bucket += grid[i][j];
			}
			x_sum_buckets[x_partitions - 1] += bucket;

		}

		c_ranges[x_partitions - 1] = new Range(lb, ub);

		// ///////////////////////////////////////
		// Phase 2
		// ///////////////////////////////////////
		// Compute histogram on row buckets
		// ///////////////////////////////////////

		// Loop until i < #X-partitions do

		// Loop until i < #y-partitions do
		/*
		 * While part_sum of bucket< N-X-partition/#y-partitions do 
		 * add another bucket 
		 * done
		 * 
		 * if part_sum N-X-partition/#y-partitions + 1/2 last bucket then
		 * remove last bucket
		 * fi
		 */
		// save the partition row ranges + index + partial/bucket sum
		// done

		// done

		// For each partition
		for (int p_x = 0; p_x < x_partitions; p_x++) {

			// initialize lower and upper bound
			lb = 0;
			ub = 0;

			// Compute histogram for y-axis
			for (int p = 0; p < y_partitions - 1; p++) {

				int p_y = p + p_x * y_partitions;

				bucket = 0;

				// For each row in partition x
				for (int i = lb; i < LAT_BOUND
						&& y_sum_buckets[p_y] < x_sum_buckets[p_x]
								/ y_partitions; i++, ub++) {

					bucket = 0;
					for (int j = c_ranges[p_x].lb(); j <= c_ranges[p_x].ub()
							&& j < LON_BOUND; j++) {
						bucket += grid[i][j];
					}
					y_sum_buckets[p_y] += bucket;

				}
				// Remove last bucket
				if (y_sum_buckets[p_y] > (x_sum_buckets[p_x] / y_partitions + 0.5 * bucket)) {
					y_sum_buckets[p_y] -= bucket;
					ub--;
				}

				r_ranges[p_y] = new Range(lb, ub);

				lb = ub;
			}

			// Last partition should get remaining cells

			for (int i = lb; i < LAT_BOUND; i++, ub++) {

				bucket = 0;
				for (int j = c_ranges[p_x].lb(); j <= c_ranges[p_x].ub()
						&& j < LON_BOUND; j++) {
					bucket += grid[i][j];
				}
				y_sum_buckets[y_partitions - 1 + p_x * y_partitions] += bucket;

			}

			r_ranges[y_partitions - 1 + p_x * y_partitions] = new Range(lb, ub);

		}

	}

	public static void createEquiwidthGrid(List<User> users, int servers) {
		int x_partitions = (int) Math.floor(Math.sqrt(servers));
		int y_partitions = (int) Math.ceil(Math.sqrt(servers));

		// Initialize ranges
		c_ranges = new Range[x_partitions];
		r_ranges = new Range[x_partitions * y_partitions];

		// Initialize the partial sums for column and rows
		x_sum_buckets = new int[x_partitions];
		y_sum_buckets = new int[x_partitions * y_partitions];

		// #X-partitions
		// #Y-partitions

		int lb = 0;
		int ub = 0;
		for (int i = 0; i < x_partitions; i++) {
			ub = LON_BOUND / (x_partitions - i);
			c_ranges[i] = new Range(lb, ub);
			lb = ub;
		}

		for (int i = 0; i < x_partitions; i++) {
			lb = 0;
			ub = 0;
			for (int j = 0; j < y_partitions; j++) {
				ub = (LAT_BOUND / (y_partitions - j));
				r_ranges[i * x_partitions + j] = new Range(lb, ub);
				lb = ub;
			}
		}
		// Add all users into the cell
		for (User user : users) {
			int i = (int) ((user.lon - 0.0000001) / ((LON_BOUND / x_partitions)));
			int j = (int) ((user.lat - 0.0000001) / ((LAT_BOUND / (x_partitions * y_partitions))));
			x_sum_buckets[i]++;
			y_sum_buckets[j]++;
			// System.out.println("i=" + i + ",j=" + j);
		}

	}

	private static ArrayList<User> readUsersFromfile(String filename) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		ArrayList<User> list = new ArrayList<User>();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));
			int i = 0;
			while ((sCurrentLine = br.readLine()) != null) {

				String[] split = sCurrentLine.split(",");
				Double lon = Double.parseDouble(split[1]) + 180;
				Double lat = Double.parseDouble(split[2]) + 90;
				list.add(new User((i++) + "", lon, lat));

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}

	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CouchBaseData.setLoggingOff();
		// List<User> users = CouchBaseData.getData();
		// String filename =
		// "C:\\Users\\Costantinos\\Desktop\\data\\newGeolife\\geolife-Beijing.1000000.data";
		//String filename = "C:\\Users\\Costantinos\\Desktop\\data\\random\\1000k.data";
		// String filename =
		// "C:\\Users\\Costantinos\\Desktop\\data\\newOldenburg\\oldenburg.1000000.data";
		// String filename = "unique_log.txt";
		 String filename =
		 "C:\\Users\\Costantinos\\Desktop\\data\\rayzit\\rayzit.txt";

		List<User> users = readUsersFromfile(filename);
		long start = System.currentTimeMillis();
		HistogramDivision.createEquiwidthGrid(users, 9);
		long end = System.currentTimeMillis();
		System.out.println("HistogramDivision time:" + (end - start));

		// for (Range r : r_ranges) {
		// System.out.print(r + " ");
		// }
		// System.out.println();
		//
		// for (Range c : c_ranges) {
		// System.out.print(c + " ");
		// }

		for (int i = 0; i < y_sum_buckets.length; i++) {
			System.out.println("s" + (i + 1) + "\t" + y_sum_buckets[i]);
		}

		
		
		
		// Exit
		System.exit(0);

		start = System.currentTimeMillis();
		HistogramDivision.createEquidepthGrid(users, 9);
		end = System.currentTimeMillis();
		System.out.println("HistogramDivision time:" + (end - start));

		// for (Range r : r_ranges) {
		// System.out.print(r + " ");
		// }
		// System.out.println();
		//
		// for (Range c : c_ranges) {
		// System.out.print(c + " ");
		// }

		for (int i = 0; i < y_sum_buckets.length; i++) {
			System.out.println(i + "\t" + y_sum_buckets[i]);
		}

		System.out.println();
	}
}
