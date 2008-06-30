package org.nebulaframework.core.grid.cluster.registration;

import java.io.Serializable;
import java.util.UUID;

public interface Registration extends Serializable {

	/**
	 * Returns the assigned UUID for the registered Node
	 * @return UUID id
	 */
	public abstract UUID getNodeId();

	/**
	 * Returns the Cluster Manager UUID 
	 * @return UUID Cluster Manager Id
	 */
	public abstract UUID getClusterId();
	
	/**
	 * Returns the ClusterManager Broker URL
	 * @return String BrokerURL
	 */
	public abstract String getBrokerUrl();


}