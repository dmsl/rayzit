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
