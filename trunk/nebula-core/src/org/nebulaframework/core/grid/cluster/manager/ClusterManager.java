package org.nebulaframework.core.grid.cluster.manager;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.messaging.ServiceMessageSender;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.support.ID;
import org.nebulaframework.deployment.classloading.service.support.ClassLoadingServiceSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class ClusterManager implements InitializingBean {

	private UUID clusterId;
	private String brokerUrl;
	private ConnectionFactory connectionFactory;
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

	@Required
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public ServiceMessageSender getServiceMessageSender() {
		return serviceMessageSender;
	}

	@Required
	public void setServiceMessageSender(
			ServiceMessageSender serviceMessageSender) {
		this.serviceMessageSender = serviceMessageSender;
	}

	public ClusterRegistrationService getClusterRegistrationService() {
		return clusterRegistrationService;
	}

	@Required
	public void setClusterRegistrationService(
			ClusterRegistrationService nodeRegistrationService) {
		this.clusterRegistrationService = nodeRegistrationService;
	}

	public ClusterJobService getJobService() {
		return jobService;
	}

	@Required
	public void setJobService(ClusterJobService jobService) {
		this.jobService = jobService;
	}

	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void afterPropertiesSet() throws Exception {
		ClassLoadingServiceSupport.startClassLoadingService(this, connectionFactory);
	}
	
}
