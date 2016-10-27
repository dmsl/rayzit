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
