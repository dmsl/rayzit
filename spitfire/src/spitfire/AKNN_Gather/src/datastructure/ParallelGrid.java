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

import java.util.List;

import proximity.datastructures.User;
import utils.Trigonometry;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;

public class ParallelGrid {

	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180

	private int latStride = 0;
	private int lonStride = 0;
	private int lat_bound = 0;
	private int lon_bound = 0;
	int k;

	Cell[][] m_Grid = null;

	// Cell size = spacesize / # of users
	public ParallelGrid(int numOfusers, int k) {
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

		for (int i = 0; i < lon_bound; i++) {
			for (int j = 0; j < lat_bound; j++) {
				/* compute minD and maxD of user to cell */
				// Create the cell if it is null
				m_Grid[i][j] = new Cell(k);
			}
		}

		this.k = k;
	}

	public Cell getCell(User n) {
		return m_Grid[(int) (n.lon / lonStride)][(int) (n.lat / latStride)];
	}

	public void insertCell(final List<User> neighborsList) {

		// Get Number of processors available and how many to use;
		int gotp;

		gotp = Runtime.getRuntime().availableProcessors();
		if (lon_bound < gotp)
			gotp = lon_bound;

		// System.out.println(" ** " + gotp + " processors available");

		// We use up to the number avaialble but no more
		final int nump = gotp;

		// System.out.println(" ** Using " + nump + " of " + gotp +
		// " processors");

		// Calculate the basic parameters
		// Here we calculate how the problem is to be split up
		// so we divide the number of rows by the number of processors
		int rsize = lon_bound / nump;

		final int slice_row = rsize;


		try {
			new ParallelTeam(nump).execute(new ParallelRegion() {
				// Run Method
//				 public void start()
//				 {
//				 System.out.println(" ** ParallelRegion Start");
//				 }

				@Override
				public void run() throws Exception {
					int myPID = getThreadIndex();					
					int mystart_row = 0, myend_row = lon_bound;

					mystart_row = myPID*slice_row;
					myend_row = mystart_row + slice_row;
		
					// If it is the last one then add the rest iteration
					if (myPID == (nump - 1)) {
						myend_row += (lon_bound % nump);
					}
			
					// TODO Auto-generated method stub
					for (int i = mystart_row; i < myend_row && i < lon_bound; i++) {
						for (int j = 0; j < lat_bound; j++) {
							/* compute minD and maxD of user to cell */
							for (int l = 0; l < neighborsList.size(); l++) {
								User n = neighborsList.get(l);
								// Place the Neighbor into the computed cell
								placeNeighborInCell(i, j, n);
							}
						}
					}
				}
				// Finish Method
				// public void finish()
				// {
				// System.out.println(" ** ParallelRegion Finish");
				// }

			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		ParallelGrid my_grid = new ParallelGrid(1000, 10);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
