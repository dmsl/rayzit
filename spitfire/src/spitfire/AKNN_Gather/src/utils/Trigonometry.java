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
package utils;

public class Trigonometry {

	public static double slopeToDegree(double dSlope) {
		double dRadian = Math.atan(dSlope);
		double dDegree = Math.toDegrees(dRadian);
		return dDegree;
	}

	/* CORRECTED aytin den tin xreiazomaste tha eprepe PANTA na kaloyme tin pointToRectangelBoundaryMinDistance
	public static double pointToRectangleMinDistance(double p_x, double p_y,
			double r_x1, double r_y1, double r_x2, double r_y2) {
		double dist = 0.0;

		if (p_x < r_x1 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
		else if (p_x < r_x1 && p_y > r_y1 && p_y < r_y2)
			return r_x1 - p_x;
		else if (p_x < r_x1 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y2); // CORRECTED p_y -> r_y2
		else if (p_x > r_x1 && p_x < r_x2 && p_y > r_y2)
			return p_y - r_y2;
		else if (p_x > r_x2 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		else if (p_x > r_x2 && p_y > r_y1 && p_y < r_y2)
			return p_x - r_x2;
		else if (p_x > r_x2 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y1); 
		else if (p_x > r_x1 && p_x < r_x2 && p_y < r_y1) // CORRECTED r_x2 -> r_x1 and r_x1 -> r_x2
			return r_y1 - p_y;

		return dist;

	}
	*/

	/* CORRECTED aytin den tin xreiazomaste tha eprepe PANTA na kaloyme tin pointToRectangelBoundaryMaxDistance

	public static double pointToRectangleMaxDistance(double p_x, double p_y,
			double r_x1, double r_y1, double r_x2, double r_y2) {
		double dist = 0.0;

		if (p_x < r_x1 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		else if (p_x < r_x1 && p_y > r_y1 && p_y < r_y2) {
			if (p_y > (r_y2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x2, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y2);

		} else if (p_x < r_x1 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		else if (p_x > r_x1 && p_x < r_x2 && p_y > r_y2) {
			if (p_x > (r_x2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y1);
		} else if (p_x > r_x2 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y1);

		else if (p_x > r_x2 && p_y > r_y1 && p_y < r_y2) {

			if (p_y > (r_y2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x1, r_y2);

		} else if (p_x > r_x2 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y2);
		else if (p_x > r_x1 && p_x < r_x2 && p_y < r_y1) { // CORRECTED r_x2 -> r_x1 and r_x1 -> r_x2
			if (p_x > (r_x2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y2);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y2);

		}
		return dist;

	}
	 */

	public static double pointToRectangleBoundaryMaxDistance(double p_x,
			double p_y, double r_x1, double r_y1, double r_x2, double r_y2) {
		
		/* CORRECTED den xreiazontai
		 double dist = 0.0;

		if (p_x < r_x1 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		else if (p_x < r_x1 && p_y > r_y1 && p_y < r_y2) {
			if (p_y > (r_y2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x2, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y2);

		} else if (p_x < r_x1 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		else if (p_x > r_x1 && p_x < r_x2 && p_y > r_y2) {
			if (p_x > (r_x2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y1);
		} else if (p_x > r_x2 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y1);

		else if (p_x > r_x2 && p_y > r_y1 && p_y < r_y2) {

			if (p_y > (r_y2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			else
				return getEuclideanDistance(p_x, p_y, r_x1, r_y2);

		} else if (p_x > r_x2 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y2);
		else if (p_x > r_x1 && p_x < r_x2 && p_y < r_y1) { // CORRECTED r_x2 -> r_x1 and r_x1 -> r_x2
			if (p_x > (r_x2 / 2))
				return getEuclideanDistance(p_x, p_y, r_x1, r_y2);
			else
				return getEuclideanDistance(p_x, p_y, r_x2, r_y2);

		}

		// Inside Rectangle
		else {
		 */
			double d1 = getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			double d2 = getEuclideanDistance(p_x, p_y, r_x1, r_y2);
			double d3 = getEuclideanDistance(p_x, p_y, r_x2, r_y1);
			double d4 = getEuclideanDistance(p_x, p_y, r_x2, r_y2);

			return Math.max(Math.max(d1, d2), Math.max(d3, d4));
		// CORRECTED }

		// return dist;

	}

	public static double pointToRectangleBoundaryMinDistance(double p_x,
			double p_y, double r_x1, double r_y1, double r_x2, double r_y2) {
		// CORRECTED double dist = 0.0;

		//Left down
		if (p_x < r_x1 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y1);
		//Left Horizontal Border
		else if (p_x < r_x1 && p_y >= r_y1 && p_y <= r_y2)
			return r_x1 - p_x;
		//Left Up
		else if (p_x < r_x1 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x1, r_y2); // corrected p_y -> r_y2
		//Up Vertical Border
		else if (p_x >= r_x1 && p_x <= r_x2 && p_y > r_y2)
			return p_y - r_y2;
		//Right Up
		else if (p_x > r_x2 && p_y > r_y2)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y2);
		//Right Horizontal Border
		else if (p_x > r_x2 && p_y >= r_y1 && p_y <= r_y2)
			return p_x - r_x2;
		//Right down
		else if (p_x > r_x2 && p_y < r_y1)
			return getEuclideanDistance(p_x, p_y, r_x2, r_y1);
		//Down Vertical Border
		else if (p_x >= r_x1 && p_x <= r_x2 && p_y < r_y1) // corrected r_x2 -> r_x1 and r_x1 -> r_x2
			return r_y1 - p_y;
		// Inside Rectangle
		else {
			double d1 = Math.abs(p_x - r_x1); // CORRECTED getEuclideanDistance(p_x, p_y, r_x1, r_y1);
			double d2 = Math.abs(r_x2 - p_x); // CORRECTED getEuclideanDistance(p_x, p_y, r_x1, r_y2);
			double d3 = Math.abs(p_y - r_y1); // CORRECTED getEuclideanDistance(p_x, p_y, r_x2, r_y1);
			double d4 = Math.abs(r_y2 - p_y); // CORRECTED getEuclideanDistance(p_x, p_y, r_x2, r_y2);

			return Math.min(Math.min(d1, d2), Math.min(d3, d4));
		}
		// return dist;

	}

	public static double getEuclideanDistance(double x1, double y1, double x2,
			double y2) {
		return Math.sqrt((Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2)));

	}

	// 1
	// -----------------
	// | | |<----- distance ------>| x
	// 0| |2
	// | |
	// -----------------
	// 3
	//
	// This function return the right side
	//
	//

	public static Point intersection(double x1, double y1, double x2,
			double y2, double x3, double y3, double x4, double y4) {
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0)
			return null;

		double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2)
				* (x3 * y4 - y3 * x4))
				/ d;
		double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2)
				* (x3 * y4 - y3 * x4))
				/ d;

		Point p = new Point(xi, yi);
		if ((xi < Math.min(x1, x2) || xi > Math.max(x1, x2)))
			return null;
		if ((xi < Math.min(x3, x4) || xi > Math.max(x3, x4)))
			return null;
		return p;
	}

	public static int getSide(double p_x, double p_y, double x1, double y1,
			double x2, double y2) {
		double c_x = (x2 + x1) / 2.0;
		double c_y = (y2 + y1) / 2.0;

		if ((p_x - c_x) == 0)
			return 0;

		// The slope from the point to the center of the rectangle
		double slope = (p_y - c_y) / (p_x - c_x);

		// Calculate the slope for each diagonal
		double slope1 = (y2 - y1) / (x2 - x1);
		// double slope2 = (y2 - y1) / (x2 - x1);// -slope1

		double theta = Math.abs(slopeToDegree(slope1));

		double p_degree = Math.abs(slopeToDegree(slope));

		// System.out.println("Θ=" + theta + ",P_Θ=" + p_degree); // The
		// expected
		// output is: 45
		if (p_x <= c_x && p_y <= c_y) {
			if (p_degree <= theta)
				return 0;
			else
				return 3;
		} else if (p_x <= c_x && p_y >= c_y) {
			if (p_degree <= theta)
				return 0;
			else
				return 1;
		} else if (p_x >= c_x && p_y >= c_y) {
			if (p_degree >= theta)
				return 1;
			else
				return 2;
		} else if (p_x >= c_x && p_y <= c_y) {
			if (p_degree <= theta)
				return 2;
			else
				return 3;
		}
		return 0;
	}

	public static void main(String[] args) {
		/**
		 * All trigonometric functions in java.lang.Math return values in
		 * radians. Therefore, you can use the Math.toDegrees() to convert to
		 * degree. The inverse of sin, cosine and tangent are arcsine, arccosine
		 * and arctangent.
		 */

		double p_x = 12.001;// 1.5;
		double p_y = 1.001;// 3;
		double x1 = 100;
		double y1 = 100;
		double x2 = 200;
		double y2 = 200;

		// System.out.println(slopeToDegree(dSlope)); // The expected output is:
		// 45
		// degrees.
		System.out.println(getSide(p_x, p_y, x1, y1, x2, y2));

//		System.out
//				.println(pointToRectangleMinDistance(p_x, p_y, x1, y1, x2, y2));
	}

}