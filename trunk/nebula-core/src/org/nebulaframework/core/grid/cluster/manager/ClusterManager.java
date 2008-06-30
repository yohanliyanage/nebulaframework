package org.nebulaframework.core.grid.cluster.manager;

import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.core.grid.cluster.manager.services.registration.NodeRegistrationService;
import org.nebulaframework.core.support.ID;

public class ClusterManager {

	private UUID clusterId;
	private String brokerUrl;
	private ServiceMessageSender serviceMessageSender;
	private NodeRegistrationService nodeRegistrationService;

	
	public ClusterManager() {
		super();
		this.clusterId = ID.getId();
	}

	public UUID getClusterId() {
		return clusterId;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public ServiceMessageSender getServiceMessageSender() {
		return serviceMessageSender;
	}

	public void setServiceMessageSender(
			ServiceMessageSender serviceMessageSender) {
		this.serviceMessageSender = serviceMessageSender;
	}

	public NodeRegistrationService getNodeRegistrationService() {
		return nodeRegistrationService;
	}

	public void setNodeRegistrationService(
			NodeRegistrationService nodeRegistrationService) {
		this.nodeRegistrationService = nodeRegistrationService;
	}

}
