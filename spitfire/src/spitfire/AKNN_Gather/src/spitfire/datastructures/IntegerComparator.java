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

import java.util.Comparator;

/**
 * Comparator Implementation for keys of type Integer.
 * @author V.Boutchkova
 */
public class IntegerComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    int result = 0;
    if (isLessThan(o1, o2)) result = -1;
    else if (isLessThan(o2, o1)) result = 1;
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
    if (a == null) return false;
    try {
      Integer i = (Integer) a;
    } catch (ClassCastException ex) {
      return false;
    }
    return true;
  }
}