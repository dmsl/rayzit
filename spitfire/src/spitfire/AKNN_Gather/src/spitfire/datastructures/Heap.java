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

import utils.*;

import java.io.Serializable;
import java.util.*;

import proximity.datastructures.User;

/**
 * Abstract <code>Heap</code> class.
 * 
 * A heap is a binary tree that stores a collection of keys at its internal
 * nodes and that satisfies two additional properties:<br>
 * 1. Heap-Order Property: for every node <code>v</code> other than the root the
 * key stored at <code>v</code> is greater than or equal to the key stored at
 * <code>v</code>'s parent.<br>
 * 2. Complete Binary Tree: the levels 0, 1, ... (height - 1) have the maximum
 * number of nodes possible and in level (height - 1) all the nodes are to the
 * left.<br>
 * 
 * @author V.Boutchkova
 */
public abstract class Heap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7048946972872917464L;
	/** The number of elements in this heap */
	protected int size;
	/** The height of the heap; a heap with one element has height = 1. */
	protected int height;
	/** Comparator, defining a total relation on the keys */
	protected Comparator cmp;

	/** Constructs an empty heap with the given comparator. */
	public Heap(Comparator cmp) {
		this.cmp = cmp;
		int size = 0;
		int height = 0;
	}

	/**
	 * Adds a node with the given <code>key</code> and <code>value</code> in the
	 * heap and updates the <code>size</code> and the <code>height</code>
	 * values.
	 */
	public void add(Object key, Object value) {
		add(new User((String) key, 0.0, 0.0, 0.0, (Double)value));
	}

	/**
	 * Adds the given item in the heap and updates the <code>size</code> and the
	 * <code>height</code> values.
	 */
	abstract public void add(User entry);

	/**
	 * Extracts the root of the Heap and updates the <code>size</code> and the
	 * <code>height</code> values.
	 */
	abstract public User extractItem();

	/**
	 * Reads the key, stored in the root of the heap, without extracting the
	 * root.
	 */
	abstract public Object readMinKey();

	/**
	 * Reads the element value of the item, stored in the root of the heap
	 * without removing the root.
	 */
	abstract public Object readMinElement();

	/** Reads the number of items stored in the heap. */
	public int size() {
		return size;
	}

	/** Reads the height of the heap. */
	public int getHeight() {
		return height;
	}

	/** Checks whether the heap is empty. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Computes the height of a heap with a given size. */
	public static int computeHeight(int size) {
		int height = 0;
		if (size == 1)
			height++;
		if (size > 1)
			height = computeHeight(size / 2) + 1;
		return height;
	}

}