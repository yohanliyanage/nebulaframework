package org.nebulaframework.discovery.ws;

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