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
package ready;
/*************************************************************************
 *  Compilation:  javac Interval.java
 *  Execution:    java Interval
 *
 *  Implementation of an interval.
 *
 *************************************************************************/

public class Interval<Key extends Comparable<Key>> { 
    public final Key low;      // left endpoint
    public final Key high;     // right endpoint
   
    public Interval(Key low, Key high) {
        if (less(high, low)) throw new RuntimeException("Illegal argument");
        this.low  = low;
        this.high = high;
    }

    // is x between low and high
    public boolean contains(Key x) {
        return !less(x, low) && !less(high, x);
    }

    // does this interval a intersect interval b?
    public boolean intersects(Interval<Key> b) {
        Interval<Key> a  = this;
        if (less(a.high, b.low)) return false;
        if (less(b.high, a.low)) return false;
        return true;
    }

    // does this interval a equal interval b?
    public boolean equals(Interval<Key> b) {
        Interval<Key> a  = this;
        return a.low.equals(b.low) && a.high.equals(b.high);
    }


    // comparison helper functions
    private boolean less(Key x, Key y) {
        return x.compareTo(y) < 0;
    }

    // return string representation
    public String toString() {
        return "[" + low + ", " + high + "]";
    }



    // test client
    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);

        Interval<Integer> a = new Interval<Integer>(5, 17);
        Interval<Integer> b = new Interval<Integer>(5, 17);
        Interval<Integer> c = new Interval<Integer>(5, 18);
        System.out.println(a.equals(b));
        System.out.println(!a.equals(c));
        System.out.println(!b.equals(c));


        // generate N random points in [-1, 2] and compute
        // fraction that lies in [0, 1]
        Interval<Double> interval = new Interval<Double>(0.0, 1.0);
        int cnt = 0;
        for (int i = 0; i < N; i++) {
            Double x = 3 * Math.random() - 1.0;
            if (interval.contains(x)) cnt++;
        }
        System.out.println("fraction = " + (1.0 * cnt / N));
    }
}