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
package org.nebulaframework.discovery.ws;

import javax.jws.WebService;

/**
 * {@code ColombusManager} is the management interface of
 * Colombus Servers. This interface allows to add / remove
 * clusters from a Colombus Server, and also to register
 * peer Colombus Servers.
 * <p>
 * <i>Web Service</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@WebService
public interface ColombusManager {
	
	/**
	 * Registers a Cluster with given IP Address
	 * in this Colombus Server.
	 * 
	 * @param ip Cluster IP
	 */
	public void registerCluster(String ip);
	
	/**
	 * Unregisters a Cluster with given IP Address
	 * from this Colombus Server.
	 * 
	 * @param ip Cluster IP
	 */
	public void unregisterCluster(String ip);
	
	/**
	 * Registers a peer Colombus Server with this
	 * Colombus Server.
	 * 
	 * @param ip IP Address of Peer Colombus Server
	 */
	public void registerColombusService(String ip);
	
	/**
	 * Unregisters a peer Colombus Server from this
	 * Colombus Server.
	 * 
	 * @param ip IP Address of Peer Colombus Server
	 */
	public void unregisterColombusService(String ip);
	
}