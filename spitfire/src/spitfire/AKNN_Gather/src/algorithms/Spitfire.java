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
package algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import proximity.datastructures.FibonacciHeapKMax;
import proximity.datastructures.FibonacciHeapNode;
import proximity.datastructures.User;
import benchmark.Benchmarking;
import couchbase.connector.CouchBaseData;
import datastructure.Grid;
import datastructure.SpitfireCell;
import datastructure.SpitfireGrid;
import edu.rit.pj.Comm;

public class Spitfire implements Algorithm {

	public static final boolean DEBUG = false;
	
	private SpitfireGrid m_Grid;

	// Shared variables.

	// World communicator.
	static Comm world;
	static int size;
	static int rank;

	// Number of nodes.
	// static int n;

	public static void print(String msg) {
		System.err.println(msg);
	}

	// Create a field for the main function

	private static Spitfire spitfire;

	/**
	 * Gets the parameters and make a spifiregrid (ArrayHeap)
	 * 
	 * @param numberOfUsers
	 * @param k
	 * @param cellSize
	 * @param c
	 * @throws IOException
	 */
	public Spitfire(int numberOfUsers, int k, int cellSize, double c, int n)
			throws IOException {
		// /////////////////////////////////////////////////////
		// Create the grid
		// /////////////////////////////////////////////////////

		// Dynamic division grid

		// print("numberOfUsers={"+numberOfUsers+"}"+"k={"+k+"}"+"cellSize={"+cellSize+"}"+"d_b_size={"+d_b_size+"}"+"n={"+n+"}");
		try {
			m_Grid = new SpitfireGrid(k, cellSize, c, n);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Default constructor
	 */
	public Spitfire() {
	}

	/**
	 * Create a k-heap to find the end result set
	 * 
	 * @param neighborsList
	 * @param k
	 */
	public HashMap<String, List<User>> findAkNNs(List<User> neighborsList, int K) {

		HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();
		boolean continue_to_next_u = false;

		/*
		 * Iterate through all the users
		 * */
		for (User u : neighborsList) {

			FibonacciHeapKMax<User> kheap = new FibonacciHeapKMax<User>(K);

			SpitfireCell c = SpitfireGrid.getCell(u);

			if (c == null)
				continue;

			// Step one Get the insider and create a k-maxheap
			// Copy the user because if the user are altered the original
			// objects in iset will be altered too

			HashSet<User> iset = c.getInsiders();

			for (User n_in : iset) {
				// Don't create user if you don't need the actual value but only
				// the id
				User user = n_in;// new User(n_in);
				user.setDistance(u.lon, u.lat);
				kheap.insertMaxheap(user, user.minD);
			}

			// TODO: CHECK IF KSET SCANNING IS NEEDED

			ArrayList<User> klist = new ArrayList<User>(c.getKset());

			// Generate an iterator. Start just after the last element.
			ListIterator<User> li = klist.listIterator(klist.size());

			// Iterate in reverse.
			while (li.hasPrevious()) {

				// Get the kth from kset
				// Create new kth in order to keep the distance factor of the
				// kset's users
				User kth = new User(li.previous());

				// Get the kth from KNN in iset
				// Complete the kheap

				FibonacciHeapNode<User> root_u = kheap.max();
				if (root_u == null || kheap.size() <= K) {
					kth.setDistance(u);
					kheap.insertMaxheap(kth, kth.minD);
					continue;
				}

				User kth_u = root_u.getData();
				// Calculate the u to kset distance
				double dist_u_kset = c.getDistance(u) - kth.minD;
				// Check the distance
				if (kth_u.getDistance(u) < dist_u_kset) {
					continue_to_next_u = true;
					break;// and continue with next n
				} else {
					kth.setDistance(u);
					kheap.insertMaxheap(kth, kth.minD);
				}

			}

			// Check if user u is satisfied and move to the next user
			if (continue_to_next_u) {
				neighborsListSet.put(u.key, kheap.toArrayList());
				continue;
			}

			// Get the kth from KNN in iset and kset
			FibonacciHeapNode<User> root_u = kheap.max();
			if (root_u != null) {
				User kth_u = root_u.getData();
				// Calculate the u to oset distance
				double dist_u_oset = c.getDistance(u) + c.kth.minD;

				// Step three
				if (kth_u.getDistance(u) < dist_u_oset) {
					neighborsListSet.put(u.key, kheap.toArrayList());
					continue;
				} else {
					HashSet<User> oset = c.getOutsides();
					for (User oUser : oset) {
						User user = oUser;// new User(oUser);
						user.setDistance(u.lon, u.lat);
						kheap.insertMaxheap(user, user.minD);
					}

				}
			} else {
				HashSet<User> oset = c.getOutsides();
				for (User oUser : oset) {
					User user = oUser;// new User(oUser);
					user.setDistance(u.lon, u.lat);
					kheap.insertMaxheap(user, user.minD);
				}

			}

			neighborsListSet.put(u.key, kheap.toArrayList());

		}
		return neighborsListSet;
	}

	/**
	 * Create the heap for each cell of the grid
	 * 
	 * @param neighborsList
	 * @param k
	 * @param cellSize
	 */
	public void buildingStructures(List<User> neighborsList, int K, int cellSize) {

		try {
			m_Grid.populateCells(neighborsList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create the heap for each cell of the grid
	 * 
	 * @param neighborsList
	 * @param k
	 */
	public void buildingStructures(List<User> neighborsList, int K) {

		try {
			m_Grid.populateCells(neighborsList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		int K = 2;
		int CellSize = Grid.LOG_N;

		// d_b = c*sqrt(kA/(nð))
		double c = Grid.LOG_N;
		int numberOfUsers = 1000;
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		if (args.length < 3) {
			print("prog <K> <CellSize> <numberOfUsers> <d_b_size>");
			System.exit(0);
		}

		try {
			K = Integer.parseInt(args[0]);
			CellSize = Integer.parseInt(args[1]);
			numberOfUsers = Integer.parseInt(args[2]);
			c = Double.parseDouble(args[3]);
		} catch (Exception e) {
			print("prog <K> <CellSize> <numberOfUsers> <d_b_size>");
			System.exit(0);
		}
		CouchBaseData.setLoggingOff();

		// Initialize world communicator.
		try {
			Comm.init(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		world = Comm.world();
		Benchmarking.world = world;
		rank = world.rank();
		size = world.size();

		if (rank == 0) {
			print("Getting Data ...");
		}

		startTime = System.currentTimeMillis();
		try {
			spitfire = new Spitfire(numberOfUsers, K, CellSize, c,
					numberOfUsers);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		print("Create adjacency matrix : Time \t" + duration + "\t ms");
		// ////////////////////////////////////////////////////////////////////////////
		// Get partial data for each Server
		// ////////////////////////////////////////////////////////////////////////////

		List<User> neighborsList = null;
		if (size > 1) {
			if (rank == 0) {
				print("Getting partial Data ...");
			}
			Benchmarking.r_mylb = SpitfireGrid.r_mylb;
			Benchmarking.r_myub = SpitfireGrid.r_myub;
			Benchmarking.c_mylb = SpitfireGrid.c_mylb;
			Benchmarking.c_myub = SpitfireGrid.c_myub;

			// print("BBOX:" + Benchmarking.c_mylb + "-"
			// + Benchmarking.c_myub + "," + Benchmarking.r_mylb + "-"
			// + Benchmarking.r_myub);

			// print("Number of cells:" + 360
			// / Math.log(allNeighborsList.size()) + "," + 180
			// / Math.log(allNeighborsList.size()));

			// ParallelDistributedSpiralGrid.handoverList = new
			// HashSet<Neighbor>(
			// neighborsList);
		}

		startTime = System.currentTimeMillis();
		neighborsList = CouchBaseData.getData();
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		print("Fetch Data(" + neighborsList.size() + ") : Time \t" + duration
				+ "\t ms");

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * DistributedGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////

		startTime = System.currentTimeMillis();

		spitfire.buildingStructures(neighborsList, K);

		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		print("Building Structures : Time \t" + duration + "\t ms");
		// Print the size of the handover list
		SpitfireGrid.calculateHandoverListsSize();

		@SuppressWarnings("unused")
		HashMap<String, List<User>> spitfireNeighborsListSet = null;

		// if (rank == 0) {
		startTime = System.currentTimeMillis();
		// Distributed search not for all neighbors (allNeighborsList)
		spitfireNeighborsListSet = spitfire.findAkNNs(neighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;

		print("Spitfire : Time \t" + duration + "\t ms");
		// }

		// for (Entry<String, List<User>> knn : spitfireNeighborsListSet
		// .entrySet()) {
		// System.out.print(knn.getKey() + "\t");
		// for (User neighbor : knn.getValue()) {
		// System.out.print(neighbor.key /*+ "|" + neighbor.minD*/+ "\t");
		// }
		// System.out.println();
		// }

		/* Total memory currently in use by the JVM */
		print("Total memory : \t" + Runtime.getRuntime().totalMemory()
				+ "\t(bytes)");

		// /////////////////////////////////////////////////////////////////////////////////////////////////////////
		// STOP HERE
		// /////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.exit(0);

	}
}
