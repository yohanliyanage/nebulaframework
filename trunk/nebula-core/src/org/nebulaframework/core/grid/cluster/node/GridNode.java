package org.nebulaframework.core.grid.cluster.node;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.ServiceMessage;
import org.nebulaframework.core.grid.cluster.manager.services.registration.NodeRegistrationService;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.nebulaframework.core.support.ID;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GridNode implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(GridNode.class);

	private UUID id;
	private Registration registration;
	private GridNodeProfile profile;
	private NodeRegistrationService clusterManager;
	private ApplicationContext applicationContext;
	
	public GridNode(GridNodeProfile profile) {
		super();
		this.id = ID.getId();
		this.profile = profile;
		log.debug("Node " + id + " created");
	}

	public UUID getId() {
		return id;
	}

	public NodeRegistrationService getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(NodeRegistrationService clusterManager) {
		this.clusterManager = clusterManager;
	}

	public GridNodeProfile getProfile() {
		return profile;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void register() throws RegistrationException {
		
		if (registration != null) {
			throw new IllegalStateException("Unable to register. Already registered with a Cluster");
		}
		
		this.registration = clusterManager.registerNode(id);

		log.info("Node " + id + " registered in Cluster "
				+ registration.getClusterId());
		log.debug("Broker URL : " + registration.getBrokerUrl());
	}

	public void unregister() {
		
		if (registration == null) {
			throw new IllegalStateException("Unable to unregister. Not registered with any Cluster");
		}
		
		this.clusterManager.unregisterNode(this.id);
		log.info("Node " + id + " unregistered from Cluster");
	}

	public void onServiceMessage(ServiceMessage message) {
		log.info("Recieved Service Message : " + message.toString());
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
