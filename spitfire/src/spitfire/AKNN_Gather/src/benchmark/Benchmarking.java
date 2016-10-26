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
package benchmark;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;

import proximity.datastructures.User;
import algorithms.BruteForce;
import algorithms.DistributedSpiralProximityGrid;
import algorithms.ParallelProximityGrid;
import algorithms.ProximityGrid;
import algorithms.SpiralProximityGrid;
import algorithms.Spitfire;
import couchbase.connector.CouchBaseData;
import datastructure.DistributedSpiralGrid;
import datastructure.SpitfireGrid;
import edu.rit.pj.Comm;

public class Benchmarking {

	/**
	 * 
	 * Verify any result
	 * 
	 * @param map1
	 * @param map2
	 * @return
	 */
	// World communicator.
	public static Comm world;
	static int size;
	static int rank;
	// private static DistributedProximityGrid dgr = new
	// DistributedProximityGrid();
	private static DistributedSpiralProximityGrid dgr;
	public static int r_mylb = 0;
	public static int c_mylb = 0;
	public static int r_myub = 180;
	public static int c_myub = 360;
	private static Spitfire spitfire;

	public static boolean verify(HashMap<String, List<User>> map1,
			HashMap<String, List<User>> map2) {
		if (map1.size() != map2.size()) {
			System.out.println("Mimatch Sizes : " + map1.size() + "!="
					+ map2.size());
			return false;
		}

		List<User> list1;
		List<User> list2;

		for (Entry<String, List<User>> entry : map1.entrySet()) {
			list2 = map2.get(entry.getKey());
			if (list2 == null)
				return false;
			list1 = entry.getValue();
			if (!equalLists(list1, list2)) {
				Gson gson = new Gson(); 
				System.out.println("USER:"+entry.getKey());
				return false;
			}
		}

		return true;

	}

	public static boolean equalLists(List<User> one, List<User> two) {
		if (one == null && two == null) {
			return true;
		}

		if ((one == null && two != null) || one != null && two == null
				|| one.size() != two.size()) {
			return false;
		}

		Gson gson = new Gson(); 
		
		for (int i = 0; i < one.size(); i++) {
			if (!one.get(i).key.equals(two.get(i).key)) {
				
				System.out.println("The correct (" + i + ")answer is "
						+ one.get(i).key);
				System.out.println("Your (" + i + ")answer is "
						+ two.get(i).key);
				
				System.out.println(gson.toJson(one));
				System.out.println(gson.toJson(two));
				return false;
			}
		}

		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CouchBaseData.setLoggingOff();

		// Initialize world communicator.
		try {
			Comm.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		world = Comm.world();
		rank = world.rank();
		size = world.size();

		int K = 16;

		if (rank == 0) {
			System.out.println("Getting Data ...");
		}
		long startTime = System.currentTimeMillis();
		List<User> allNeighborsList = CouchBaseData.getData();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		long total = duration;

		if (rank == 0) {
			System.out.println("Got " + allNeighborsList.size() + " users "
					+ " in [" + duration + "] ms");
		}

		startTime = System.currentTimeMillis();
		// ////////////////////////////////////////////////////////////////////////////
		// * IMPORTANT
		// * DistributedGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////
		// try {
		// dgr = new DistributedSpiralProximityGrid(allNeighborsList, K);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		try {
			spitfire = new Spitfire(allNeighborsList.size(), K, 2,0,1000000);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// ////////////////////////////////////////////////////////////////////////////
		List<User> neighborsList;
		if (size > 1) {

			if (SpitfireGrid.r_mylb == 0) {
				r_mylb = (int) Math.ceil(SpitfireGrid.r_mylb
						* SpitfireGrid.lonSegments);
				r_myub = (int) Math.ceil(SpitfireGrid.r_myub
						* SpitfireGrid.lonSegments);

			} else {
				r_mylb = (int) Math.ceil((SpitfireGrid.r_mylb - 1)
						* SpitfireGrid.lonSegments);
				r_myub = (int) Math.ceil((SpitfireGrid.r_myub + 1)
						* SpitfireGrid.lonSegments);
			}

			if (SpitfireGrid.c_mylb == 0) {
				c_mylb = (int) Math.ceil(SpitfireGrid.c_mylb
						* SpitfireGrid.latSegments);
				c_myub = (int) Math.ceil(SpitfireGrid.c_myub
						* SpitfireGrid.latSegments);
			} else {
				c_mylb = (int) Math.ceil((SpitfireGrid.c_mylb - 1)
						* SpitfireGrid.latSegments);
				c_myub = (int) Math.ceil((SpitfireGrid.c_myub + 1)
						* SpitfireGrid.latSegments);
			}

			// System.out.println("Number of cells:" + 360
			// / Math.log(allNeighborsList.size()) + "," + 180
			// / Math.log(allNeighborsList.size()));

			neighborsList = CouchBaseData.getData();

			// System.out.println(rank+".Getting partial Data ["+neighborsList.size()+"]");
			// DistributedSpiralGrid.handoverList = new HashSet<Neighbor>(
			// neighborsList);
		} else {
			neighborsList = allNeighborsList;
		}
		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * DistributedGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////

		// startTime = System.currentTimeMillis();
		//
		// dgr.buildingStructures(neighborsList, K);
		// HashMap<String, List<Neighbor>> DGRneighborsListSet = null;
		// // Distributed search not for all neighbors (allNeighborsList)
		// if (rank == 0)
		// DGRneighborsListSet = dgr.findAkNNs(allNeighborsList, K);
		// if (rank == 0) {
		// endTime = System.currentTimeMillis();
		// duration = endTime - startTime;
		// total += duration;
		// System.out.println("DistributedSpiralProximityGrid : Time ["
		// + duration + "] ms");
		// }

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * Spitfire benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////

		startTime = System.currentTimeMillis();

		spitfire.buildingStructures(neighborsList, K);

		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		System.out.println("Building Structures : Time [" + duration + "] ms");

		HashMap<String, List<User>> spitfireNeighborsListSet = null;

		// if (rank == 0) {
		startTime = System.currentTimeMillis();
		// Distributed search not for all neighbors (allNeighborsList)
		try {
			spitfireNeighborsListSet = spitfire.findAkNNs(allNeighborsList, K);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;

		System.out.println("Spitfire : Time [" + duration + "] ms");

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * Spitfire benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////

		// stop any running process
		if (rank != 0)
			System.exit(0);

		// Gson gson = new Gson();
		// System.out.println(gson.toJson(DGRneighborsListSet));

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * BruteForce benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////
		System.out.println("BruteForce is starting");
		BruteForce bf;
		HashMap<String, List<User>> BFneighborsListSet = null;

		bf = new BruteForce();
		startTime = System.currentTimeMillis();

		// Nothing to do
		bf.buildingStructures(allNeighborsList, K);

		BFneighborsListSet = bf.findAkNNs(allNeighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		total += duration;
		System.out.println("BruteForce : Time [" + duration + " ms]");

		// System.out.println(gson.toJson(BFneighborsListSet));
		if (verify(BFneighborsListSet, spitfireNeighborsListSet))
			System.out
					.println("0.[SUCCESS]:Your program has successfully executed the tests.");
		else
			System.out
					.println("0.[ERROR]:Your program has unsuccessfully executed the tests..");

		// if (verify(BFneighborsListSet, DGRneighborsListSet))
		// System.out
		// .println("1.[SUCCESS]:Your program has successfully executed the tests.");
		// else
		// System.out
		// .println("1.[ERROR]:Your program has unsuccessfully executed the tests..");
		// free any necessary memory
		// BFneighborsListSet = DGRneighborsListSet;
		// DGRneighborsListSet = null;

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * Grid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////
		// IMPORTANT: The transformation is already applied
		ProximityGrid gr;
		HashMap<String, List<User>> GRneighborsListSet = null;
		gr = new ProximityGrid();

		startTime = System.currentTimeMillis();

		gr.buildingStructures(allNeighborsList, K);

		GRneighborsListSet = gr.findAkNNs(allNeighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		total += duration;
		System.out.println("ProximityGrid : Time [" + duration + "] ms");
		// System.out.println(gson.toJson(GRneighborsListSet));

		if (verify(GRneighborsListSet, BFneighborsListSet))
			System.out
					.println("2.[SUCCESS]:Your program has successfully executed the tests.");
		else
			System.out
					.println("2.[ERROR]:Your program has unsuccessfully executed the tests..");
		// free any necessary memory
		BFneighborsListSet = GRneighborsListSet;
		GRneighborsListSet = null;

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * ParallelGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////
		// IMPORTANT: The transformation is already applied
		ParallelProximityGrid pgr;
		HashMap<String, List<User>> pGRneighborsListSet = null;
		pgr = new ParallelProximityGrid();

		startTime = System.currentTimeMillis();

		pgr.buildingStructures(allNeighborsList, K);

		pGRneighborsListSet = pgr.findAkNNs(allNeighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		total += duration;

		System.out
				.println("ParallelProximityGrid : Time [" + duration + "] ms");

		// Verify the results
		if (verify(BFneighborsListSet, pGRneighborsListSet))
			System.out
					.println("3.[SUCCESS]:Your program has successfully executed the tests.");
		else
			System.out
					.println("3.[ERROR]:Your program has unsuccessfully executed the tests.");
		// free any necessary memory
		pGRneighborsListSet = null;

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * SpiralGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////
		// IMPORTANT: The transformation is already applied
		SpiralProximityGrid spg;
		HashMap<String, List<User>> SPGneighborsListSet = null;
		spg = new SpiralProximityGrid();

		startTime = System.currentTimeMillis();

		spg.buildingStructures(allNeighborsList, K);

		SPGneighborsListSet = spg.findAkNNs(allNeighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		total += duration;

		System.out.println("SpiralGrid : Time [" + duration + "] ms");

		// Verify the results
		if (verify(BFneighborsListSet, SPGneighborsListSet))
			System.out
					.println("4.[SUCCESS]:Your program has successfully executed the tests.");
		else
			System.out
					.println("4.[ERROR]:Your program has unsuccessfully executed the tests..");
		// free any necessary memory
		SPGneighborsListSet = null;

		System.out.println("Total Time=" + total + "ms");

		System.exit(0);
	}

}
