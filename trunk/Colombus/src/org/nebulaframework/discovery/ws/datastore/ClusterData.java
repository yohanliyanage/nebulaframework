package org.nebulaframework.discovery.ws.datastore;

import java.util.ArrayList;
import java.util.List;

public class ClusterData {
	
	private static List<String> clusters = new ArrayList<String>();
	private static int requestCount=0;
	
	public static synchronized void addCluster(String ip) {
		if (! clusters.contains(ip)) {
			clusters.add(ip);
		}
	}

	public static synchronized void removeCluster(String ip) {
		clusters.remove(ip);
	}
		
	public static String nextCluster() {
		if (clusters.size()==0) return null;
		requestCount++;
		return clusters.get(requestCount % clusters.size());
	}
	
}
