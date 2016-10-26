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
 *  Compilation:  javac Interval2D.java
 *  Execution:    java Interval2D
 *
 *  Implementation of 2D interval.
 *
 *************************************************************************/

public class Interval2D<Key extends Comparable<Key>> { 
    public final Interval<Key> intervalX;   // x-interval
    public final Interval<Key> intervalY;   // y-interval
   
    public Interval2D(Interval<Key> intervalX, Interval<Key> intervalY) {
        this.intervalX = intervalX;
        this.intervalY = intervalY;
    }

    // does this 2D interval a intersect b?
    public boolean intersects(Interval2D<Key> b) {
        if (intervalX.intersects(b.intervalX)) return true;
        if (intervalY.intersects(b.intervalY)) return true;
        return false;
    }

    // does this 2D interval contain (x, y)?
    public boolean contains(Key x, Key y) {
        return intervalX.contains(x) && intervalY.contains(y);
    }

    // return string representation
    public String toString() {
        return intervalX + " x " + intervalY;
    }



    // test client
    public static void main(String[] args) {
        Interval<Double> intervalX = new Interval<Double>(0.0, 1.0);
        Interval<Double> intervalY = new Interval<Double>(5.0, 6.0);
        Interval2D<Double> box1 = new Interval2D<Double>(intervalX, intervalY);
        intervalX = new Interval<Double>(-5.0, 5.0);
        intervalY = new Interval<Double>(3.0, 7.0);
        Interval2D<Double> box2 = new Interval2D<Double>(intervalX, intervalY);
        System.out.println("box1 = " + box1);
        System.out.println("box2 = " + box2);
        System.out.println(box1.contains(0.5, 5.5));
        System.out.println(!box1.contains(1.5, 5.5));
        System.out.println(box1.intersects(box2));
        System.out.println(box2.intersects(box1));
    }
}