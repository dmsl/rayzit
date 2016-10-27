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

import java.io.*;

import proximity.datastructures.User;

/**
 * Composition of key-value data.
 * @author V.Boutchkova
 */
public class Item implements Serializable {

	private Object key;
	private Object value;

	public Item(Object key, Object value) {
	    this.key = key;
	    this.value = value;
	}

	public Object getKey() {
    return key;
	}

	public Object getValue() {
	  return value;
	}

	public Object setValue(Object value) {
	  if (value == null)
		  throw new NullPointerException();
    Object oldValue = this.value;
    this.value = value;
    return oldValue;
	}

	public String toString() {
	  return key.toString()+"="+value.toString();
	}
}