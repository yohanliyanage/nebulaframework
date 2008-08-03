package org.nebulaframework.discovery.ws.datastore;

import java.util.ArrayList;
import java.util.List;
/**
 * Data Store of peer Colombus Servers. Keeps track of peer servers 
 * which are reigstered with this Colombus Server.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ColombusServices {
	
	private static List<String> servers = new ArrayList<String>();
	
	/**
	 * Adds a peer server to this Colombus Server.
	 *  
	 * @param ip IP Address
	 */
	public static synchronized void addColombusServer(String ip) {
		if (!servers.contains(ip)) {
			servers.add(ip);
		}
	}

	/**
	 * Removes a peer serve from this Colombus Server.
	 * 
	 * @param ip IP Address
	 */
	public static synchronized void removeColombusServer(String ip) {
		servers.remove(ip);
	}
	
	/**
	 * Returns the List of registered peer servers.
	 * 
	 * @return List of Servers
	 */
	public static synchronized List<String> getColombusServers() {
		return servers;
	}
	
}
