package org.nebulaframework.discovery.ws.datastore;

import java.util.ArrayList;
import java.util.List;

public class ColombusServices {
	
	private static List<String> servers = new ArrayList<String>();
	
	public static synchronized void addColombusServer(String ip) {
		if (!servers.contains(ip)) {
			servers.add(ip);
		}
	}

	public static synchronized void removeColombusServer(String ip) {
		servers.remove(ip);
	}
	
	public static synchronized List<String> getColombusServers() {
		return servers;
	}
	
}
