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
