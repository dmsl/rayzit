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
package datastructure;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;

import proximity.datastructures.User;
import spitfire.datastructures.ArrayHeap;
import utils.Trigonometry;

public class SpitfireCell extends ArrayHeap implements Serializable {

	/**
	 * K-heap. max-heap, max size k, and ordered by minD(user,cell)
	 */
	private HashSet<User> kSet = null;
	private HashSet<User> outsiders_Set;
	private HashSet<User> insiders_Set;
	private HashSet<User> candidate_Set;
	// Lower Left and Upper Right corner
	public double llx;
	public double lly;
	public double urx;
	public double ury;

	public User kth;
	public int pruned = 0;

	/**
	 * Insert all the users beside k users
	 * 
	 * @param user
	 */
	private void insertRest(User user) {

		// //Neighbor inside cell then add it to the inside set
		if (isInCell(user)) {
			insiders_Set.add(user);
		} else {
			if (kth == null || user.minD < kth.maxD) {
				outsiders_Set.add(user);
			}
			// Else reject
			else {
				pruned++;
			}
		}

	}

	/**
	 * Check if a user is in cell
	 * 
	 * @param n
	 * @return
	 */
	private boolean isInCell(User n) {
		return (n.lon >= llx && n.lon <= urx && n.lat >= lly && n.lat <= ury);

	}

	/**
	 * Create New empty Cell
	 * 
	 * @param cmp
	 * @param k
	 */
	public SpitfireCell(Comparator<?> cmp, int k) {
		super(cmp);
		outsiders_Set = new HashSet<User>();
	}

	/**
	 * Create New Cell with the with the given neighbors
	 * 
	 * @param comparator
	 * @param neighbors
	 * @param k
	 * @param llx
	 * @param lly
	 * @param urx
	 * @param ury
	 */
	public SpitfireCell(Comparator<?> comparator, User[] neighbors, int k,
			double llx, double lly, double urx, double ury) {
		// Create the heap
		super(comparator, neighbors);

		// Set the location of the cell
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;

		outsiders_Set = new HashSet<User>();
		insiders_Set = new HashSet<User>();
		kSet = new HashSet<User>(k);
		// Given the kth distance the candidate_set will constructed
		kth = new User(extractItem());
		kSet.add(kth);

		// Create k-heap
		// Copy each user to maxheap to store minD
		for (int i = 0; i < k - 1 && i < size(); i++) {

			// Check k set to find the maximum distance
			User copyUser = new User(extractItem());
			kSet.add(copyUser);

			if (kth.maxD < copyUser.maxD)
				kth = copyUser;
		}

		// Insert the rest of the users to the inside and outside list
		for (User neighbor : getElements()) {
			insertRest(neighbor);
		}
	}

	/**
	 *  
	 */
	private static final long serialVersionUID = -5090279854402026337L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Spitfire.main()");

		User[] neighbors = new User[1000000];

		for (int i = 0; i < neighbors.length; i++) {
			neighbors[i] = new User("User " + i, i * -100, i * -100, i, -i + 10);
		}

		// SpitfireCell ah = new SpitfireCell(new DistanceComparator(),
		// neighbors,
		// 10);

		// System.out.println("Min value " + ah.extractItem().maxD);

	}

	/**
	 * Get all the candidates users. Calculate the candidate set only one time
	 * 
	 * @return
	 */
	public HashSet<User> getCandidates() {

		if (candidate_Set == null) {
			candidate_Set = new HashSet<User>(outsiders_Set);
			candidate_Set.addAll(insiders_Set);
			candidate_Set.addAll(kSet);
		}

		return candidate_Set;
	}

	/**
	 * 
	 * @return
	 */
	public HashSet<User> getInsiders() {
		return insiders_Set;
	}

	/**
	 * 
	 * @return
	 */
	public HashSet<User> getOutsides() {
		return outsiders_Set;
	}

	/**
	 * 
	 * @return
	 */
	public HashSet<User> getKset() {
		return kSet;
	}

	public double getDistance(User user) {
		return Trigonometry.pointToRectangleBoundaryMinDistance(user.lon,
				user.lat, llx, lly, urx, ury);
	}

}
