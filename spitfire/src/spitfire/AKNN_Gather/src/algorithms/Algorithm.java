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

import java.util.HashMap;
import java.util.List;

import proximity.datastructures.User;

public interface Algorithm {
	public 	HashMap<String, List<User>> findAkNNs(List<User> neighborsList,int K);
	public 	void buildingStructures(List<User> neighborsList,int K,int cellSize);
	public 	void buildingStructures(List<User> neighborsList,int K);
}
