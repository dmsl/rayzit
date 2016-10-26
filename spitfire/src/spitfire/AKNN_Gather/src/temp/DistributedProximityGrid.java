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
