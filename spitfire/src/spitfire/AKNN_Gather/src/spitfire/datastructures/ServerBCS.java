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
