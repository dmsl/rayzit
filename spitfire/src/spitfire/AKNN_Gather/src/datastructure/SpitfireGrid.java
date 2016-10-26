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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import couchbase.connector.CouchBaseData;

import proximity.datastructures.User;
import spitfire.datastructures.ServerBCS;
import utils.HistogramDivision;
import utils.ObjectCompression;
import utils.Trigonometry;
import algorithms.Spitfire;
import benchmark.Benchmarking;
import edu.rit.mp.ObjectBuf;
import edu.rit.mp.buf.ObjectItemBuf;
import edu.rit.pj.Comm;
import edu.rit.pj.IntegerForLoop;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import edu.rit.util.Range;

public class SpitfireGrid {

	// Handover Lists
	public static final int BORDER_CANDIDATE_LISTS_NUMBER = 8;

	// Due to high send/receive complexity we need to communicate to each server
	// with one message
	// ServerBCS with hashset to eliminate any duplicate users
	public static HashMap<Integer, ServerBCS>[] adjacencyLists;

	public static final int LL = 0;// LEFT
	public static final int UL = 1;// UP LEFT
	public static final int DL = 2;// DOWN LEFT
	public static final int RR = 3;// RIGHT
	public static final int UR = 4;// UP RIGHT
	public static final int DR = 5;// DOWN RIGHT
	public static final int UU = 6;// UP
	public static final int DD = 7;// DOWN

	public static final boolean BENCH = false;// -90:90

	public static int SERVER_LAT_BOUND = 180;// -90:90 + 1 for the border
	// candidates
	public static int SERVER_LON_BOUND = 360;// -180:180 + 1 for the border
	// Candidates
	private static double db_lat;
	private static double db_lon;
	public static double latSegments = 0;
	public static double lonSegments = 0;
	public static double bd_latSegments = 0;
	public static double bd_lonSegments = 0;
	public static int lat_cells = 0;
	public static int lon_cells = 0;
	static int k;

	static SpitfireCell[][] m_Grid = null;
	// Shared variables.

	// World communicator.
	static Comm world;
	static int size;
	static int rank;
	public static int sqrtM;
	static int receiversSize = 0;// How many user should receive from

	// Fill the Grid.
	static Range[] r_ranges;
	public static Range r_myrange;
	public static int r_mylb;
	public static int r_myub;
	// Communication buffers.
	static ObjectBuf<SpitfireCell>[] slices;
	static ObjectBuf<SpitfireCell> myslice;

	// Fill the Grid.
	static Range[] c_ranges;
	public static Range c_myrange;
	public static int c_mylb;
	public static int c_myub;

	// Cell size = spacesize / # of users
	/**
	 * Constructor get d_b_size != cellSize
	 * 
	 * @param k
	 * @param cellSize
	 * @param c
	 * @throws IOException
	 */
	public SpitfireGrid(int k, int cellSize, double c, int n)
			throws IOException {

		int Ai = SpitfireGrid.initRangesBasedOnHistogram();

		// We include the last number so we need add +1 :p
		SERVER_LON_BOUND = c_myub - c_mylb + 1;
		SERVER_LAT_BOUND = r_myub - r_mylb + 1;

		// Area of the square
		int A = 360 * 180;// SpitfireGrid.initRangesBasedOnHistogram();

		final double sqrt2 = Math.sqrt(2);

		// Initialize stride to match rectangle area
		double db = c * Math.sqrt(k * A / (n * Math.PI));
		db_lat = (db / sqrt2);
		db_lon = (db * sqrt2);
		bd_latSegments = (double) SERVER_LAT_BOUND / db_lat;
		bd_lonSegments = (double) SERVER_LON_BOUND / db_lon;

		sqrtM = (int) Math.floor(Math.sqrt(size));

		// Now calculate the cellsize
		switch (cellSize) {
		case Grid.N:
			latSegments = (double) SERVER_LAT_BOUND / Math.log(Ai);
			lonSegments = (double) SERVER_LON_BOUND / Math.log(Ai);
			break;
		case Grid.SQRT_N:
			latSegments = (double) SERVER_LAT_BOUND / Math.sqrt(Ai);
			lonSegments = (double) SERVER_LON_BOUND / Math.sqrt(Ai);
			break;
		case Grid.LN_N:
			latSegments = (double) SERVER_LAT_BOUND / Math.log(Ai);
			lonSegments = (double) SERVER_LON_BOUND / Math.log(Ai);
			break;
		case Grid.LOG_N:
			latSegments = (double) SERVER_LAT_BOUND / Math.log10(Ai);
			lonSegments = (double) SERVER_LON_BOUND / Math.log10(Ai);
			break;
		// One cell per server 9 SERVERS
		case Grid.SINGLE:
			latSegments = (double) SERVER_LAT_BOUND / 3;
			lonSegments = (double) SERVER_LON_BOUND / 3;
			break;

		/*
		stride = axis_length / num_of_cells
		for Random and Oldenburg ONLY 10K and 100K
		                    num_of_cells = { N/k, N/(k*lgN),
		N/(k*lnN), N/(k*sqrtN), N/k^2, N/(k^2*lg^2N), N/(k^2*ln^2N)
		where N is the population of each partition (N_p) 
		 */
		// N/k
		case Grid.PARAM1:
			latSegments = (double) SERVER_LAT_BOUND / k;
			lonSegments = (double) SERVER_LON_BOUND / k;
			break;
		// N/(k*lgN)
		case Grid.PARAM2:
			latSegments = (double) SERVER_LAT_BOUND / (Math.log10(Ai) * k);
			lonSegments = (double) SERVER_LON_BOUND / (Math.log10(Ai) * k);
			break;
		// N/(k*lnN)
		case Grid.PARAM3:
			latSegments = (double) SERVER_LAT_BOUND / (Math.log(Ai) * k);
			lonSegments = (double) SERVER_LON_BOUND / (Math.log(Ai) * k);
			break;
		// N/(k*sqrtN)
		case Grid.PARAM4:
			latSegments = (double) SERVER_LAT_BOUND / (Math.sqrt(Ai) * k);
			lonSegments = (double) SERVER_LON_BOUND / (Math.sqrt(Ai) * k);
			break;
		// N/k^2
		case Grid.PARAM5:
			latSegments = (double) SERVER_LAT_BOUND / (Math.pow(k, 2) * Ai);
			lonSegments = (double) SERVER_LON_BOUND / (Math.pow(k, 2) * Ai);
			break;
		// N/(k^2*lg^2N)
		case Grid.PARAM6:
			latSegments = (double) SERVER_LAT_BOUND
					/ (Math.pow(k, 2) * Math.pow(Math.log10(Ai), 2));
			lonSegments = (double) SERVER_LON_BOUND
					/ (Math.pow(k, 2) * Math.pow(Math.log10(Ai), 2));
			break;
		// N/(k^2*ln^2N)
		case Grid.PARAM7:
			latSegments = (double) SERVER_LAT_BOUND
					/ (Math.pow(k, 2) * Math.pow(Math.log(Ai), 2));
			lonSegments = (double) SERVER_LON_BOUND
					/ (Math.pow(k, 2) * Math.pow(Math.log(Ai), 2));
			break;
		// axis_length / SQRT(4*N_p/1000)
		case Grid.PARAM8:
			latSegments = (double) SERVER_LAT_BOUND
					/ Math.sqrt(4 * (Ai / 1000.0));
			lonSegments = (double) SERVER_LON_BOUND
					/ Math.sqrt(4 * (Ai / 1000.0));
			break;
		default:
			latSegments = (double) SERVER_LAT_BOUND / 1;
			lonSegments = (double) SERVER_LON_BOUND / 1;
			break;
		}

		if (latSegments == 0)
			latSegments = 1;

		if (lonSegments == 0)
			lonSegments = 1;

		// AREA/SEGMENT_SIZE = CELLS
		lat_cells = (int) Math.round((double) SERVER_LAT_BOUND
				/ (double) latSegments);
		lon_cells = (int) Math.round((double) SERVER_LON_BOUND
				/ (double) lonSegments);
		// Spitfire.print("LAT_BOUND=" + LAT_BOUND);
		// Spitfire.print("LON_BOUND=" + LON_BOUND);
		// Spitfire.print("Grid : " + (lon_cells + 1) + "X" + (lat_cells + 1));

		// Create the cell with empty handles to Cell object
		m_Grid = new SpitfireCell[lon_cells + 1][lat_cells + 1];

		SpitfireGrid.k = k;

		// Initialize all the sets
		// Create a matrix with Servers handover lists
		createAdjacencyLists();

	}

	@SuppressWarnings("unchecked")
	private static void createAdjacencyLists() {

		adjacencyLists = new HashMap[BORDER_CANDIDATE_LISTS_NUMBER];

		for (int b = 0; b < size; b++) {

			if (b == rank)
				continue;

			Range r_ranges = HistogramDivision.r_ranges[b];
			int r_lb = r_ranges.lb();
			int r_ub = r_ranges.ub();

			Range c_ranges = HistogramDivision.c_ranges[b / sqrtM];
			int c_lb = c_ranges.lb();
			int c_ub = c_ranges.ub();

			// TODO Fix this for a Small amount of users
			// Check if the server is the adjacent

			// IMPORTANT: The put function is unique and we cannot override any
			// previous hasmap. Direction + Rank (serverID) => Unique

			// DOWN
			if (r_ub == r_mylb
					&& (c_myrange.contains(c_ub) && c_myrange.contains(c_lb))) {
				if (adjacencyLists[DD] == null) {
					adjacencyLists[DD] = new HashMap<Integer, ServerBCS>();
				}
				adjacencyLists[DD].put(b, new ServerBCS(new HashSet<User>(), b,
						r_ranges, c_ranges));

			} else
			// UP
			if (r_lb == r_myub
					&& (c_myrange.contains(c_ub) && c_myrange.contains(c_lb))) {
				if (adjacencyLists[UU] == null) {
					adjacencyLists[UU] = new HashMap<Integer, ServerBCS>();
				}
				adjacencyLists[UU].put(b, new ServerBCS(new HashSet<User>(), b,
						r_ranges, c_ranges));
			} else
			// LEFT
			if (c_ub == c_mylb) {

				// LEFT LEFT
				if ((r_myrange.contains(r_ub) && r_myrange.contains(r_lb))
						|| (r_ranges.contains(r_myub) && r_ranges
								.contains(r_mylb))
						|| (r_myrange.contains(r_ub - 1) && r_lb < r_mylb)
						|| (r_myrange.contains(r_lb + 1) && r_ub > r_myub)) {
					// Create new adjacency list if it is null
					if (adjacencyLists[LL] == null) {
						adjacencyLists[LL] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[LL].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}
				// Create Diagonal Adjacency list
				// LEFT UP corner

				if (r_myub < r_ub && r_myub >= r_lb) {
					// Create new adjacency list if it is null
					if (adjacencyLists[UL] == null) {
						adjacencyLists[UL] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[UL].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}
				// Create Diagonal Adjacency list
				// LEFT DOWN corner
				if (r_mylb > r_lb && r_mylb <= r_ub) {
					// Create new adjacency list if it is null
					if (adjacencyLists[DL] == null) {
						adjacencyLists[DL] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[DL].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}

			} else
			// RIGHT
			if (c_lb == c_myub) {

				// RIGHT RIGHT
				if ((r_myrange.contains(r_ub) && r_myrange.contains(r_lb))
						|| (r_ranges.contains(r_myub) && r_ranges
								.contains(r_mylb))
						|| (r_myrange.contains(r_ub - 1) && r_lb < r_mylb)
						|| (r_myrange.contains(r_lb + 1) && r_ub > r_myub)) {
					// Create new adjacency list if it is null
					if (adjacencyLists[RR] == null) {
						adjacencyLists[RR] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[RR].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}
				// Create Diagonal Adjacency list
				// RIGHT UP corner
				if (r_myub < r_ub && r_myub >= r_lb) {
					// Create new adjacency list if it is null
					if (adjacencyLists[UR] == null) {
						adjacencyLists[UR] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[UR].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}
				// Create Diagonal Adjacency list
				// RIGHT DOWN corner
				if (r_mylb > r_lb && r_mylb <= r_ub) {
					// Create new adjacency list if it is null
					if (adjacencyLists[DR] == null) {
						adjacencyLists[DR] = new HashMap<Integer, ServerBCS>();
					}
					adjacencyLists[DR].put(b, new ServerBCS(
							new HashSet<User>(), b, r_ranges, c_ranges));
				}

			}
		}

	}

	/**
	 * Return the respect cell for the 'n' user
	 * 
	 * @param n
	 * @return cell
	 */
	public static SpitfireCell getCell(User n) {
		int x = (int) ((n.lon - c_mylb) / lonSegments);
		int y = (int) ((n.lat - r_mylb) / latSegments);
		// if (m_Grid.length <= x || m_Grid[0].length <= y) {
		// Spitfire.print("xy[" + m_Grid.length + "][" + m_Grid[0].length
		// + "]");
		// Spitfire.print("xy[" + x + "][" + y + "]");
		// Spitfire.print("n[" + n.lon + "][" + n.lat + "]");
		// Spitfire.print("ls[" + lonSegments + "][" + latSegments + "]");
		// Spitfire.print("lb[" + c_mylb + "][" + r_mylb + "]");
		// Spitfire.print("ub[" + c_myub + "][" + r_myub + "]");
		// }
		return m_Grid[x][y];
	}

	/**
	 * Initialize the range for each server based on histogram. Need to connect
	 * to couchbase
	 * 
	 * @return the number of user in the specific server
	 * @throws IOException
	 */
	public static int initRangesBasedOnHistogram() throws IOException {
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

		List<User> users = CouchBaseData.getData();
		long start = System.currentTimeMillis();

		// Change the division
		HistogramDivision.createEquidepthGrid(users, size);
		// HistogramDivision.createEquiwidthGrid(users, size);

		long end = System.currentTimeMillis();
		Spitfire.print("HistogramDivision time:\t" + (end - start));

		// Free uncessary memory
		users = null;

		// if (Math.sqrt(size) > N)
		sqrtM = (int) Math.floor(Math.sqrt(size));

		// if (size > N) {
		// r_ranges = new Range(0, lon_bound - 1).subranges(size / N);
		// c_ranges = new Range(0, lat_bound - 1).subranges(N);
		// } else {
		// r_ranges = new Range(0, lon_bound - 1).subranges(size);
		// c_ranges = new Range(0, lat_bound - 1).subranges(size);
		// }
		// 0-1-2
		c_myrange = HistogramDivision.c_ranges[rank / sqrtM];
		// 0-1-2-3-4-5-6-7-8
		r_myrange = HistogramDivision.r_ranges[rank];
		Spitfire.print(rank + ".lat:" + r_myrange.toString() + " ");
		Spitfire.print(rank + ".lon:" + c_myrange.toString() + " ");

		r_mylb = r_myrange.lb();
		r_myub = r_myrange.ub();

		c_mylb = c_myrange.lb();
		c_myub = c_myrange.ub();

		return HistogramDivision.y_sum_buckets[rank];

	}

	/**
	 * Calculate the distance between user and cell. Create the new user and
	 * return the user
	 * 
	 * @param i
	 * @param j
	 * @param n_in
	 * @return user
	 */
	@SuppressWarnings("unused")
	private static User copyUserWithDistToCell(int x1, int y1, int x2, int y2,
			User n_in) {

		User n = new User(n_in.key, n_in.lon, n_in.lat);
		// Spitfire.print("Grid.insertCell(" + x1 + "," + y1 + ":"
		// + x2 + "," + y2 + ")");

		// Calculate the minimum distance
		double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		// Calculate the maximum distance
		double maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		n.minD = minD;
		n.maxD = maxD;
		return n;
	}

	/**
	 * Calculate the distance between user and cell. NOT create the new user and
	 * return the user
	 * 
	 * @param i
	 * @param j
	 * @param n_in
	 * @return user
	 */
	private static User getUserWithDistToCell(double x1, double y1, double x2,
			double y2, User n) {
		// Spitfire.print("Grid.insertCell(" + x1 + "," + y1 + ":"
		// + x2 + "," + y2 + ")");

		// Calculate the minimum distance
		double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		// Calculate the maximum distance
		double maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		n.minD = minD;
		n.maxD = maxD;
		return n;
	}

	/**
	 * Calculate the distance between user and border. Create the new user and
	 * return the user
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param n_in
	 * @return
	 */
	@SuppressWarnings("unused")
	private static User copyUserWithDistToBorder(double x1, double x2,
			double y1, double y2, User n_in) {
		User n = new User(n_in.key, n_in.lon, n_in.lat);
		// Spitfire.print("Grid.insertCell(" + x1 + "," + y1 + ":"
		// + x2 + "," + y2 + ")");

		// Calculate the minimum distance
		double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		// Calculate the maximum distance
		double maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		n.minD = minD;
		n.maxD = maxD;
		return n;
	}

	/**
	 * Calculate the distance between user and border. NOT create the new user
	 * and return the user
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param n_in
	 * @return
	 */
	private static User getUserWithDistToBorder(double x1, double x2,
			double y1, double y2, User n) {
		// Spitfire.print("Grid.insertCell(" + x1 + "," + y1 + ":"
		// + x2 + "," + y2 + ")");

		// Calculate the minimum distance
		double minD = Trigonometry.pointToRectangleBoundaryMinDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		// Calculate the maximum distance
		double maxD = Trigonometry.pointToRectangleBoundaryMaxDistance(n.lon,
				n.lat, x1, y1, x2, y2);

		n.minD = minD;
		n.maxD = maxD;
		return n;
	}

	/**
	 * Create the grid cell. This function consist of two phases. BCL
	 * construction and Exchange phase.
	 * 
	 * @param neighborsList
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void populateCells(final List<User> neighborsList)
			throws IOException, ClassNotFoundException {
		/**
		 * IMPORTANT THE initRanges is call for main benchmarking class
		 */

		// initRanges();

		if (size != 0) {
			// ///////////////////////////////////////////////////////
			// Calclulate the time for constructing the BCLs
			// ///////////////////////////////////////////////////////
			long startTime = System.currentTimeMillis();
			// fakeFillHandoverLists(neighborsList);
			fillHandoverLists(neighborsList, c_mylb, c_myub, r_mylb, r_myub);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			Spitfire.print("BCL construction \t" + duration);

		}
		// ///////////////////////////////////////////////////////////////////////////
		// Set up communication buffers.
		// ///////////////////////////////////////////////////////////////////////////
		// slices = ObjectBuf.patchBuffers(m_Grid, r_ranges, c_ranges);
		// myslice = ObjectBuf.patchBuffer(m_Grid, r_myrange, c_myrange);

		// ///////////////////////////////////////////////////////////////////////////
		// Phase Two
		// Exchange BCLs
		// ///////////////////////////////////////////////////////////////////////////

		exchangeBCLs(neighborsList);

		// ***********************************
		// Gather all matrix row slices into process 0.
		// world.gather(0, myslice, slices);

		// Due to the MPI environment we should kill all the process after the
		// calculation

		// ***********************************
		// if (rank != 0)
		// System.exit(0);

	}

	public static void calculateHandoverListsSize() {
		double hsize = 0.0;
		for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
			HashMap<Integer, ServerBCS> list = adjacencyLists[i];

			if (list == null)
				continue;
			for (ServerBCS serverBCS : list.values()) {
				hsize += serverBCS.BCS.size();
				// Spitfire.print(rank + "-->" + serverBCS.rank
				// + "HandoverList's objects :\t" + serverBCS.BCS.size());
			}

		}

		Spitfire.print("HandoverList's objects :\t" + hsize);
		// Free unnecessary memory
		adjacencyLists = null;
		System.gc();
	}

	@SuppressWarnings("unused")
	private static void fakeFillHandoverLists(final List<User> neighborsList) {
		for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
			if (adjacencyLists[i] != null)
				for (ServerBCS serverBCS : adjacencyLists[i].values()) {
					// Broadcast the lists
					serverBCS.BCS.addAll(neighborsList);
				}
		}

	}

	private static void fillHandoverLists(final List<User> neighborsList,
			int c_mylb, int c_myub, int r_mylb, int r_myub) {
		// Create the distance comparator in order to sort the neighbors with
		// their maxD
		DistanceComparator dc = new DistanceComparator();

		/* compute minD and maxD of user to cell */
		// Create the cell if it is null
		// Spitfire.print("neighborsList.size = " + neighborsList.size());
		// Left
		if (adjacencyLists[LL] != null) {

			for (double start_point = r_mylb; start_point <= r_myub; start_point += db_lat) {
				User[] neighborsArray = new User[neighborsList.size()];
				int ii = 0;
				double x1 = c_mylb;
				double y1 = start_point;
				double x2 = c_mylb;
				double y2 = ((start_point + db_lat) < r_myub) ? (start_point + db_lat)
						: r_myub;
				for (User neighbor : neighborsList) {
					neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1,
							y2, neighbor);
				}

				SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1,
						y1, x2, y2);
				HashSet<User> set = new HashSet<User>(cell.getCandidates());

				// Iterate through the Left side Servers
				// System.out.println(i + ".BCS(" + set.size() + ")");

				for (ServerBCS serverBCS : adjacencyLists[LL].values()) {
					if (serverBCS.r_contains(start_point)
							|| serverBCS.r_contains(y2)) {
						serverBCS.BCS.addAll(set);
						serverBCS.sumPruned += cell.pruned;
						serverBCS.numPruned++;
						serverBCS.sumMaxD += cell.kth.maxD;
						serverBCS.numMaxD++;
					}
				}
				// if(rank ==8 || rank==7)
				debugCell(cell);

				// handoverLists[DL].addAll(set);
				// handoverLists[UL].addAll(set);
			}
		}
		// Right
		if (adjacencyLists[RR] != null)
			for (double start_point = r_mylb; start_point <= r_myub; start_point += db_lat) {
				User[] neighborsArray = new User[neighborsList.size()];
				int ii = 0;
				double x1 = c_myub;
				double y1 = start_point;
				double x2 = c_myub;
				double y2 = ((start_point + db_lat) < r_myub) ? (start_point + db_lat)
						: r_myub;

				for (User neighbor : neighborsList) {

					neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1,
							y2, neighbor);
				}
				SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1,
						y1, x2, y2);
				HashSet<User> set = new HashSet<User>(cell.getCandidates());

				// System.out.println(i + ".BCS(" + set.size() + ")");

				// Iterate through the Right side Servers
				for (ServerBCS serverBCS : adjacencyLists[RR].values()) {
					if (serverBCS.r_contains(start_point)
							|| serverBCS.r_contains(y2)) {
						serverBCS.BCS.addAll(set);
						serverBCS.sumPruned += cell.pruned;
						serverBCS.numPruned++;
						serverBCS.sumMaxD += cell.kth.maxD;
						serverBCS.numMaxD++;
					}
				}

				debugCell(cell);
				// handoverLists[DR].addAll(set);
				// handoverLists[UR].addAll(set);
			}
		// Up
		if (adjacencyLists[UU] != null)
			for (double start_point = c_mylb; start_point <= c_myub; start_point += db_lon) {
				User[] neighborsArray = new User[neighborsList.size()];
				int ii = 0;
				double x1 = start_point;
				double y1 = r_myub;
				double x2 = ((start_point + db_lon) < c_myub) ? (start_point + db_lon)
						: c_myub;
				double y2 = r_myub;
				for (User neighbor : neighborsList) {

					neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1,
							y2, neighbor);
				}
				SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1,
						y1, x2, y2);

				HashSet<User> set = new HashSet<User>(cell.getCandidates());

				// System.out.println(i + ".BCS(" + set.size() + ")");

				// Iterate through the Up side Servers
				for (ServerBCS serverBCS : adjacencyLists[UU].values()) {
					if (serverBCS.c_contains(start_point)
							|| serverBCS.c_contains(x2)) {
						serverBCS.BCS.addAll(set);
						serverBCS.sumPruned += cell.pruned;
						serverBCS.numPruned++;
						serverBCS.sumMaxD += cell.kth.maxD;
						serverBCS.numMaxD++;
					}
				}

				debugCell(cell);
				// handoverLists[UL].addAll(set);
				// handoverLists[UR].addAll(set);
			}
		// Down
		if (adjacencyLists[DD] != null)
			for (double start_point = c_mylb; start_point <= c_myub; start_point += db_lon) {
				User[] neighborsArray = new User[neighborsList.size()];
				int ii = 0;
				double x1 = start_point;
				double y1 = r_mylb;
				double x2 = ((start_point + db_lon) < c_myub) ? (start_point + db_lon)
						: c_myub;
				double y2 = r_mylb;
				for (User neighbor : neighborsList) {

					neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1,
							y2, neighbor);
				}
				SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1,
						x2, y1, y2);

				HashSet<User> set = new HashSet<User>(cell.getCandidates());

				// System.out.println(i + ".BCS(" + set.size() + ")");

				// Iterate through the Down side Servers
				for (ServerBCS serverBCS : adjacencyLists[DD].values()) {
					if (serverBCS.c_contains(start_point)
							|| serverBCS.c_contains(x2)) {
						serverBCS.BCS.addAll(set);
						serverBCS.sumPruned += cell.pruned;
						serverBCS.numPruned++;
						serverBCS.sumMaxD += cell.kth.maxD;
						serverBCS.numMaxD++;
					}
				}

				debugCell(cell);
			}

		filldDiagonalHandoverLists(neighborsList, c_mylb, c_myub, r_mylb,
				r_myub);

	}

	/**
	 * Debugging function that prints the cell content
	 * 
	 * @param cell
	 */
	private static void debugCell(SpitfireCell cell) {
		if (Spitfire.DEBUG) {
			for (User user : cell.getKset()) {
				if (user.minD == 0.0) {
					System.out
							.println(rank + ":" + user.key + ":" + user.minD
									+ ":" + user.maxD + ":" + user.lon + ","
									+ user.lat);
				} else {
					System.out.println(rank + ":" + user.key + ":" + user.minD
							+ ":" + user.maxD);
				}
			}
		}
	}

	/**
	 * 
	 * @param neighborsList
	 * @param c_mylb
	 * @param c_myub
	 * @param r_mylb
	 * @param r_myub
	 */
	private static void filldDiagonalHandoverLists(
			final List<User> neighborsList, int c_mylb, int c_myub, int r_mylb,
			int r_myub) {
		// Create the distance comparator in order to sort the neighbors with
		// their maxD
		DistanceComparator dc = new DistanceComparator();
		// Up Left

		if (adjacencyLists[UL] != null) {
			User[] neighborsArray = new User[neighborsList.size()];
			int ii = 0;
			double x1 = c_mylb;
			double y1 = r_myub;
			double x2 = c_mylb;
			double y2 = r_myub;
			for (User neighbor : neighborsList) {
				neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1, y2,
						neighbor);
			}
			SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1, y1,
					x2, y2);
			HashSet<User> set = new HashSet<User>(cell.getCandidates());

			// System.out.println("Corner.BCS(" + set.size() + ")");

			// Iterate through the Up Left side Servers
			for (ServerBCS serverBCS : adjacencyLists[UL].values()) {
				serverBCS.BCS.addAll(set);
				serverBCS.sumPruned += cell.pruned;
				serverBCS.numPruned++;
				serverBCS.sumMaxD += cell.kth.maxD;
				serverBCS.numMaxD++;
			}
			debugCell(cell);
		}

		// Up Right
		if (adjacencyLists[UR] != null) {
			User[] neighborsArray = new User[neighborsList.size()];
			int ii = 0;
			double x1 = c_myub;
			double y1 = r_myub;
			double x2 = c_myub;
			double y2 = r_myub;
			for (User neighbor : neighborsList) {
				neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1, y2,
						neighbor);
			}
			SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1, y1,
					x2, y2);
			HashSet<User> set = new HashSet<User>(cell.getCandidates());

			// System.out.println("Corner.BCS(" + set.size() + ")");

			// Iterate through the Up Right side Servers
			for (ServerBCS serverBCS : adjacencyLists[UR].values()) {
				serverBCS.BCS.addAll(set);
				serverBCS.sumPruned += cell.pruned;
				serverBCS.numPruned++;
				serverBCS.sumMaxD += cell.kth.maxD;
				serverBCS.numMaxD++;
			}
			debugCell(cell);
		}
		// Down Left
		if (adjacencyLists[DL] != null) {
			User[] neighborsArray = new User[neighborsList.size()];
			int ii = 0;
			double x1 = c_mylb;
			double y1 = r_mylb;
			double x2 = c_mylb;
			double y2 = r_mylb;
			for (User neighbor : neighborsList) {
				neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1, y2,
						neighbor);
			}
			SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1, y1,
					x2, y2);
			HashSet<User> set = new HashSet<User>(cell.getCandidates());

			// System.out.println("Corner.BCS(" + set.size() + ")");

			// Iterate through the Down Left side Servers
			for (ServerBCS serverBCS : adjacencyLists[DL].values()) {
				serverBCS.BCS.addAll(set);
				serverBCS.sumPruned += cell.pruned;
				serverBCS.numPruned++;
				serverBCS.sumMaxD += cell.kth.maxD;
				serverBCS.numMaxD++;
			}
			debugCell(cell);
		}
		// Down Right
		if (adjacencyLists[DR] != null) {
			User[] neighborsArray = new User[neighborsList.size()];
			int ii = 0;
			double x1 = c_myub;
			double y1 = r_myub;
			double x2 = c_myub;
			double y2 = r_myub;
			for (User neighbor : neighborsList) {
				neighborsArray[ii++] = getUserWithDistToBorder(x1, x2, y1, y2,
						neighbor);
			}
			SpitfireCell cell = new SpitfireCell(dc, neighborsArray, k, x1, y1,
					x2, y2);

			HashSet<User> set = new HashSet<User>(cell.getCandidates());

			// System.out.println("Corner.BCS(" + set.size() + ")");

			// Iterate through the Up Right side Servers
			for (ServerBCS serverBCS : adjacencyLists[DR].values()) {
				serverBCS.BCS.addAll(set);
				serverBCS.sumPruned += cell.pruned;
				serverBCS.numPruned++;
				serverBCS.sumMaxD += cell.kth.maxD;
				serverBCS.numMaxD++;
			}
			debugCell(cell);
		}
	}

	/**
	 * Exchange the bcl and construct the heaps
	 * 
	 * @param neighborsList
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static void exchangeBCLs(List<User> neighborsList)
			throws IOException, ClassNotFoundException {
		long startTime;
		long endTime;
		long duration;
		final HashSet<User> receivedUsers = new HashSet<User>();
		// Initialize the counter for time
		startTime = System.currentTimeMillis();
		// //////////////////////////////////////////////////////////////////////
		// Receive: Create one thread to run the multithreaded receive function
		// //////////////////////////////////////////////////////////////////////
		startTime = System.currentTimeMillis();
		// ArrayList<User> receivedUsers = receiveFromAllBCS();
		Runnable run = new Runnable() {

			public void run() {
				try {
					parallelReceiveFromAllBCS(receivedUsers);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(run);
		t.start();

		// //////////////////////////////////////////////////////////////////////
		// Send
		// //////////////////////////////////////////////////////////////////////

		sendToAllBCS();

		// //////////////////////////////////////////////////////////////////////
		// Construct the heaps after all BCS are received
		// //////////////////////////////////////////////////////////////////////
		// Wait until all BCLs are received
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;

		Spitfire.print(rank + ". |EC| received \t" + receivedUsers.size());

		Spitfire.print(rank + ".Communication time \t" + duration + "\tms");

		startTime = System.currentTimeMillis();
		HashSet<User> union = new HashSet<User>(receivedUsers.size()
				+ neighborsList.size());
		union.addAll(neighborsList);
		union.addAll(receivedUsers);
		constructHeaps(union);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		Spitfire.print("localkNNBuild time \t" + duration + "\tms");
	}

	/**
	 * After the BCLs are receive the construction of the heaps are the next
	 * thing on the list
	 * 
	 * @param union
	 */
	private static void constructHeaps(HashSet<User> union) {

		// Create the distance comparator in order to sort the neighbors with
		// their maxD
		DistanceComparator dc = new DistanceComparator();
		// CONSTRUCT THE HEAPS
		int i = 0;
		int j = 0;

		for (i = 0; i <= lon_cells; i++) {

			double x1 = i * lonSegments + c_mylb;

			double x2 = (i + 1) * lonSegments + c_mylb;
			if (lon_cells == i) {
				x2 = c_myub;
			}

			for (j = 0; j <= lat_cells; j++) {
				// compute minD and maxD of user to cell
				// Create the cell if it is null
				User[] neighborsArray = new User[union.size()];
				int ii = 0;
				// Transform the space into the geographical space to match with
				// user's longitude and latitude

				double y1 = j * latSegments + r_mylb;
				double y2 = (j + 1) * latSegments + r_mylb;
				if (lat_cells == j)
					y2 = r_myub;

				for (User user : union) {
					neighborsArray[ii++] = getUserWithDistToCell(x1, y1, x2,
							y2, user);
					// if (neighborsArray[ii - 1].key.equals("3760")) {
					// neighborsArray[ii - 1].minD=-neighborsArray[ii - 1].minD;
					// System.out.print(neighborsArray[ii - 1].key + ":["
					// + neighborsArray[ii - 1].minD + "]["
					// + neighborsArray[ii - 1].maxD + "]");
					// System.out.printf("x1:%d, y1:%d, x2:%d,y2:%d\n", x1,
					// y1, x2, y2);
					// }

				}
				m_Grid[i][j] = new SpitfireCell(dc, neighborsArray, k, x1, y1,
						x2, y2);
			}
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unused")
	private static HashSet<User> receiveFromAllBCS() throws IOException,
			ClassNotFoundException {

		// long startTime = 0;
		// long endTime = 0;
		// long duration = 0;
		HashSet<User> user = new HashSet<User>();

		// Receive from all the servers
		ObjectCompression<ArrayList<User>> compressionUtil = new ObjectCompression<ArrayList<User>>();

		for (int b = 0; b < size; b++) {
			if (b == rank)
				continue;

			ObjectItemBuf<byte[]> oiBuf = ObjectBuf.buffer();
			/*CommStatus status = */world.receive(null, oiBuf);
			// Spitfire.print(rank + "<--" + status.fromRank);
			if (oiBuf == null)
				continue;

			if (oiBuf.item == null)
				continue;
			user.addAll(compressionUtil.decompress(oiBuf.item));
			// HashSet<User> temp = new HashSet<User>();
			// // Decompress
			// temp.addAll(compressionUtil.decompress(oiBuf.item));
			//
			// startTime = System.currentTimeMillis();
			// Spitfire.print(rank + " Receive(" + temp.size() + ") from "
			// + (rank + 1));
			// for (User n_in : temp) {
			// // Place the Neighbor into the computed cell
			// for (int i = r_mylb; i <= r_myub; i++) {
			// for (int j = c_mylb; j <= c_myub; j++) {
			// //
			// //
			// Spitfire.print("SpitfireGrid.receiveFromAllBCS("+spitfireCell.size()+")");
			//
			// User n = calculateDistanceUserToCell(i, j, n_in);
			//
			// m_Grid[i][j].insert(n);
			// }
			// }
			//
			// }
			// endTime = System.currentTimeMillis();
			// duration += endTime - startTime;
		}
		// Spitfire.print("heap and candidate set computation time ["
		// + duration + "] ms");
		return user;
	}

	/**
	 * Multithreaded receive function
	 * 
	 * @param user
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static void parallelReceiveFromAllBCS(final HashSet<User> user)
			throws IOException, ClassNotFoundException {
		// long startTime = 0;
		// long endTime = 0;
		// long duration = 0;
		// Iterate though the server in order to create one message per server
		for (int r = 0; r < size; r++) {
			if (r == rank)
				continue;
			HashSet<User> sendSet = null;
			// Iterate for all the directions and combined the server message
			// sets
			for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
				if (adjacencyLists[i] != null) {
					ServerBCS sb = adjacencyLists[i].get(r);
					// If the hashmap has a serverBCS then that means that the
					// server is right direction and need to create for the
					// first time the set that will be send
					if (sb != null) {
						if (sendSet == null)
							sendSet = new HashSet<User>();
					}
				}
			}
			if (sendSet != null) {
				// Broadcast the lists
				receiversSize++;
			}

		}

		try {
			new ParallelTeam().execute(new ParallelRegion() {
				public void run() throws Exception {
					execute(0, receiversSize - 1, new IntegerForLoop() {
						public void run(int first, int last) throws IOException {
							ObjectCompression<ArrayList<User>> compressionUtil = new ObjectCompression<ArrayList<User>>();
							for (int r = first; r <= last; ++r) {
								// Spitfire.print(rank + "Send --> " + r);

								ObjectItemBuf<byte[]> oiBuf = ObjectBuf
										.buffer();
								/*CommStatus status = */world.receive(null,
										oiBuf);
								// Spitfire.print(rank + "<--"
								// + status.fromRank);
								if (oiBuf == null)
									continue;

								if (oiBuf.item == null)
									continue;
								try {
									synchronized (user) {
										user.addAll(compressionUtil
												.decompress(oiBuf.item));

									}
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}

							}
						}
					});
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void parallelSendToAllBCS() throws IOException {
		ObjectCompression<ArrayList<User>> compressionUtil = new ObjectCompression<ArrayList<User>>();

		ObjectBuf<byte[]> buf = null;

		try {
			new ParallelTeam().execute(new ParallelRegion() {
				public void run() throws Exception {
					execute(0, size - 1, new IntegerForLoop() {
						public void run(int first, int last) throws IOException {
							for (int r = first; r <= last; ++r) {
								// Spitfire.print(rank + "Send --> " + r);
								if (r == rank)
									continue;
								ObjectBuf<byte[]> buf = null;
								buf = ObjectBuf.buffer();
								world.send(r, buf, null);
							}
						}
					});
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send the BCLs to each server
	 * 
	 * @throws IOException
	 */
	private static void sendToAllBCS() throws IOException {
		// Spitfire.print("NEW");
		ObjectCompression<ArrayList<User>> compressionUtil = new ObjectCompression<ArrayList<User>>();
		ObjectBuf<byte[]> buf = null;

		// Iterate though the server in order to create one message per server
		for (int r = 0; r < size; r++) {
			if (r == rank)
				continue;
			HashSet<User> sendSet = null;
			// Iterate for all the directions and combined the server message
			// sets
			for (int i = 0; i < BORDER_CANDIDATE_LISTS_NUMBER; i++) {
				if (adjacencyLists[i] != null) {
					ServerBCS sb = adjacencyLists[i].get(r);
					// If the hashmap has a serverBCS then that means that the
					// server is right direction and need to create for the
					// first time the set that will be send
					if (sb != null) {
						if (sendSet == null)
							sendSet = new HashSet<User>();
						sendSet.addAll(sb.BCS);
						Spitfire.print(rank + "-" + i + "->" + r + "Prune\t"
								+ sb.sumPruned / sb.numPruned);
						Spitfire.print(rank + "-" + i + "->" + r + "AVG maxd\t"
								+ sb.sumMaxD / sb.numMaxD);
					}
				}
			}
			if (sendSet != null) {
				// Broadcast the lists
				buf = ObjectBuf.buffer(compressionUtil
						.compress(new ArrayList<User>(sendSet)));
				world.send(r, buf, null);
			}

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create a new grid
		// SpitfireGrid my_grid = null;
		// try {
		// my_grid = new SpitfireGrid(10, Grid.LOG_N, 0, 100000);
		//
		// createAdjacencyLists();
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

}
