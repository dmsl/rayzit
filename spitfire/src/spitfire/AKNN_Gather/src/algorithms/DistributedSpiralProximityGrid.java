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
