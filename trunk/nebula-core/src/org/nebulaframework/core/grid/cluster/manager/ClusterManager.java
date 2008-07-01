package org.nebulaframework.core.grid.cluster.manager;

import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.support.ID;

public class ClusterManager {

	private UUID clusterId;
	private String brokerUrl;
	private ServiceMessageSender serviceMessageSender;
	private ClusterRegistrationService clusterRegistrationService;
	private ClusterJobService jobService;
	
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

	public ClusterRegistrationService getClusterRegistrationService() {
		return clusterRegistrationService;
	}

	public void setClusterRegistrationService(
			ClusterRegistrationService nodeRegistrationService) {
		this.clusterRegistrationService = nodeRegistrationService;
	}

	public ClusterJobService getJobService() {
		return jobService;
	}

	public void setJobService(ClusterJobService jobService) {
		this.jobService = jobService;
	}
	
	

}
