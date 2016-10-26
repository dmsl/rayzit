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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import proximity.datastructures.User;
import benchmark.AllBenchmarking;
import benchmark.Benchmarking;
import couchbase.connector.CouchBaseData;
import datastructure.AllDistributedSpiralGrid;
import datastructure.Cell;
import datastructure.DistributedSpiralGrid;
import datastructure.Grid;
import edu.rit.mp.ObjectBuf;
import edu.rit.pj.Comm;
import edu.rit.util.Range;

public class AllDistributedSpiralProximityGrid implements Algorithm {

	private AllDistributedSpiralGrid m_Grid;

	public AllDistributedSpiralProximityGrid(List<User> neighborsList, int k) throws IOException {
		// Create the grid
		m_Grid = new AllDistributedSpiralGrid(neighborsList.size(), k,Grid.LOG_N);

			AllDistributedSpiralGrid.initRanges();
		
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

	public AllDistributedSpiralProximityGrid() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public HashMap<String, List<User>> findAkNNs(
			List<User> neighborsList, int K) {
		HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();
		int k;
		
		for (User n : neighborsList) {
			
			List<User> temp = new LinkedList<User>();

			Cell c = m_Grid.getCell(n);

			if (c == null)
				continue;

			List<User> tempList = c.AllToArrayList();
			// Get the cell
			for (User n_in : tempList) {				

				if (n.key.equals(n_in.key))
					continue;
				n_in.setDistance(n.lon, n.lat);

				temp.add(n_in);
			}
			Collections.sort(temp);

			if (temp.size() < K)
				k = temp.size();
			else
				k = K;

			neighborsListSet.put(n.key, temp.subList(0, k));

		}

		return neighborsListSet;
	}

	public void buildingStructures(List<User> neighborsList, int K) {
		// Create the grid
		m_Grid = new AllDistributedSpiralGrid(neighborsList.size(), K,
				Grid.LOG_N);

		try {
			m_Grid.insertCell(neighborsList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void buildingStructures(List<User> neighborsList, int K,
			int cellSize) {
		// Create the grid
		m_Grid = new AllDistributedSpiralGrid(neighborsList.size(), K, cellSize);

		try {
			m_Grid.insertCell(neighborsList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {

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

		// Initialize world communicator.
		try {
			Comm.init(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<User> neighborsList = CouchBaseData.getData();

		AllDistributedSpiralProximityGrid dgr = new AllDistributedSpiralProximityGrid(neighborsList, K);

		@SuppressWarnings("unused")
		HashMap<String, List<User>> proxg = null;

		long startTime = System.currentTimeMillis();

		dgr.buildingStructures(neighborsList, K, CellSize);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Building Structures : Time [" + duration + "] ms");

		startTime = System.currentTimeMillis();
		proxg = dgr.findAkNNs(neighborsList, K);
		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		System.out.println("AllDistributedSpiralProximityGrid Search : Time [" + duration + "] ms");

		System.exit(0);
	}

}
