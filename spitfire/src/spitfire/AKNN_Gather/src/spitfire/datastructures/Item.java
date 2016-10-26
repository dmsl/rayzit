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