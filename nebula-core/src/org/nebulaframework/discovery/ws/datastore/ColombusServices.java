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
 * Data Store of peer Colombus Servers. Keeps track of peer servers 
 * which are registered with this Colombus Server.
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
