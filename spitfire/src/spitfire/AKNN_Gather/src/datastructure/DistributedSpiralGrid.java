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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;

import proximity.datastructures.User;
import utils.Trigonometry;
import benchmark.Benchmarking;
import edu.rit.mp.ObjectBuf;
import edu.rit.mp.buf.ObjectItemBuf;
import edu.rit.pj.Comm;
import edu.rit.pj.CommRequest;
import edu.rit.pj.IntegerForLoop;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import edu.rit.util.Range;

public class DistributedSpiralGrid {

	public static final boolean BENCH = false;// -90:90

	public static final int LAT_BOUND = 180;// -90:90
	public static final int LON_BOUND = 360;// -180:180
	public static final int BORDER_CANDIDATE_LISTS_NUMBER = 8;

	public static int N = 2;
	public static int latStride = 0;
	public static int lonStride = 0;
	public static HashSet<User>[] handoverLists = new HashSet[BORDER_CANDIDATE_LISTS_NUMBER];

	public static final int LL = 0;// LEFT
	public static final int UL = 1;// UP LEFT
	public static final int DL = 2;// DOWN LEFT
	public static final int RR = 3;// RIGHT
	public static final int UR = 4;// UP RIGHT
	public static final int DR = 5;// DOWN RIGHT
	public static final int UU = 6;// UP
	public static final int DD = 7;// DOWN

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
	public static Range r_myrange;
	public static int r_mylb = 0;
	public static int r_myub = 360;
	// Communication buffers.
	static ObjectBuf<Cell>[] slices;
	static ObjectBuf<Cell> myslice;

	// Fill the Grid.
	static Range[] c_ranges;
	public static Range c_myrange;
	public static int c_mylb = 0;
	public static int c_myub = 180;

	public static double diagonal;

	// Cell size = spacesize / # of users
	public DistributedSpiralGrid(int numOfusers, int k) {
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

		DistributedSpiralGrid.k = k;

		diagonal = 2 * Math.sqrt(Math.pow(latStride, 2)
				+ Math.pow(lonStride, 2));
		// Initialize all the sets
		for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
			handoverLists[i] = new HashSet<User>();
		}
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

	public void insertCell(final List<User> neighborsList) throws Exception {

		/**
		 * IMPORTANT THE initRanges is call for main benchmarking class
		 */
		// initRanges();

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

		// TODO Auto-generated method stub
		// new ParallelTeam().execute(new ParallelRegion() {
		// public void run() throws Exception {
		// execute(0, neighborsList.size() - 1, new IntegerForLoop() {
		// public void run(int first, int last) {
		// for (int i = first; i <= last; ++i) {

		for (int i = 0; i < neighborsList.size(); i++) {
			User n = neighborsList.get(i);
			// Place the Neighbor into the computed cell
			placeNeighborInCell(n, -1);
		}

		// }
		//
		// });
		// }
		// });
		// ///////////////////////////////////////////////////////////////////////////
		// Phase Two
		// Exchange BCLs
		// ///////////////////////////////////////////////////////////////////////////
		exchangeBCLs();

		// Prints All the elements
		// for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
		// System.out.println(rank + ".BCL(" + i + ")"
		// + handoverLists.get(i).size());
		//
		// }

		// Free unnecessary memory
		handoverLists = null;
		// System.out.println("Gather from " + rank);

		// System.out.println("DistributedSpiralGrid.gather()");

		// ***********************************
		// Gather all matrix row slices into process 0.
		world.gather(0, myslice, slices);

		// Due to the MPI environment we should kill all the process after the
		// calculation

		// ***********************************
		if (rank != 0)
			System.exit(0);

	}

	private static void exchangeBCLs() throws IOException {
		// TODO Auto-generated method stub

		// //////////////////////////////////////////////////////////////////////
		// Send
		// //////////////////////////////////////////////////////////////////////
		sendToAllBCS();

		// //////////////////////////////////////////////////////////////////////
		// Receive
		// //////////////////////////////////////////////////////////////////////
		receiveFromAllBCS();

	}

	private static void receiveFromAllBCS() throws IOException {
		// Receive from all the servers
		for (int b = 0; b < size; b++) {
			if (b == rank)
				continue;
			// System.out.println("Waititng for " + (rank + 1));
			ObjectItemBuf<ArrayList<User>> oiBuf = ObjectBuf.buffer();
			world.receive(null, oiBuf);
			ArrayList<User> temp = oiBuf.item;
			// System.out.println(rank + ".DistributedSpiralGrid.receive("
			// + temp.size() + ") from " + (rank + 1));
			for (int i = 0; i < temp.size(); i++) {
				User n = temp.get(i);
				// Place the Neighbor into the computed cell
				placeNeighborInCell(n, b);
			}
		}
	}

	private static void sendToAllBCS() throws IOException {
		ObjectBuf<ArrayList<User>> buf = null;

		Gson gson = new Gson();

		// Broadcast the lists
		for (int b = 0; b < size; b++) {
			if (b == rank)
				continue;
			// Above
			if ((rank % N) == (b % N) && b > rank) {
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[UU]));
//				System.out.println(rank + "UU -> " + b + "|"
//						+ gson.toJson(handoverLists[UU]));
			} else
			// Down
			if ((rank % N) == (b % N) && b < rank) {
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[DD]));
//				System.out.println(rank + "DD -> " + b + "|"
//						+ gson.toJson(handoverLists[DD]));
			} else
			// Right
			if ((rank % N) < (b % N) && (b - rank) < N && (b - rank) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[RR]));
//				System.out.println(rank + "RR -> " + b + "|"
//						+ gson.toJson(handoverLists[RR]));
			} else
			// Left 
			if ((rank % N) > (b % N) && Math.abs(b - rank) < N
					&& (b - rank) < 0) {
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[LL]));
//				System.out.println(rank + "LL -> " + b + "|"
//						+ gson.toJson(handoverLists[LL]));
			} else
			// Up Left
			if ((rank % N) > (b % N) && b > rank) {
				// HashSet<Neighbor> set = new HashSet<Neighbor>();
				// set.addAll(handoverLists[LL]);
				// set.addAll(handoverLists[UU]);
				//
				// buf = ObjectBuf.buffer(new ArrayList<Neighbor>(set));
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[UL]));
//				System.out.println(rank + "UL -> " + b + "|"
//						+ gson.toJson(handoverLists[UL]));
			} else
			// Up Right
			if ((rank % N) < (b % N) && b > rank) {
				// HashSet<Neighbor> set = new HashSet<Neighbor>();
				// set.addAll(handoverLists[RR]);
				// set.addAll(handoverLists[UU]);
				//
				// buf = ObjectBuf.buffer(new ArrayList<Neighbor>(set));
				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[UR]));
//				System.out.println(rank + "UR -> " + b + "|"
//						+ gson.toJson(handoverLists[UR]));
			} else
			// Down Left
			if ((rank % N) > (b % N) && b < rank) {
				// HashSet<Neighbor> set = new HashSet<Neighbor>();
				// set.addAll(handoverLists[DD]);
				// set.addAll(handoverLists[LL]);
				// buf = ObjectBuf.buffer(new ArrayList<Neighbor>(set));

				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[DL]));
//				System.out.println(rank + "DL -> " + b + "|"
//						+ gson.toJson(handoverLists[DL]));
			} else
			// Down Right
			if ((rank % N) < (b % N) && b < rank) {
				// HashSet<Neighbor> set = new HashSet<Neighbor>();
				// set.addAll(handoverLists[DD]);
				// set.addAll(handoverLists[RR]);
				// buf = ObjectBuf.buffer(new ArrayList<Neighbor>(set));

				buf = ObjectBuf.buffer(new ArrayList<User>(
						handoverLists[DR]));
//				System.out.println(rank + "DR -> " + b + "|"
//						+ gson.toJson(handoverLists[DR]));
			}

			world.send(b, buf, new CommRequest());
		}
	}

	/*
		private static void receiveBCS() throws IOException {
			// TODO Auto-generated method stub
			// receive from the right
			if ((rank + 1) < size && ((rank + 1) % N) > 0) {
				// System.out.println("Waititng for " + (rank + 1));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank + 1, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank + 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}

			}
			// receive from the above
			if ((rank + N) < size) {
				// System.out.println("Waititng for " + (rank + N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank + N, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank + N));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}
			// receive from the left
			if ((rank % N) > 0) {
				// System.out.println("Waititng for " + (rank - 1));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank - 1, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank - 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}
			// receive from the below
			if ((rank - N) >= 0) {
				// System.out.println("Waititng for " + (rank - N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank - N, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank - N));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}
			// /////////////////////////////////////////////////////////////////////////
			// Diagonal Lists
			// /////////////////////////////////////////////////////////////////////////

			// Right Up
			if ((rank + N + 1) < size && (rank / N) < (N - 1)
					&& ((rank + 1) % N) > 0) {
				// System.out.println("Waititng for " + (rank - N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank + N + 1, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank + N + 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}

			// Left up
			if ((rank + N - 1) > 0 && (rank / N) < (N - 1) && (rank % N) > 0) {
				// System.out.println("Waititng for " + (rank - N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank + N - 1, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank + N - 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}

			// Right Down
			if ((rank - N + 1) > 0 && ((rank + 1) % N) > 0 && (rank / N) > 0) {
				// System.out.println("Waititng for " + (rank - N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(rank - N + 1, oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank - N + 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}

			// Left Down
			if (((rank - N) - 1) >= 0 && (rank % N) > 0 && (rank / N) > 0) {
				// System.out.println("Waititng for " + (rank - N));
				ObjectItemBuf<ArrayList<Neighbor>> oiBuf = ObjectBuf.buffer();
				world.receive(((rank - N) - 1), oiBuf);
				ArrayList<Neighbor> temp = oiBuf.item;
				// System.out.println(rank + ".DistributedSpiralGrid.receive("
				// + temp.size() + ") from " + (rank - N - 1));
				for (int i = 0; i < temp.size(); i++) {
					Neighbor n = temp.get(i);
					// Place the Neighbor into the computed cell
					placeNeighborInCell(n, -1);
				}
			}
		}

		private static void sendBCS() throws IOException {
			ObjectBuf<ArrayList<Neighbor>> buf;

			// TODO Auto-generated method stub
			// Send to the right
			if ((rank + 1) < size && ((rank + 1) % N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(RR)));
				// System.out.println("Send " + buf.length() + " to " + (rank + 1));
				world.send(rank + 1, buf);

			}
			// Send to the above
			if ((rank + N) < size) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(UU)));
				// System.out.println("Send to " + (rank + N));
				world.send(rank + N, buf);
			}
			// Send to the left
			if ((rank % N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(LL)));
				// System.out.println("Send to " + (rank - 1));
				world.send(rank - 1, buf);
			}
			// Send to the below
			if ((rank - N) >= 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(DD)));
				// System.out.println("Send to " + (rank - N));
				world.send(rank - N, buf);
			}

			// /////////////////////////////////////////////////////
			// Diagonal Lists
			// /////////////////////////////////////////////////////
			// Right Up

			if ((rank + N + 1) < size && (rank / N) < (N - 1)
					&& ((rank + 1) % N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(UR)));
				// System.out.println(rank + ".ruSend to " + (rank + N + 1));
				world.send(rank + N + 1, buf);
			}

			// Left up
			if ((rank + N - 1) > 0 && (rank / N) < (N - 1) && (rank % N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(UL)));
				// System.out.println(rank + ".luSend to " + (rank + N - 1));
				world.send(rank + N - 1, buf);
			}

			// Right Down
			if ((rank - N + 1) > 0 && ((rank + 1) % N) > 0 && (rank / N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(DR)));
				// System.out.println(rank + ".rdSend to " + (rank - N + 1));
				world.send(rank - N + 1, buf);
			}

			// Left Down
			if (((rank - N) - 1) >= 0 && (rank % N) > 0 && (rank / N) > 0) {
				buf = ObjectBuf.buffer(new ArrayList<Neighbor>(handoverLists
						.get(DL)));
				// System.out.println(rank + ".ldSend to " + (rank - N - 1));
				world.send((rank - N) - 1, buf);
			}
		}
	*/

	private static void placeNeighborInCell(User n_in, int b) {

		double r1, c1;
		double r2, c2;

		// Row is the c
		// Column is the r
		int startr = (int) (n_in.lon / lonStride);
		int startc = (int) (n_in.lat / latStride);
		int max = Math.max(2 * lon_bound, 2 * lat_bound) + 1;

		if (b > 0) {
			int r_middle = (int) Math.ceil((r_mylb + r_myub) / 2.0);
			int c_middle = (int) Math.ceil((c_mylb + c_myub) / 2.0);
			if (startr < r_middle && startc < c_middle) {
				startr = r_mylb;
				startc = c_mylb;
			} else if (startr < r_middle && startc > c_middle) {
				startr = r_mylb;
				startc = c_myub;
			} else if (startr > r_middle && startc < c_middle) {
				startr = r_myub;
				startc = c_mylb;
			} else {
				startr = r_myub;
				startc = c_myub;
			}
			max = Math.max(2 * (r_myub - r_mylb), 2 * (c_myub - c_mylb)) + 1;
		}

		int r = 0;
		int c = 0;
		int dx = 0;
		int dy = -1;
		boolean spiralEmptyCycle = false;

		for (int l = 0; l < Math.pow(max, 2); l++) {

			int my_r = (startr + r);
			int my_c = (startc + c);

			// This means that a cycle has been done out of the range
			if ((my_r >= r_mylb && my_r <= r_myub)
					&& (my_c >= c_mylb && my_c <= c_myub)) {


				// check each cycle
				// Bottom diagonal is the end of each cycle
				if (r == c && my_r <= startr && my_c <= startc) {
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

				synchronized (m_Grid[my_r][my_c]) {
					if (m_Grid[my_r][my_c] == null)
						m_Grid[my_r][my_c] = new Cell(k);
				}
				// Double minD =
				// currReport.getUser().getObfLocation().distance(
				// currCell.getCenter() ) - currCell.getRadius();

				// Double maxD = minD + 2 * currCell.getRadius();

				r1 = my_r * lonStride;
				c1 = my_c * latStride;
				r2 = (my_r + 1) * lonStride;
				c2 = (my_c + 1) * latStride;

				// System.out.println("Grid.insertCell(" + x1 + "," + y1 +
				// ":"
				// + x2 + "," + y2 + ")");
				double maxD = 0.0;
				double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
						n.lat, r1, c1, r2, c2);

				if (minD != 0.0) {
					maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
							n.lat, r1, c1, r2, c2);
				}

				// System.out.println((n.lon+180)+","+(n.lat+90)+"=="+((i +
				// 1) *
				// lonStride / 2.0) + ","
				// + ((j + 1) * latStride / 2.0));
				/* compute minD and maxD of user to cell */

				// TODO:FIX set the n.distance and n.maxD
				n.minD = minD;
				n.maxD = maxD;

				synchronized (m_Grid[my_r][my_c]) {
					spiralEmptyCycle = !m_Grid[my_r][my_c].insert(n, minD,
							minD == 0.0) && spiralEmptyCycle;
				}


			} else {
				if (b < 0) {

					// DOWN
					if (my_r <= r_mylb && my_c >= c_mylb && my_c <= c_myub)
						synchronized (handoverLists[DD]) {
							handoverLists[DD].add(n_in);
						}
					else
					// RIGHT
					if (my_r >= r_mylb && my_r <= r_myub && my_c >= c_myub)
						synchronized (handoverLists[RR]) {
							handoverLists[RR].add(n_in);
						}
					else
						// UP
						if (my_r >= r_myub && my_c >= c_mylb && my_c <= c_myub)
							synchronized (handoverLists[UU]) {
								handoverLists[UU].add(n_in);
							}
						else
					// LEFT
					if (my_r >= r_mylb && my_r <= r_myub && my_c <= c_mylb)
						synchronized (handoverLists[LL]) {
							handoverLists[LL].add(n_in);
						}
					else

					
					// DOWN RIGHT
					if (my_r <= r_mylb && my_c >= c_myub)
						synchronized (handoverLists[DR]) {
							handoverLists[DR].add(n_in);
						}
					else
					// DOWN LEFT
					if (my_r <= r_mylb && my_c <= c_mylb)
						synchronized (handoverLists[DL]) {
							handoverLists[DL].add(n_in);
						}
					else
					// UP LEFT
					if (my_r >= r_myub && my_c <= c_mylb)
						synchronized (handoverLists[UL]) {
							handoverLists[UL].add(n_in);
						}
					else
					// UP RIGHT
					if (my_r >= r_myub && my_c >= c_myub)
						synchronized (handoverLists[UR]) {
							handoverLists[UR].add(n_in);
						}
				}

			}

			if (r == c || (r < 0 && r == -c) || (r > 0 && r == (1 - c))) {
				int temp = dx;
				dx = -dy;
				dy = temp;
			}
			r = r + dx;
			c = c + dy;

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DistributedSpiralGrid my_grid = new DistributedSpiralGrid(1000, 10);
		System.out.println(my_grid.getCell(new User("U", 10, 90)));
	}

}
