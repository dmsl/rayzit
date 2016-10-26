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

import proximity.datastructures.User;

/**
 * Comparator Implementation for keys of type Double.
 * 
 * @author V.Boutchkova
 */
public class DistanceComparator implements Comparator, Serializable {


	public int compare(Object o1, Object o2) {
		int result = 0;
		if (isLessThan(o1, o2))
			result = -1;
		else if (isLessThan(o2, o1))
			result = 1;
		return result;
	}

	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	private boolean isLessThan(Object a, Object b) {
		if (isComparable(a) && isComparable(b)) {
			double aInt = ((Double) a).doubleValue();
			double bInt = ((Double) b).doubleValue();
			return (aInt < bInt);
		} else {
			throw new IllegalArgumentException("Uncomparable parameter!");
		}
	}

	public boolean isComparable(Object a) {
		if (a == null)
			return false;
		try {
			Double i = (Double) a;
		} catch (ClassCastException ex) {
			return false;
		}
		return true;
	}
}