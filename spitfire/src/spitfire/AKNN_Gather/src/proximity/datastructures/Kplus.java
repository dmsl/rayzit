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
/**
 * 
 */
package proximity.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Georgios Chatzimilioudis
 * 
 *         Implementation of k+structure. Characteristics of k+structure.
 *         consists of two structures: - K-heap. max-heap, max size k, and
 *         ordered by minD(user,cell) - boundary-list
 * 
 * 
 */
public class Kplus<T extends DoubleValueComparable<T>> implements Serializable {

	/* *************************************************************************
	 * Private member variables
	 * **************************************************** */

	/**
	 * 
	 */
	private static final long serialVersionUID = -8817190630702826595L;

	/**
	 * K-heap. max-heap, max size k, and ordered by minD(user,cell)
	 */
	private FibonacciHeapKMax<T> kheap = null;

	/**
	 * java.util.Collections.binarySearch(): This method runs in log(n) time for
	 * a "random access" list (which provides near-constant-time positional
	 * access). If the specified list does not implement the RandomAccess
	 * interface and is large, this method will do an iterator-based binary
	 * search that performs O(n) link traversals and O(log n) element
	 * comparisons.
	 * 
	 * Interface java.util.RandomAccess: The best algorithms for manipulating
	 * random access lists (such as ArrayList) can produce quadratic behavior
	 * when applied to sequential access lists (such as LinkedList). Generic
	 * list algorithms are encouraged to check whether the given list is an
	 * instanceof this interface before applying an algorithm that would provide
	 * poor performance if it were applied to a sequential access list, and to
	 * alter their behavior if necessary to guarantee acceptable performance.
	 * 
	 * java.util.ArrayList: Resizable-array implementation of the List
	 * interface. Implements all optional list operations, and permits all
	 * elements, including null. In addition to implementing the List interface,
	 * this class provides methods to manipulate the size of the array that is
	 * used internally to store the list. (This class is roughly equivalent to
	 * Vector, except that it is unsynchronized.) The size, isEmpty, get, set,
	 * iterator, and listIterator operations run in constant time. The add
	 * operation runs in amortized constant time, that is, adding n elements
	 * requires O(n) time. All of the other operations run in linear time
	 * (roughly speaking). The constant factor is low compared to that for the
	 * LinkedList implementation.
	 * 
	 * ArrayList is implementing RandomAccess while LinkedList implementing
	 * Queue.
	 * 
	 * ArrayList: The ArrayList is actually encapsulating an actualy Array, an
	 * Object[]. When you instanciate ArrayList, an array is created, and when
	 * you add values into it, the array changes its size accordingly. This
	 * gives you strengths and weaknesses: - Fast Random Access. You can perform
	 * random access without fearing for performence. Calling get(int) will just
	 * access the underlying array. - Adding values might be slow When you
	 * don’t know the amount of values the array will contain when you create
	 * it, a lot of shifting is going to be done in the memory space when the
	 * ArrayList manipulates its internal array. - Slow manipulation When
	 * you’ll want to add a value randomly inside the array, between two
	 * already existing values, the array will have to start moving all the
	 * values one spot to the right in order to let that happen.
	 * 
	 * LinkedList: - Fast manipulation. As you’d expect, adding and removing
	 * new data anywhere in the list is instantanious. Change two links, and you
	 * have a new value anywhere you want it. - No random access. Even though
	 * the get(int) is still there, it now just iterates the list until it
	 * reaches the index you specified. It has some optimizations in order to do
	 * that, but that’s basically it.
	 * 
	 * Proximity uses random access insertions. We want to insert an object at
	 * the right spot to keep the list sorted. This has two parts that are
	 * conflicting: 1. many random insertions -> linkedLists are efficient in
	 * this. ArrayLIsts are not 2. many random accesses needed for the binary
	 * search to find where to insert the new element -> ArrayLists are
	 * efficient. Linked Lists are not.
	 * 
	 * Implement ArrayList and scan for updates. change k+structure algorithm to
	 * check whether the new boundary area overlaps with the old boundary area
	 * (or the min-max area of the boundary elements). if it does then you need
	 * to call update and scan. else you just clear the boundary list.
	 * 
	 */
	/* Boundary-list (simple array list) */
	private ArrayList<T> boundaryList = new ArrayList<T>();

	/* U-list (simple array list) */
	private HashSet<T> UList = new HashSet<T>();
	
	
	/* Keeping track of duplicates  */
	private HashSet<String> duplicates = new HashSet<String>();

	/* ******* END of private member variables ******************************** */

	/* *************************************************************************
	 * Constructors / Destructors
	 * **************************************************** */

	public Kplus(int k) {
		this.kheap = new FibonacciHeapKMax<T>(k);
	}

	/* ******* END of constructors/destructors ******************************** */

	/* *************************************************************************
	 * Getters / Setters
	 * **************************************************** */

	/* ******* END of getters / setters ******************************** */

	/* *************************************************************************
	 * Functions
	 * **************************************************** */

	/**
	 * insert an element inside k+structure as defined in proximity (mdm'11)
	 * because class is generic key must be also entered separately (e.g. key =
	 * minD for proximity mdm'11) return true if the user was inserted into the
	 * heap return false if the user was rejected from the heap
	 */
	public boolean insert(T newElement, double key, boolean isInCell) {
		
		if(!duplicates.add(newElement.getId()))
			return true;
		
		if (isInCell) {
			this.UList.add(newElement);
		}
		/* check whether new element is inside K area
		 * new element < head of heap or if it is the first element to enter */
		else if (kheap.max() == null || key < kheap.max().getKey()) {

			T oldHead = this.kheap.insertMaxheap(newElement, key);

			/*
			 * update boundary list given evicted element from K set
			 * and the secondary value (maxD) of the new hkeap head (K set max)
			 */
			if (oldHead != null)
				this.updateBoundaryList(oldHead, kheap.max().getData()
						.getSecondaryKey());

		} // end if inside K set
		/* check whether new element is inside B area
		 * new element < head of heap maxD (secondary value) */
		else if (key < kheap.max().getData().getSecondaryKey()) {

			/* insert element inside the boundarylist */
			this.boundaryList.add(newElement);

		} else {
			/* else discard new element */
			return false;
		}
		return true;

	}

	/**
	 * update boundary list given the evicted element from the kheap and the
	 * maxD (secondary value) of the new head. stes: - calculate new boundary
	 * area given the maxD of the new head - if evicted element is not in the
	 * new boundary area then none of the elements in the old boundary list will
	 * be inside the boundary area. => clear list. - else if the maximum value
	 * of the old boundary list is still inside the new boundary area then just
	 * add the evicted element into the boundary list. - else scan old boundary
	 * list and delete all elements that fall outside the new boundary area.
	 * enter evicted element into boundary list.
	 * 
	 * we just take care of the extremes. even not taking care of them the time
	 * complexity would be exactly the same (O(n)) so maybe it is just an
	 * overkill.
	 * 
	 * @param oldHead
	 * @param boundaryEnd
	 */
	private void updateBoundaryList(T oldHead, double boundaryEnd) {

		// TODO: test

		/* check whether old head is not inside the new boundary area */
		if (oldHead.getKey() > boundaryEnd) {

			/* none of the boundary list elements is inside the boundary area */
			this.boundaryList.clear();

		} // end if old head is not inside new boundary area
		/* else scan list and evict any element outside the new boundary area */
		else {
			/* new boundary list */
			ArrayList<T> newBList = new ArrayList<T>();
			/* add evicted element from kheap into the new boundary list */
			newBList.add(oldHead);

			/* iterate boundary list */
			for (T current : this.boundaryList) {
				// ListIterator<T> iter = this.boundaryList.listIterator();
				// while ( iter.hasNext() ) {
				//
				// T current = iter.next();

				/* if current element is inside the new boundary area */
				if (current.getKey() <= boundaryEnd) {

					/* keep current element */
					newBList.add(current);

				}// end if current is inside boundary list
			}// end of old boundary list scan

			/* clear old boundary list */
			this.boundaryList.clear();
			/* use new boundary list */
			this.boundaryList.addAll(newBList);

		}

	}

	public int size() {
		return this.boundaryList.size() + this.kheap.size() + this.UList.size();
	}

	/**
	 * return an arraylist representation of all data inside the k+structure
	 */

	public ArrayList<T> getUList() {
		/* make ulist to result set */
		HashSet<T> result = this.UList;
		return new ArrayList<T>(result);
	}

	public ArrayList<T> getSortedBoundaryList() {
		/* make boundaryList to result set */
		ArrayList<T> result = this.boundaryList;
		Collections.sort(result);
		return new ArrayList<T>(result);
	}

	public ArrayList<T> getkheapList() {
		/* make boundaryList to result set */
		return new ArrayList<T>(this.kheap.toArrayList());
	}
	
	public ArrayList<T> toArrayList() {
		/* make ulist to result set */
		HashSet<T> result = this.UList;
		/* make kheap to result set */
		result.addAll(this.kheap.toArrayList());
		/* merge with boundary list */
		//result.addAll(this.boundaryList);

		return new ArrayList<T>(result);

	}
	
	public ArrayList<T> AllToArrayList() {
		/* make ulist to result set */
		HashSet<T> result = this.UList;
		/* make kheap to result set */
		result.addAll(this.kheap.toArrayList());
		/* merge with boundary list */
		result.addAll(this.boundaryList);
		
		return new ArrayList<T>(result);

	}
	
	public T getKth(){
		return this.kheap.max().data;
	}

	/* ******* END of functions ******************************** */
}
