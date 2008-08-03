/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.discovery.ws.datastore;

import java.util.ArrayList;
import java.util.List;

/**
 * DataStore for Cluster information. Keeps track of registered
 * Clusters in this Colombus Server.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterData {
	
	private static List<String> clusters = new ArrayList<String>();
	private static int requestCount=0;

	/**
	 * Adds a new Cluster to this Colombus Server.
	 * @param ip IP Address of Cluster
	 */
	public static synchronized void addCluster(String ip) {
		if (! clusters.contains(ip)) {
			clusters.add(ip);
		}
	}

	/**
	 * Removes the given Cluster from this Colombus Server.
	 * 
	 * @param ip IP Address
	 */
	public static synchronized void removeCluster(String ip) {
		clusters.remove(ip);
	}
	
	/**
	 * Returns the next available Cluster from this 
	 * Colombus Server. The next cluster is determined
	 * using round-robin method.
	 * 
	 * @return Next available Cluster IP
	 */
	public static String nextCluster() {
		if (clusters.size()==0) return null;
		requestCount++;
		return clusters.get(requestCount % clusters.size());
	}
	
}
