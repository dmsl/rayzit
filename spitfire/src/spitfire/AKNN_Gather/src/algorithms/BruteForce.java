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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import proximity.datastructures.User;
import couchbase.connector.CouchBaseData;
import datastructure.Grid;

public class BruteForce implements Algorithm {

	public HashMap<String, List<User>> findAkNNs(
			List<User> neighborsList, int K) {
		// TODO Auto-generated method stub
		HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();

		// Create new list for each user

		for (int j = 0; j < neighborsList.size(); j++) {
			User n = (User) neighborsList.get(j);
			List<User> temp = new LinkedList<User>();
			List<User> tempList = neighborsList;
			// Get the cell
			// System.out.println("Grid.findAkNNs() in "+tempList.size());
			for (int k = 0; k < tempList.size(); k++) {
				User n_in = tempList.get(k);

//				if (n.key.equals(n_in.key))
//					continue;
				n_in.setDistance(n.lon, n.lat);

				temp.add(n_in);
			}
			Collections.sort(temp);
			neighborsListSet.put(n.key, temp.subList(0, K));

		}
		return neighborsListSet;
	}

	public void buildingStructures(List<User> neighborsList, int K) {
		// TODO Auto-generated method stub

	}

	public static void main(String args[]) {

		// Global variables

		int K = 2;
		int CellSize = Grid.LOG_N;

		if (args.length < 2) {
			System.err.println("prog <K> <CellSize>");
			System.exit(0);
		}

		try {
			K = Integer.parseInt(args[0]);
			CellSize = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("prog <K> <CellSize>");
			System.exit(0);
		}
		CouchBaseData.setLoggingOff();
		System.out.println("Getting Data ...");
		long startTime = System.currentTimeMillis();
		List<User> allNeighborsList = CouchBaseData.getData();
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		long total = duration;
		System.out.println("Got " + allNeighborsList.size() + " users "
				+ " in [" + duration + "] ms");
		
		startTime = System.currentTimeMillis();
		BruteForce bf = new BruteForce();

		bf.buildingStructures(allNeighborsList, K);
		HashMap<String, List<User>> BFneighborsListSet = null;
		// Centralized search
		BFneighborsListSet = bf.findAkNNs(allNeighborsList, K);

		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		total = duration;
		System.out.println("Brute Force : Time [" + duration + "] ms");
		
//		for (Entry<String, List<User>> knn : BFneighborsListSet
//				.entrySet()) {
//			System.err.print(knn.getKey() + "\t");
//			for (User neighbor : knn.getValue()) {
//				System.err.print(neighbor.key/* + "|" + neighbor.minD*/ + "\t");
//			}
//			System.err.println();
//		}
		
		System.exit(0);
	}

	public void buildingStructures(List<User> neighborsList, int K,
			int cellSize) {
		// TODO Auto-generated method stub

	}

}
