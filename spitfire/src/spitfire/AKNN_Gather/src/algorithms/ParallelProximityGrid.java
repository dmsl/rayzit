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
