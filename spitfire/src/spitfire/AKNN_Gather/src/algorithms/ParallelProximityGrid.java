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


import com.google.gson.Gson;

import couchbase.connector.CouchBaseData;
import datastructure.Cell;
import datastructure.ParallelGrid;

public class ParallelProximityGrid implements Algorithm {

	private ParallelGrid m_Grid;


	
	public ParallelProximityGrid(List<User> neighborsList, int k) {
		// TODO Auto-generated constructor stub
		// Transform 'lon' and 'lat' to positive
//		for (Iterator<Neighbor> iterator = neighborsList.iterator(); iterator
//				.hasNext();) {
//			Neighbor neighbor = (Neighbor) iterator.next();
//			neighbor.lat += 90;
//			neighbor.lon += 180;
//
//		}

	}

	public ParallelProximityGrid() {
		// TODO Auto-generated constructor stub
	}

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
		// Create the grid
		m_Grid = new ParallelGrid(neighborsList.size(), K);

		m_Grid.insertCell(neighborsList);

	}

	public static void main(String[] args) {

		int K = 2;
		CouchBaseData.setLoggingOff();

		List<User> neighborsList = CouchBaseData.getData();

		ParallelProximityGrid gr = new ParallelProximityGrid(neighborsList, K);

		gr.buildingStructures(neighborsList, K);

		HashMap<String, List<User>> neighborsListSet = gr.findAkNNs(
				neighborsList, K);

		Gson gson = new Gson();
		// convert java object to JSON format,
		// and returned as JSON formatted string
		String json = gson.toJson(neighborsListSet);

		System.out.println(json);

		System.exit(0);
	}

	public void buildingStructures(List<User> neighborsList, int K,
			int cellSize) {
		// TODO Auto-generated method stub
		
	}

}
