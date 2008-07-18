package org.nebulaframework.discovery.ws;

import javax.jws.WebService;

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

	/**
	 * {@inheritDoc}
	 */
	public void registerCluster(String ip) {
		ClusterData.addCluster(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerColombusService(String ip) {
		ColombusServices.addColombusServer(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterCluster(String ip) {
		ClusterData.removeCluster(ip);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterColombusService(String ip) {
		ColombusServices.removeColombusServer(ip);
	}
}