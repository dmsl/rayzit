/**
  * Spitfire: A distributed main-memory algorithm for AkNN query processing
  *
  * Copyright (c) 2015 Data Management Systems Laboratory, University of Cyprus
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.
  * If not, see http://www.gnu.org/licenses/.
  *
 */
/**
 * Created by Constantinos Costa.
 * Copyright (c) 2015 DMSL. All rights reserved
 */
package datastructure;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import proximity.datastructures.User;
import utils.Trigonometry;
import benchmark.AllBenchmarking;
import edu.rit.mp.ObjectBuf;
import edu.rit.pj.Comm;
import edu.rit.pj.IntegerForLoop;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import edu.rit.util.Range;

public class AllDistributedSpiralGrid {

	public static final boolean BENCH = false;// -90:90

	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180

	public static int latStride = 0;
	public static int lonStride = 0;
	public static int lat_bound = 0;
	public static int lon_bound = 0;
	static int k;

	static Cell[][] m_Grid = null;

	// Shared variables.

	// World communicator.
	static Comm world;
	static int size;
	static int rank;

	// Fill the Grid.
	static Range[] r_ranges;
	static Range r_myrange;
	public static int r_mylb = 0;
	public static int r_myub = 360;
	// Communication buffers.
	static ObjectBuf<Cell>[] slices;
	static ObjectBuf<Cell> myslice;

	// Fill the Grid.
	static Range[] c_ranges;
	static Range c_myrange;
	public static int c_mylb = 0;
	public static int c_myub = 180;

	private static int N = 2;

	// Cell size = spacesize / # of users
	public AllDistributedSpiralGrid(int numOfusers, int k, int cellSize) {

		switch (cellSize) {
		case Grid.N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log(numOfusers));
			break;
		case Grid.SQRT_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.sqrt(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.sqrt(numOfusers));
			break;
		case Grid.LN_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log(numOfusers));
			break;
		case Grid.LOG_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log10(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log10(numOfusers));
			break;
		default:
			latStride = (int) Math.ceil((double) LAT_BOUND / 1);
			lonStride = (int) Math.ceil((double) LON_BOUND / 1);
			break;
		}

		if (latStride == 0)
			latStride = 1;

		if (lonStride == 0)
			lonStride = 1;

		lat_bound = (int) Math.ceil((double) LAT_BOUND / latStride);
		lon_bound = (int) Math.ceil((double) LON_BOUND / lonStride);

		// System.out.println("Grid"+lonStride+","+latStride);
		// Create the cell with empty handle to Cell object
		m_Grid = new Cell[lon_bound][lat_bound];

		AllDistributedSpiralGrid.k = k;
	}

	public Cell getCell(User n) {
		return m_Grid[(int) (n.lon / lonStride)][(int) (n.lat / latStride)];
	}

	public static void initRanges() throws IOException {
		if (BENCH) {

			// Initialize arguments
			String[] args = new String[0];
			// // Initialize world communicator.
			Comm.init(args);
			world = Comm.world();
		} else
			world = AllBenchmarking.world;

		size = world.size();
		rank = world.rank();

		if (Math.sqrt(size) > N)
			N = (int) Math.sqrt(size);

		if (size > N) {
			r_ranges = new Range(0, lon_bound - 1).subranges(size / N);
			c_ranges = new Range(0, lat_bound - 1).subranges(N);
		} else {
			r_ranges = new Range(0, lon_bound - 1).subranges(size);
			c_ranges = new Range(0, lat_bound - 1).subranges(size);
		}

		r_myrange = r_ranges[rank / N];
		c_myrange = c_ranges[rank % N];

		// System.out.println(r_myrange.toString() + " ");
		// System.out.println(c_myrange.toString() + " ");

		r_mylb = r_myrange.lb();
		r_myub = r_myrange.ub();

		c_mylb = c_myrange.lb();
		c_myub = c_myrange.ub();

	}

	public void insertCell(List<User> neighborsList)
			throws Exception {

		/**
		 * IMPORTANT THE initRanges is call for main benchmarking class
		 */

		if (rank != 0) {
			for (int i = r_mylb; i <= r_myub; i++) {
				for (int j = c_mylb; j <= c_myub; j++) {
					/* compute minD and maxD of user to cell */
					// Create the cell if it is null
					m_Grid[i][j] = new Cell(k);
				}
			}
		} else {
			for (int i = 0; i < lon_bound; i++) {
				for (int j = 0; j < lat_bound; j++) {
					/* compute minD and maxD of user to cell */
					// Create the cell if it is null
					m_Grid[i][j] = new Cell(k);
				}
			}
		}

		// ///////////////////////////////////////////////////////////////////////////
		// Set up communication buffers.
		// ///////////////////////////////////////////////////////////////////////////
		slices = ObjectBuf.patchBuffers(m_Grid, r_ranges, c_ranges);
		myslice = ObjectBuf.patchBuffer(m_Grid, r_myrange, c_myrange);
		// ///////////////////////////////////////////////////////////////////////////
		// Phase one
		// ///////////////////////////////////////////////////////////////////////////
/*
		double co_r_mylb = r_mylb * lonStride;
		double co_c_mylb = c_mylb * latStride;
		double co_r_myub = r_myub * lonStride;
		double co_c_myub = c_myub * latStride;

		List<Neighbor> new_neighborsList = new LinkedList<Neighbor>();
		for (Neighbor neighbor : neighborsList) {
			// Check if the user is in the range
			if ((co_r_mylb <= neighbor.lon && co_r_myub >= neighbor.lon
					&& co_c_mylb <= neighbor.lat && co_c_myub >= neighbor.lat))
				new_neighborsList.add(neighbor);
		}
		neighborsList.clear();
		neighborsList.addAll(new_neighborsList);*/

		// ///////////////////////////////////////////////////////////////////////////
		// Phase Two
		// ///////////////////////////////////////////////////////////////////////////
		final List<User> p_neighborsList = neighborsList;
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		new ParallelTeam().execute(new ParallelRegion() {
			public void run() throws Exception {
				execute(0, p_neighborsList.size() - 1, new IntegerForLoop() {
					public void run(int first, int last) {
						for (int i = first; i <= last; ++i) {

							// for (int i = 0; i < temp.size(); i++) {
							User n = p_neighborsList.get(i);
							// Place the Neighbor into the computed cell
							placeNeighborInCell(n, -1);
						}
					}

				});
			}
		});

		// Gather all matrix row slices into process 0.
		world.gather(0, myslice, slices);
		// Gather all matrix col slices into process 0.
		// world.gather(0, c_myslice, c_slices);
		// Due to the MPI environment we should kill all the process after the
		// calculation
		if (rank != 0)
			System.exit(0);

	}

	private static void placeNeighborInCell(User n_in, int b) {

		double x1, y1;
		double x2, y2;
		int startx = (int) (n_in.lon / lonStride);
		int starty = (int) (n_in.lat / latStride);
		if (b >= 0) {
			int r_middle = (int) Math.round((r_mylb + r_myub) / 2.0);
			int c_middle = (int) Math.round((c_mylb + c_myub) / 2.0);
			if (startx < r_middle && starty < c_middle) {
				startx = r_mylb;
				starty = c_mylb;
			} else if (startx < r_middle && starty > c_middle) {
				startx = r_mylb;
				starty = c_myub;
			} else if (startx > r_middle && starty < c_middle) {
				startx = r_myub;
				starty = c_mylb;
			} else {
				startx = r_myub;
				starty = c_myub;
			}

		}

		int max;
		if (size > 0)
			max = Math.max(2 * lon_bound, 2 * lat_bound) + 1;
		else
			max = Math.max(2 * (r_myub - r_mylb), 2 * (c_myub - c_mylb)) + 1;

		int x = 0;
		int y = 0;
		int dx = 0;
		int dy = -1;
		boolean spiralEmptyCycle = false;
		
		for (int l = 0; l < Math.pow(max, 2); l++) {

			int my_x = (startx + x);
			int my_y = (starty + y);
	
			if (my_x >= 0 && my_y >= 0 && my_x < lon_bound && my_y < lat_bound) {

				if ((my_x >= r_mylb && my_x <= r_myub)
						&& (my_y >= c_mylb && my_y <= c_myub)) {

					// check each cycle
					// Bottom diagonal is the end of each cycle
					if (x == y && my_x <= startx && my_y <= starty) {
						if (spiralEmptyCycle) {
							// System.out.println("Cycle " + my_x + ","
							// + my_y);
							break;
						}
						// reinitialize the flag
						spiralEmptyCycle = true;
					}

					/* compute minD and maxD of user to cell */
					// Create the cell if its null
					User n = new User(n_in.key, n_in.lon, n_in.lat);

					synchronized (m_Grid[my_x][my_y]) {
						if (m_Grid[my_x][my_y] == null)
							m_Grid[my_x][my_y] = new Cell(k);
					}
					// Double minD =
					// currReport.getUser().getObfLocation().distance(
					// currCell.getCenter() ) - currCell.getRadius();

					// Double maxD = minD + 2 * currCell.getRadius();

					x1 = my_x * lonStride;
					y1 = my_y * latStride;
					x2 = (my_x + 1) * lonStride;
					y2 = (my_y + 1) * latStride;

					// System.out.println("Grid.insertCell(" + x1 + "," + y1 +
					// ":"
					// + x2 + "," + y2 + ")");
					double maxD = 0.0;
					double minD = Trigonometry.pointToRectangleBoundaryMinDistance(
							n.lon, n.lat, x1, y1, x2, y2);

					if (minD != 0.0) {
						maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
								n.lat, x1, y1, x2, y2);
					}

					// System.out.println((n.lon+180)+","+(n.lat+90)+"=="+((i +
					// 1) *
					// lonStride / 2.0) + ","
					// + ((j + 1) * latStride / 2.0));
					/* compute minD and maxD of user to cell */

					// TODO:FIX set the n.distance and n.maxD
					n.minD = minD;
					n.maxD = maxD;

					synchronized (m_Grid[my_x][my_y]) {
						spiralEmptyCycle = !m_Grid[my_x][my_y].insert(n, minD,
								minD == 0.0) && spiralEmptyCycle;
					}
				}
			}
			if (x == y || (x < 0 && x == -y) || (x > 0 && x == (1 - y))) {
				int temp = dx;
				dx = -dy;
				dy = temp;
			}
			x = x + dx;
			y = y + dy;

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AllDistributedSpiralGrid my_grid = new AllDistributedSpiralGrid(1000,
				10, Grid.LOG_N);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
