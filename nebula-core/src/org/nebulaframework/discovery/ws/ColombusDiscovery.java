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
 * {@code ColombusDiscovery} allows Nebula Grid Members to 
 * detect Clusters using a Web Service based discovery
 * mechanism, named as <b>Nebula Colombus Service</b>.
 * <p>
 * This interface is the service which allows to discover
 * nodes, and it is implemented as a web service endpoint
 * in Colombus Server.
 * <p>
 * <i>Web Service</i>
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
@WebService
public interface ColombusDiscovery {
	
	/**
	 * Attempts to discover a Cluster using a given
	 * Colombus Server.
	 * 
	 * @return IP Address of Cluster
	 */
	public String discover();
	
}