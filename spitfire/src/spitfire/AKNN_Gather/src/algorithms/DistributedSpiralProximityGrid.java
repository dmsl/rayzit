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
import utils.Trigonometry;

import com.google.gson.Gson;

import couchbase.connector.CouchBaseData;
import datastructure.Cell;
import datastructure.DistributedSpiralGrid;
import datastructure.Grid;

public class DistributedSpiralProximityGrid implements Algorithm {

	private DistributedSpiralGrid m_Grid;

	public DistributedSpiralProximityGrid(List<User> neighborsList, int K)
			throws IOException {
		// TODO Auto-generated constructor stub
		// Create the grid
		m_Grid = new DistributedSpiralGrid(neighborsList.size(), K);
		DistributedSpiralGrid.initRanges();
	}

	public DistributedSpiralProximityGrid() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public HashMap<String, List<User>> findAkNNs(
			List<User> neighborsList, int K) {
		HashMap<String, List<User>> neighborsListSet = new HashMap<String, List<User>>();
		int k;
		Gson gson= new Gson();
		for (User n : neighborsList) {

			List<User> temp = new LinkedList<User>();

			Cell c = m_Grid.getCell(n);

			if (c == null)
				continue;

			List<User> tempList = c.AllToArrayList();
			// Get the cell
			//System.out.println("head in "+gson.toJson(c.getKth()));
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

		try {
			m_Grid.insertCell(neighborsList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		int K = 2;
		CouchBaseData.setLoggingOff();

		List<User> neighborsList = CouchBaseData.getData();

		DistributedSpiralProximityGrid dspg = null;
		try {
			dspg = new DistributedSpiralProximityGrid(neighborsList, K);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (dspg != null) {
			dspg.buildingStructures(neighborsList, K);

			HashMap<String, List<User>> neighborsListSet = dspg.findAkNNs(
					neighborsList, K);

			Gson gson = new Gson();
			// convert java object to JSON format,
			// and returned as JSON formatted string
			String json = gson.toJson(neighborsListSet);

			System.out.println(json);
		}
		System.exit(0);
	}

	public void buildingStructures(List<User> neighborsList, int K,
			int cellSize) {
		// TODO Auto-generated method stub

	}

}
