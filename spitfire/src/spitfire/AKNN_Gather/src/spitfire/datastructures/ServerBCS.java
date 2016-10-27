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
package spitfire.datastructures;

import java.io.Serializable;
import java.util.HashSet;

import edu.rit.util.Range;

import proximity.datastructures.User;

public class ServerBCS implements Serializable {

	public HashSet<User> BCS;
	public int rank;
	public Range r_range;
	public Range c_range;
	public int sumPruned = 0;
	public int numPruned = 0;
	public int numMaxD = 0;
	public double sumMaxD = 0;
	/**
	 * Add the border candidate set for a server with specific id(rank)
	 * 
	 * @param BCS
	 * @param rank
	 */
	public ServerBCS(HashSet<User> BCS, int rank, Range r_range, Range c_range) {
		this.BCS = BCS;
		this.rank = rank;
		this.r_range = r_range;
		this.c_range = c_range;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3249385362387829466L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public boolean r_contains(double start_point) {
		return r_range.lb()<=start_point && r_range.ub()>=start_point;	
	}

	public boolean c_contains(double start_point) {
		return  c_range.lb()<=start_point && c_range.ub()>=start_point;
	}

}
