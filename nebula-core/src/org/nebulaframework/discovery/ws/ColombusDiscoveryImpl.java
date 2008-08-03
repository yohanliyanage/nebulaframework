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

/**
 * Implementation of {@code ColombusDiscovery}, which allows 
 * Nebula Grid Members to detect Clusters using a Web Service 
 * based discovery mechanism, named as <b>Nebula Colombus 
 * Service</b>.
 * <p>
 * This interface is the service which allows to discover
 * nodes, and it is implemented as a web service end-point
 * in Colombus Server.
 * <p>
 * <i>Web Service</i>
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
@WebService(endpointInterface="org.nebulaframework.discovery.ws.ColombusDiscovery", serviceName="ColombusDiscovery")
public class ColombusDiscoveryImpl implements ColombusDiscovery {
	
	private static Log log = LogFactory.getLog(ColombusDiscoveryImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public String discover() {
		log.debug("[ColombusDiscovery] Discovery Request Received");
		return ClusterData.nextCluster();
	}

}