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