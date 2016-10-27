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
package temp;

import java.io.IOException;
import java.util.List;

import datastructure.Cell;

import benchmark.Benchmarking;

import proximity.datastructures.User;
import utils.Trigonometry;
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

	// Number of nodes.
	static int n;

	// Fill the Grid.
	static Range[] ranges;
	static Range myrange;
	static int mylb;
	static int myub;
	// Communication buffers.
	static ObjectBuf<Cell>[] slices;
	static ObjectBuf<Cell> myslice;

	// Cell size = spacesize / # of users
	public DistributedGrid(int numOfusers, int k) {
		latStride = (int) Math.ceil((double) LAT_BOUND / Math.log(numOfusers));

		if (latStride == 0)
			latStride = 1;
		lonStride = (int) Math.ceil((double) LON_BOUND / Math.log(numOfusers));
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

	public void insertCell(final List<User> neighborsList)
			throws IOException {

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

		ranges = new Range(0, lon_bound - 1).subranges(size);
		myrange = ranges[rank];
		mylb = myrange.lb();
		myub = myrange.ub();

		if (rank != 0) {
			for (int i = mylb; i <= myub; i++) {
				for (int j = 0; j < lat_bound; j++) {
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
		slices = ObjectBuf.rowSliceBuffers(m_Grid, ranges);
		myslice = slices[rank];

		// System.out.println("DistributedGrid.insertCell(" + rank + ")[" + mylb
		// + " - " + myub + "]");

		// TODO Auto-generated method stub
		for (int i = mylb; i <= myub; i++) {
			for (int j = 0; j < lat_bound; j++) {
				/* compute minD and maxD of user to cell */
				for (int l = 0; l < neighborsList.size(); l++) {
					User n = neighborsList.get(l);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(i, j, n);
				}
			}
		}

		// Gather all matrix row slices into process 0.
		world.gather(0, myslice, slices);
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
		DistributedGrid my_grid = new DistributedGrid(1000, 10);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
