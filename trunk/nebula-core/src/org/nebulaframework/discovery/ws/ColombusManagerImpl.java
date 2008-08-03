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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.discovery.ws.datastore.ClusterData;
import org.nebulaframework.discovery.ws.datastore.ColombusServices;

/**
 * Implementation of {@code ColombusManager} is the management 
 * interface of Colombus Servers. This interface allows to 
 * add / remove clusters from a Colombus Server, and also 
 * to register peer Colombus Servers.
 * <p>
 * <i>Web Service</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@WebService(endpointInterface="org.nebulaframework.discovery.ws.ColombusManager", serviceName="ColombusManager")
public class ColombusManagerImpl implements ColombusManager {

	private static Log log = LogFactory.getLog(ColombusManagerImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public void registerCluster(String ip) {
		log.info("[ColombusManager] Cluster Registered : " + ip);
		ClusterData.addCluster(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerColombusService(String ip) {
		log.info("[ColombusManager] Colombus Peer Registered : " + ip);
		ColombusServices.addColombusServer(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterCluster(String ip) {
		log.info("[ColombusManager] Cluster Unregistered : " + ip);
		ClusterData.removeCluster(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterColombusService(String ip) {
		log.info("[ColombusManager] Colombus Peer Unregistered : " + ip);
		ColombusServices.removeColombusServer(ip);
	}
}