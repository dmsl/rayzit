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
package couchbase.connector;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import proximity.datastructures.User;
import benchmark.Benchmarking;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.SpatialView;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.gson.Gson;

public class CouchBaseData {

	public static void setLoggingOff() {

		// Tell things using spymemcached logging to use internal SunLogger API
		Properties systemProperties = System.getProperties();
		systemProperties.put("net.spy.log.LoggerImpl",
				"net.spy.memcached.compat.log.SunLogger");
		System.setProperties(systemProperties);

		Logger.getLogger("net.spy.memcached").setLevel(Level.OFF);
		Logger.getLogger("com.couchbase.client").setLevel(Level.OFF);
		Logger.getLogger("com.couchbase.client.vbucket").setLevel(Level.OFF);

		// get the top Logger
		Logger topLogger = java.util.logging.Logger.getLogger("");

		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		// see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				// found the console handler
				consoleHandler = handler;
				break;
			}
		}

		if (consoleHandler == null) {
			// there was no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}

		// set the console handler to fine:
		consoleHandler.setLevel(java.util.logging.Level.OFF);

	}

	public static List<User> getData() {

		// Set the URIs and get a client
		List<URI> uris = new LinkedList<URI>();
		List<User> neighborsList = new LinkedList<User>();

		// Connect to localhost or to the appropriate URI(s)
		uris.add(URI.create("http://10.16.30.78:8091/pools"));
		uris.add(URI.create("http://10.16.30.102:8091/pools"));
		uris.add(URI.create("http://10.16.30.101:8091/pools"));
		uris.add(URI.create("http://10.16.30.100:8091/pools"));
		uris.add(URI.create("http://10.16.30.209:8091/pools"));
		uris.add(URI.create("http://10.16.30.105:8091/pools"));
		uris.add(URI.create("http://10.16.30.83:8091/pools"));
		uris.add(URI.create("http://10.16.30.80:8091/pools"));
		uris.add(URI.create("http://10.16.30.81:8091/pools"));
		uris.add(URI.create("http://10.16.30.106:8091/pools"));

		CouchbaseClient client = null;
		try {
			CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
			cfb.setOpQueueMaxBlockTime(100000);
			cfb.setOpTimeout(500000);
			cfb.setShouldOptimize(true);
			cfb.setMaxReconnectDelay(200000);
			cfb.setViewTimeout(500000);
			// cfb.setTimeoutExceptionThreshold(500000); // wait up to 500
			// seconds when trying to enqueue an operation
			// cfb.setOpTimeout(500000);

			// CouchbaseConnectionFactory cf = new
			// CouchbaseConnectionFactory(uris, "crowdhood", "crowdhood");

			// Use the "default" bucket with no password
			client = new CouchbaseClient(cfb.buildCouchbaseConnection(uris,
					"crowdhood", "crowdhood"));

			SpatialView view = client.getSpatialView("nearest", "bbknn");

			Query myQuery = new Query();
			// Set the limit for the records

			double lowerLeftLong = Benchmarking.c_mylb - 180;
			double lowerLeftLat = Benchmarking.r_mylb - 90;
			double upperRightLong = Benchmarking.c_myub - 180;
			double upperRightLat = Benchmarking.r_myub - 90;

			// System.out.println("Ranges : " + Benchmarking.r_mylb + ","
			// + Benchmarking.c_mylb + "," + Benchmarking.r_myub + ","
			// + Benchmarking.c_myub);

			// System.out.println("BBOX: " + lowerLeftLong + "," + lowerLeftLat
			// + ":" + upperRightLong + "," + upperRightLat);

			myQuery = myQuery.setBbox(lowerLeftLong, lowerLeftLat,
					upperRightLong, upperRightLat);
			// myQuery.setLimit(10000);

			// myQuery.setRange("M", "N");
			ViewResponse queryResults = client.query(view, myQuery);
			Iterator<ViewRow> walkthrough = queryResults.iterator();
			String[] ss = null;

			while (walkthrough.hasNext()) {
				ViewRow next = walkthrough.next();
				String s = next.getValue();
				s = s.substring(1, s.length() - 1);
				ss = s.split(",");
				// Transform 'lon' and 'lat' to positive
				double lat = Double.parseDouble(ss[1]) + 90;
				double lon = Double.parseDouble(ss[0]) + 180;
				neighborsList.add(new User(next.getId(), lon, lat));
			}

			// System.out
			// .println("CouchBaseData.getData("+neighborsList.size()+")" );

		} catch (IOException e) {
			System.err.println("IOException connecting to Couchbase: "
					+ e.getMessage());
			System.exit(1);
		}

		// Do an asynchronous set

		// Shutdown and wait a maximum of three seconds to finish up operations
		client.shutdown();

		return neighborsList;

	}

	public static void main(String args[]) {

		// Global variables

		setLoggingOff();

		List<User> neighborsList = getData();

		Gson gson = new Gson();
		// convert java object to JSON format,
		// and returned as JSON formatted string
		String json = gson.toJson(neighborsList);

		System.out.println(json);

		System.exit(0);
	}
}
