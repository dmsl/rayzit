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
/**
 * 
 */
package proximity.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * @author Georgios Chatzimilioudis
 * 
 * a maximum fibonacci k-heap.
 * there can only k elements in the heap at most
 * it is a maximum heap. this is achieved by negating the key
 * of the element at insert point.
 *
 */
public class FibonacciHeapKMax<T> extends FibonacciHeap<T>  implements Serializable  {

	/* *************************************************************************
	 * Private member variables
	 * **************************************************** */

	/**
	 * 
	 */
	private static final long serialVersionUID = -1153288542658288504L;
	/* what is the maximum capacity */
	Integer capacity;
	
	/* ******* END of private member variables ******************************** */

	/* *************************************************************************
	 * Constructors / Destructors
	 * **************************************************** */

	public FibonacciHeapKMax( int capacity ) {
		super();
		this.capacity = capacity;
	}

	/* ******* END of constructors/destructors ******************************** */

	/* *************************************************************************
	 * Getters / Setters
	 * **************************************************** */

	/**
	 * @return capacity
	 */
	public Integer getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setK(Integer capacity) {
		this.capacity = capacity;
	}
	
	/* ******* END of getters / setters ******************************** */

	/* *************************************************************************
	 * Functions
	 * **************************************************** */

	/**
	 * insert a new node making sure there are at most capacity elements in the heap.
	 * if over capacity remove head and return it.
	 * NEGATE the key at entry point so that the minheap FibonacciHeap becomes max heap.
	 * 
	 * also simplify the interface to enter as element class T
	 * and return element as class T without the key.
	 */
    public T insertMaxheap(T newElement, double key) {

    	/* to avoid the insert and remove if kheap.size = capacity and new > head then discard */
//   		if ( this.size() == this.capacity && key >= this.max().getKey() ) {
//   			return null;
//   		}
    	
		FibonacciHeapNode<T> node = new FibonacciHeapNode<T>(newElement, 0);
		super.insert( node, - key );
		
		/* check capacity */
		if ( this.size() > this.capacity ) {
			FibonacciHeapNode<T> oldHeadNode = this.removeMin();
			return oldHeadNode.getData();
		}
		
		return null;
		
    }

    /**
     * rename min() into max() for code readability
     */
    public FibonacciHeapNode<T> max() {
    	if ( this.min() == null ) {
    		return null;
    	}
    	FibonacciHeapNode<T> max = this.min();
    	//max.key = - max.getKey();
    	return max;
//    	return this.min();
    }
    
    /**
     * return heap as arraylist (not ordered)
     */
    public ArrayList<T> toArrayList() {
    	
    	if (this.min() == null) {
            return new ArrayList<T>();
        }

    	ArrayList<T> result = new ArrayList<T>( this.size() );
    	
        // create a new stack and put root on it
        Stack<FibonacciHeapNode<T>> stack = new Stack<FibonacciHeapNode<T>>();
        stack.push( this.min() );

        // do a simple breadth-first traversal on the tree
        while (!stack.empty()) {
            FibonacciHeapNode<T> curr = stack.pop();
            result.add(curr.getData());

            if (curr.child != null) {
                stack.push(curr.child);
            }

            FibonacciHeapNode<T> start = curr;
            curr = curr.right;

            while (curr != start) {
                result.add(curr.getData());

                if (curr.child != null) {
                    stack.push(curr.child);
                }

                curr = curr.right;
            }
        }
        
        return result;
        
    }

	public HashSet<T> toHashSet() {

    	if (this.min() == null) {
            return null;
        }

    	HashSet<T> result = new HashSet<T>( this.size() );
    	
        // create a new stack and put root on it
        Stack<FibonacciHeapNode<T>> stack = new Stack<FibonacciHeapNode<T>>();
        stack.push( this.min() );

        // do a simple breadth-first traversal on the tree
        while (!stack.empty()) {
            FibonacciHeapNode<T> curr = stack.pop();
            result.add(curr.getData());

            if (curr.child != null) {
                stack.push(curr.child);
            }

            FibonacciHeapNode<T> start = curr;
            curr = curr.right;

            while (curr != start) {
                result.add(curr.getData());

                if (curr.child != null) {
                    stack.push(curr.child);
                }

                curr = curr.right;
            }
        }
        
        return result;
        
	}

    /* ******* END of functions ******************************** */
}
