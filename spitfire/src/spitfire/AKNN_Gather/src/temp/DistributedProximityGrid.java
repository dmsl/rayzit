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
package temp;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import proximity.datastructures.User;

import algorithms.Algorithm;
import benchmark.Benchmarking;

import com.google.gson.Gson;

import couchbase.connector.CouchBaseData;
import datastructure.Cell;
import edu.rit.mp.ObjectBuf;
import edu.rit.pj.Comm;
import edu.rit.util.Range;

public class DistributedProximityGrid implements Algorithm {

	private DistributedGrid m_Grid;

	private static HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();

	// Shared variables.

	// World communicator.
	static Comm world;
	static int size;
	static int rank;

	// Number of nodes.
	static int n;

	// Fill the Grid.
	static Range[] ranges;
	static Range myrange;
	static int mylb;
	static int myub;
	// Communication buffers.
	static ObjectBuf<Cell>[] slices;
	static ObjectBuf<Cell> myslice;

	public DistributedProximityGrid(List<User> neighborsList, int k) {
		// TODO Auto-generated constructor stub
		// Transform 'lon' and 'lat' to positive
		// for (Iterator<Neighbor> iterator = neighborsList.iterator(); iterator
		// .hasNext();) {
		// Neighbor neighbor = (Neighbor) iterator.next();
		// neighbor.lat += 90;
		// neighbor.lon += 180;
		//
		// }

	}

	public DistributedProximityGrid() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public HashMap<String, List<User>> findAkNNs(
			List<User> neighborsList, int K) {
		/*HashMap<String, List<Neighbor>> neighborsListSet = new HashMap<String, List<Neighbor>>(); */

		if (DistributedGrid.BENCH) {

			// Initialize arguments
			String[] args = new String[0];
			// // Initialize world communicator.
			try {
				Comm.init(args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			world = Comm.world();
		} else
			world = Benchmarking.world;

		size = world.size();
		rank = world.rank();

		ranges = new Range(0, DistributedGrid.lon_bound - 1).subranges(size);
		myrange = ranges[rank];
		mylb = myrange.lb();
		myub = myrange.ub();

		for (int j = 0; j < neighborsList.size(); j++) {
			User n = (User) neighborsList.get(j);
			List<User> temp = new LinkedList<User>();

			if (!myrange.contains((int) (n.lon / DistributedGrid.lonStride)))
				continue;

			List<User> tempList = m_Grid.getCell(n).toArrayList();
			// Get the cell
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
		m_Grid = new DistributedGrid(neighborsList.size(), K);

		try {
			m_Grid.insertCell(neighborsList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		int K = 2;
		CouchBaseData.setLoggingOff();

		List<User> neighborsList = CouchBaseData.getData();

		DistributedProximityGrid gr = new DistributedProximityGrid(
				neighborsList, K);

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
