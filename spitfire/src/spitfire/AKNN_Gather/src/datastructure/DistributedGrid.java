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
package datastructure;

import java.io.IOException;
import java.util.List;

import proximity.datastructures.User;
import utils.Trigonometry;
import benchmark.Benchmarking;
import edu.rit.mp.ObjectBuf;
import edu.rit.pj.Comm;
import edu.rit.util.Range;

public class DistributedGrid {

	public static final boolean BENCH = false;// -90:90

	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180

	private int latStride = 0;
	public static int lonStride = 0;
	public static int lat_bound = 0;
	public static int lon_bound = 0;
	int k;

	Cell[][] m_Grid = null;

	// Shared variables.

	// World communicator.
	static Comm world;
	static int size;
	static int rank;
	public static int N = 2;
	
	// Fill the Grid.
	static Range[] r_ranges;
	public static Range r_myrange;
	static int r_mylb;
	static int r_myub;
	// Communication buffers.
	static ObjectBuf<Cell>[] slices;
	static ObjectBuf<Cell> myslice;

	// Fill the Grid.
	static Range[] c_ranges;
	public static Range c_myrange;
	static int c_mylb;
	static int c_myub;

	// Cell size = spacesize / # of users
	public DistributedGrid(int numOfusers, int k, int cellSize) {
		
		switch (cellSize) {
		case Grid.N:
			latStride = (int) Math.ceil((double) LAT_BOUND / Math.log(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND / Math.log(numOfusers));
			break;
		case Grid.SQRT_N:
			latStride = (int) Math.ceil((double) LAT_BOUND / Math.sqrt(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND / Math.sqrt(numOfusers));
			break;
		case Grid.LN_N:
			latStride = (int) Math.ceil((double) LAT_BOUND / Math.log(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND / Math.log(numOfusers));
			break;
		case Grid.LOG_N:
			latStride = (int) Math.ceil((double) LAT_BOUND / Math.log10(numOfusers));
			lonStride = (int) Math.ceil((double) LON_BOUND / Math.log10(numOfusers));
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

		this.k = k;
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
			world = Benchmarking.world;

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
	
	public void insertCell(final List<User> neighborsList)
			throws IOException {

		initRanges();
		
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

		// Set up communication buffers.
		slices = ObjectBuf.patchBuffers(m_Grid, r_ranges, c_ranges);
		myslice=ObjectBuf.patchBuffer(m_Grid, r_myrange, c_myrange);


		// TODO Auto-generated method stub
		for (int i = r_mylb; i <= r_myub; i++) {
			for (int j = c_mylb; j <= c_myub; j++) {
				/* compute minD and maxD of user to cell */
				for (int l = 0; l < neighborsList.size(); l++) {
					User n = neighborsList.get(l);
						// Place the Neighbor into the computed cell
						placeNeighborInCell(i, j, n);
				}
			}
		}


		// Gather all matrix row slices into process 0.
	//	 world.gather(0, myslice, slices);
		// Gather all matrix col slices into process 0.
		// world.gather(0, c_myslice, c_slices);
		// Due to the MPI environment we should kill all the process after the
		// calculation
		if (rank != 0)
			System.exit(0);

	}

	private void placeNeighborInCell(int i, int j, User n_in) {
		double x1 = i * lonStride;
		double y1 = j * latStride;
		double x2 = (i + 1) * lonStride;
		double y2 = (j + 1) * latStride;
		User n = new User(n_in.key, n_in.lon, n_in.lat);
		// System.out.println("Grid.insertCell(" + x1 + "," + y1 + ":"
		// + x2 + "," + y2 + ")");

		double maxD = 0.0;
		double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon, n.lat,
				x1, y1, x2, y2);

		if (minD != 0.0) {
			maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon, n.lat, x1,
					y1, x2, y2);
		}

		// System.out.println((n.lon+180)+","+(n.lat+90)+"=="+((i + 1) *
		// lonStride / 2.0) + ","
		// + ((j + 1) * latStride / 2.0));
		/* compute minD and maxD of user to cell */

		// TODO:FIX set the n.distance and n.maxD
		n.minD = minD;
		n.maxD = maxD;

		m_Grid[i][j].insert(n, minD, minD == 0.0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DistributedGrid my_grid = new DistributedGrid(1000, 10,Grid.LOG_N);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
