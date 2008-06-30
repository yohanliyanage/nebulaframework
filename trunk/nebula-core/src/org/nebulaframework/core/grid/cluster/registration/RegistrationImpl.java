package org.nebulaframework.core.grid.cluster.registration;

import java.util.UUID;


public class RegistrationImpl implements Registration {

	private static final long serialVersionUID = -3286278119947135119L;
	
	private UUID uuid;
	private UUID clusterId;
	private String brokerUrl;

	/**
	 * {@inheritDoc}
	 */
	public UUID getNodeId() {
		return uuid;
	}

	/**
	 * Sets the UUID for Node
	 * @param uuid UUID
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public UUID getClusterId() {
		return clusterId;
	}

	/**
	 * Sets the ClusterManager UUID
	 * @param clusterId ClusterManager UUID
	 */
	public void setClusterId(UUID clusterId) {
		this.clusterId = clusterId;
	}

	/**
	 * {@inheritDoc}
	 */	
	public String getBrokerUrl() {
		return brokerUrl;
	}

	/**
	 * Sets the Broker URL of ClusterManager
	 * @param brokerUrl Broker URL
	 */
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}


}
