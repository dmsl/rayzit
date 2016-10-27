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

import proximity.datastructures.User;

import couchbase.connector.CouchBaseData;
import datastructure.Cell;
import datastructure.Grid;
import datastructure.SpiralGrid;

public class SpiralProximityGrid implements Algorithm {

	private SpiralGrid m_Grid;

	/**
	 * @param args
	 */
	public HashMap<String, List<User>> findAkNNs(
			List<User> neighborsList, int K) {
		HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();

		for (int j = 0; j < neighborsList.size(); j++) {
			User n = (User) neighborsList.get(j);
			List<User> temp = new LinkedList<User>();
			// Get the Cell of the n
			Cell c = m_Grid.getCell(n);
		
			// Get the cell
			// /////////////////////////////////////////////
			// Ulist + Kheap + Boundary List
			// ////////////////////////////////////////////
			List<User> tempList = c.AllToArrayList();
			// System.out.println("Grid.findAkNNs() in "+tempList.size());
			for (int k = 0; k < tempList.size(); k++) {
				User n_in = tempList.get(k);

				if (n.key.equals(n_in.key))
					continue;
				n_in.setDistance(n.lon, n.lat);

				temp.add(n_in);

			}
	
			Collections.sort(temp);
			neighborsListSet.put(n.key, temp.subList(0, K));

		}

		return neighborsListSet;
	}
	
	public void buildingStructures(List<User> neighborsList, int K) {
		buildingStructures(neighborsList, K, Grid.LOG_N);
	}
		public void buildingStructures(List<User> neighborsList, int K,
			int cellSize) {
		// TODO Auto-generated method stub
			// Create the grid
			m_Grid = new SpiralGrid(
					neighborsList.size(), K,cellSize);

			for (int j = 0; j < neighborsList.size(); j++) {
				User neighbor = neighborsList.get(j);
				m_Grid.insertCell(neighbor);
			}
	}
		
	public static void main(String[] args) {


		int K = 2;
		int CellSize = Grid.LOG_N;

		if (args.length < 2) {
			System.err.println("prog <K> <CellSize>");
			System.exit(0);
		}

		try {
			K = Integer.parseInt(args[0]);
			CellSize = Integer.parseInt(args[1]);
			;
		} catch (Exception e) {
			System.err.println("prog <K> <CellSize>");
			System.exit(0);
		}

		CouchBaseData.setLoggingOff();

		List<User> neighborsList = CouchBaseData.getData();

		// ////////////////////////////////////////////////////////////////////////////
		// *
		// * ProximityGrid benchmarking
		// *
		// ////////////////////////////////////////////////////////////////////////////

		SpiralProximityGrid spgr = new SpiralProximityGrid();
		

		@SuppressWarnings("unused")
		HashMap<String, List<User>> proxg = null;

		long startTime = System.currentTimeMillis();

		spgr.buildingStructures(neighborsList, K,CellSize);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Building Structures : Time [" + duration + "] ms");

		startTime = System.currentTimeMillis();
		proxg = spgr.findAkNNs(neighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		System.out.println("SpiralProximityGrid Search : Time [" + duration + "] ms");

		System.exit(0);
	}



	



}
