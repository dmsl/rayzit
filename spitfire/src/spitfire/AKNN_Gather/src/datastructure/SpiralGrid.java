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

public class SpiralGrid {

	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180

	private int latStride = 0;
	private int lonStride = 0;
	private int lat_bound = 0;
	private int lon_bound = 0;
	int k;

	Cell[][] m_Grid = null;

	// Cell size = spacesize / # of users
	public SpiralGrid(int numOfusers, int k,int cellSize) {
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

	public void insertCell(User n_in) {

		double x1, y1;
		double x2, y2;
		int startx = (int) (n_in.lon / lonStride);
		int starty = (int) (n_in.lat / latStride);

		int max = Math.max(lon_bound, lat_bound) + 1;
		int x = 0;
		int y = 0;
		int dx = 0;
		int dy = -1;
		boolean spiralEmptyCycle = false;
		// for (int i = 0; i < lon_bound; i++) {
		// for (int j = 0; j < lat_bound; j++)
		for (int l = 0; l <= Math.pow(max, 2); l++) {
			
			if ((startx + x) >= 0 && (starty + y) >= 0
					&& (startx + x) < lon_bound && (starty + y) < lat_bound) {

				
				// check each cycle
				// bottom diagonal is the end of each cycle
				if (x == y && (startx + x) <= startx && (starty + y) <= starty) {
					if (spiralEmptyCycle) {
//						System.out.println("Cycle " + (startx + x) + ","
//								+ (starty + y));
						break;
					}
					// reinitialize the flag
					spiralEmptyCycle = true;
				}

				/* compute minD and maxD of user to cell */
				// Create the cell if its null
				User n = new User(n_in.key, n_in.lon, n_in.lat);

				if (m_Grid[startx + x][starty + y] == null)
					m_Grid[startx + x][starty + y] = new Cell(k);
				// Double minD = currReport.getUser().getObfLocation().distance(
				// currCell.getCenter() ) - currCell.getRadius();

				// Double maxD = minD + 2 * currCell.getRadius();

				x1 = (startx + x) * lonStride;
				y1 = (starty + y) * latStride;
				x2 = (startx + x + 1) * lonStride;
				y2 = (starty + y + 1) * latStride;

//				 System.out.println("Grid.insertCell(" + x1 + "," + y1 + ":"
//				 + x2 + "," + y2 + ")");
				double maxD = 0.0;
				double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
						n.lat, x1, y1, x2, y2);

				if (minD != 0.0) {
					maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
							n.lat, x1, y1, x2, y2);
				}

				// System.out.println((n.lon+180)+","+(n.lat+90)+"=="+((i + 1) *
				// lonStride / 2.0) + ","
				// + ((j + 1) * latStride / 2.0));
				/* compute minD and maxD of user to cell */

				// TODO:FIX set the n.distance and n.maxD
				n.minD = minD;
				n.maxD = maxD;

				spiralEmptyCycle = !m_Grid[startx + x][starty + y].insert(n, minD,
						minD == 0.0) && spiralEmptyCycle; 

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
		SpiralGrid my_grid = new SpiralGrid(1000, 10,Grid.LOG_N);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
