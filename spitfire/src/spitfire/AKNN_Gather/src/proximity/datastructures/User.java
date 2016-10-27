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
package proximity.datastructures;

import java.io.Serializable;

public class User implements DoubleValueComparable<User>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7950791214580273673L;

	public double lon;
	public double lat;
	public double minD;
	public double maxD;
	public String key;

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		User u = (User) obj;
		return key.equals(u.key);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return key.hashCode();
	}

	public User(String key, double lon, double lat) {
		this.lat = lat;
		this.lon = lon;
		this.minD = 0;
		this.key = key;
	}

	public User(String key, double lon1, double lat1, double lon2, double lat2,
			char unit) {
		// TODO Auto-generated constructor stub
		this.lat = lat2;
		this.lon = lon2;
		this.key = key;
		this.minD = getDistance(lat1, lon1, lat2, lon2, unit);
	}

	public void setDistance(double lon1, double lat1) {
		this.minD = getDistance(lat1, lon1, lat, lon, 'M');
	}

	public void setDistance(User u) {
		minD = getDistance(u);
	}

	public User(String key, double lon1, double lat1, double minD, double maxD) {
		// TODO Auto-generated constructor stub
		this.lat = lat1;
		this.lon = lon1;
		this.key = key;
		this.minD = minD;
		this.maxD = maxD;
	}

	public User(User user) {
		this.lon=user.lon;
		this.lat=user.lat;
		this.minD=user.minD;
		this.maxD=user.maxD;
		this.key=user.key;
	}

	public double getDistance(User u) {
		return getDistance(u.lat, u.lon, lat, lon, 'M');
	}

	private double getDistance(double lat1, double lon1, double lat2,
			double lon2, char unit) {
		double dist = Math.sqrt((Math.pow(lon2 - lon1, 2) + Math.pow(lat2
				- lat1, 2)));
		return dist;

	}

	@SuppressWarnings("unused")
	private double getDistanceM(double lat1, double lon1, double lat2,
			double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		if (Double.isNaN(dist))
			System.out.println(lat1 + "," + lon1 + "," + lat2 + "," + lon2
					+ "," + 'K');

		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	public int compareTo(User nei) {
		// TODO Auto-generated method stub
		return this.minD == nei.minD ? 0 : minD < nei.minD ? -1 : 1;
	}

	public static void main(String[] args) {
	}

	public double getKey() {
		// TODO Auto-generated method stub
		return minD;
	}

	public double getSecondaryKey() {
		// TODO Auto-generated method stub
		return maxD;
	}

	public User clone() {
		// TODO Auto-generated method stub
		return new User(this.key, this.lon, this.lat);
	}

	public String getId() {
		// TODO Auto-generated method stub
		return this.key;
	}

	public Object getValue() {
		// TODO Auto-generated method stub
		return this;
	}

}