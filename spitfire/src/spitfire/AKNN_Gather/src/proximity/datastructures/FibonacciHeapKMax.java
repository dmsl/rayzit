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
