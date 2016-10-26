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

import proximity.datastructures.User;
import utils.Trigonometry;

public class Grid {
	// Cell Sizes
	public static final int N = 0;
	public static final int SQRT_N = 1;
	public static final int LN_N = 2;
	public static final int LOG_N = 3;
	public static final int SINGLE = 4;
	/*
	stride = axis_length / num_of_cells
	for Random and Oldenburg ONLY 10K and 100K
	                        num_of_cells = { N/k, N/(k*lgN),
	N/(k*lnN), N/(k*sqrtN), N/k^2, N/(k^2*lg^2N), N/(k^2*ln^2N)
	where N is the population of each partition (N_p) 
	 */
	public static final int PARAM1 = 5;
	public static final int PARAM2 = 6;
	public static final int PARAM3 = 7;
	public static final int PARAM4 = 8;
	public static final int PARAM5 = 9;
	public static final int PARAM6 = 10;
	public static final int PARAM7 = 11;
	public static final int PARAM8 = 12;


	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180


	private double latStride = 0;
	private double lonStride = 0;
	private int lat_cells = 0;
	private int lon_cells = 0;
	int k;

	Cell[][] m_Grid = null;

	// Cell size = spacesize / # of users
	public Grid(int numOfUsers, int k, int cellSize) {

		switch (cellSize) {
		case Grid.N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log(numOfUsers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log(numOfUsers));
			break;
		case Grid.SQRT_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.sqrt(numOfUsers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.sqrt(numOfUsers));
			break;
		case Grid.LN_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log(numOfUsers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log(numOfUsers));
			break;
		case Grid.LOG_N:
			latStride = (int) Math.ceil((double) LAT_BOUND
					/ Math.log10(numOfUsers));
			lonStride = (int) Math.ceil((double) LON_BOUND
					/ Math.log10(numOfUsers));
			break;
			// N/k
			case Grid.PARAM1:
				latStride = (double) LAT_BOUND / k;
				lonStride = (double) LON_BOUND / k;
				break;
			// N/(k*lgN)
			case Grid.PARAM2:
				latStride = (double) LAT_BOUND / (Math.log10(numOfUsers) * k);
				lonStride = (double) LON_BOUND / (Math.log10(numOfUsers) * k);
				break;
			// N/(k*lnN)
			case Grid.PARAM3:
				latStride = (double) LAT_BOUND / (Math.log(numOfUsers) * k);
				lonStride = (double) LON_BOUND / (Math.log(numOfUsers) * k);
				break;
			// N/(k*sqrtN)
			case Grid.PARAM4:
				latStride = (double) LAT_BOUND / (Math.sqrt(numOfUsers) * k);
				lonStride = (double) LON_BOUND / (Math.sqrt(numOfUsers) * k);
				break;
			// N/k^2
			case Grid.PARAM5:
				latStride = (double) LAT_BOUND / (Math.pow(k, 2) * numOfUsers);
				lonStride = (double) LON_BOUND / (Math.pow(k, 2) * numOfUsers);
				break;
			// N/(k^2*lg^2N)
			case Grid.PARAM6:
				latStride = (double) LAT_BOUND
						/ (Math.pow(k, 2) * Math.pow(Math.log10(numOfUsers), 2));
				lonStride = (double) LON_BOUND
						/ (Math.pow(k, 2) * Math.pow(Math.log10(numOfUsers), 2));
				break;
			// N/(k^2*ln^2N)
			case Grid.PARAM7:
				latStride = (double) LAT_BOUND
						/ (Math.pow(k, 2) * Math.pow(Math.log(numOfUsers), 2));
				lonStride = (double) LON_BOUND
						/ (Math.pow(k, 2) * Math.pow(Math.log(numOfUsers), 2));
				break;
			// axis_length / SQRT(4*N_p/1000)
			case Grid.PARAM8:
				latStride = (double) LAT_BOUND
						/ Math.sqrt(4 * (numOfUsers / 1000.0));
				lonStride = (double) LON_BOUND
						/ Math.sqrt(4 * (numOfUsers / 1000.0));
				break;
			default:
				latStride = (double) LAT_BOUND / 1;
				lonStride = (double) LON_BOUND / 1;
				break;
			}

			if (latStride == 0)
				latStride = 1;

			if (lonStride == 0)
				lonStride = 1;

			lat_cells = (int) Math.round((double) LAT_BOUND / (double) latStride);
			lon_cells = (int) Math.round((double) LON_BOUND / (double) lonStride);

			System.err.println("Grid : " + (lon_cells + 1) + "X" + (lat_cells + 1));
		// Create the cell with empty handle to Cell object
		m_Grid = new Cell[lon_cells+1][lat_cells+1];

		this.k = k;
	}

	public Cell getCell(User n) {
		return m_Grid[(int) (n.lon / lonStride)][(int) (n.lat / latStride)];
	}

	public void insertCell(User n_in) {

		double x1, y1;
		double x2, y2;

		for (int i = 0; i <= lon_cells; i++) {
			for (int j = 0; j <= lat_cells; j++) {
				/* compute minD and maxD of user to cell */
				// Create the cell if its null
				User n = new User(n_in.key, n_in.lon, n_in.lat);

				if (m_Grid[i][j] == null)
					m_Grid[i][j] = new Cell(k);
				// Double minD = currReport.getUser().getObfLocation().distance(
				// currCell.getCenter() ) - currCell.getRadius();

				// Double maxD = minD + 2 * currCell.getRadius();

				x1 = i * lonStride;
				y1 = j * latStride;
				x2 = (i + 1) * lonStride;
				y2 = (j + 1) * latStride;

				// System.out.println("Grid.insertCell(" + x1 + "," + y1 + ":"
				// + x2 + "," + y2 + ")");
				double maxD = 0.0;
				double minD = Trigonometry.pointToRectangleBoundaryMinDistance(
						n.lon, n.lat, x1, y1, x2, y2);

				if (minD != 0.0) {
					maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(
							n.lon, n.lat, x1, y1, x2, y2);
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
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Grid my_grid = new Grid(1000, 10, Grid.LOG_N);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
