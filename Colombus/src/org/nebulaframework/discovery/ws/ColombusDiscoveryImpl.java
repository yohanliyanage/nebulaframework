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
 * nodes, and it is implemented as a web service endpoint
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