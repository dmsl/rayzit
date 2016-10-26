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
import java.util.ArrayList;
import java.util.Comparator;

import proximity.datastructures.User;

/**
 * Array based implementation of Heap Data Structure. The nodes of binary tree
 * of the heap are stored in a lenear structure.
 * 
 * @author V.Boutchkova
 * @version 2.3.1 Nov 2001
 */
public class ArrayHeap extends Heap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9118847572552371533L;
	/** Heap container */
	private ArrayList<User> elements;

	/** Constructs an empty heap with the specified comparator */
	public ArrayHeap(Comparator cmp) {
		super(cmp);
		elements = new ArrayList();
	}

	/**
	 * Bottom-Up construction. Constructs a heap and fills it with the objects
	 * from <code>Users</code> using an algorithm for bottom-up
	 * construction.
	 */
	public ArrayHeap(Comparator comparator, User[] neighbors) {
		super(comparator);
		size = neighbors.length;
		height = computeHeight(size);
		elements = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			elements.add(neighbors[i]);
		}
		bottomUp(0);
	}

	/**
	 * Constructs the Neighbors of the subarray starting from loInx, i.e.
	 * Neighbors[sInx..size - 1] into the heap container.
	 */
	public void bottomUp(int sInx) {
		if (sInx + 1 >= elements.size())
			return;
		bottomUp(getLeft(sInx));
		bottomUp(getRight(sInx));
		downBubbling(sInx);
	}

	/**
	 * Adds the given Neighbor in the heap and updates the <code>size</code> and
	 * the <code>height</code> values.
	 */
	public void add(User entry) {
		elements.add(entry);
		size++;
		if ((size == (2 << (height - 1))) || (size == 1)) {
			height++;
		}
		upBubbling();
	}

	/**
	 * Extracts the root of the Heap and updates the <code>size</code> and the
	 * <code>height</code> values.
	 */
	public User extractItem() {
		swap(0, size - 1);
		User result = (User) elements.remove(size - 1);
		size--;
		if ((size + 1 == (2 << (height - 2))) || (size <= 1)) {
			height--;
		}
		downBubbling();
		return result;
	}

	/**
	 * Reads the key, stored in the root of the heap, without extracting the
	 * root.
	 */
	public Object readMinKey() {
		return ((User) elements.get(0)).getKey();
	}

	/**
	 * Reads the element value of the item, stored in the root of the heap
	 * without removing the root.
	 */
	public Object readMinElement() {
		return ((User) elements.get(0)).getValue();
	}

	/**
	 * Restores the order-property of the heap, in the case it's violated by the
	 * last node.
	 */
	protected void upBubbling() {
		upBubbling(size - 1);
	}

	/**
	 * Restores the order-property of the heap, in the case it's violated by the
	 * root.
	 */
	protected void downBubbling() {
		downBubbling(0);
	}

	/**
	 * Locally restores the order-property at the node with the given (absolute)
	 * index.
	 */
	protected void upBubbling(int index) {
		if (index == 0)
			return;
		int parentIndex = getParent(index);

		if (cmp.compare(((User) elements.get(parentIndex)).getKey(),
				((User) elements.get(index)).getKey()) > 0) {
			swap(parentIndex, index);
			upBubbling(parentIndex);
		}
	}

	/**
	 * Locally restores the order-property at the node with the given (absolute)
	 * index and the given last node.
	 */
	protected void downBubbling(int index) {
		if ((index >= size) || (getLeft(index) >= size))
			return;
		User node = (User) elements.get(index);
		User min = (User) elements.get(getLeft(index));
		int minIndex = getLeft(index);
		if (getRight(index) < size) {
			User right = (User) elements.get(getRight(index));
			if (cmp.compare(min.getKey(), right.getKey()) > 0) {
				min = right;
				minIndex = getRight(index);
			}
		}
		if (cmp.compare(node.getKey(), min.getKey()) > 0) {
			swap(index, minIndex);
			downBubbling(minIndex);
		}
	}

	/** Swaps the elements with index i and j. */
	protected void swap(int i, int j) {
		User tmp = elements.get(i);
		elements.set(i, elements.get(j));
		elements.set(j, tmp);
	}

	/**
	 * Calculates the position of the left child of the element with the given
	 * index.
	 */
	private int getLeft(int index) {
		return 2 * index + 1;
	}

	/**
	 * Calculates the position of the right child of the element with the given
	 * index.
	 */
	private int getRight(int index) {
		return 2 * index + 2;
	}

	/**
	 * Calculates the position of the parent of the element with the given
	 * index.
	 */
	private int getParent(int index) {
		return (index - 1) / 2;
	}

	public ArrayList<User> getElements() {
		return elements;
	}

	public Object[] toArray(Object[] a) {
		return elements.toArray(a);
	}
}