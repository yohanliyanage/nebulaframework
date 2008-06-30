package org.nebulaframework.core.grid.cluster.node;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.ServiceMessage;
import org.nebulaframework.core.grid.cluster.manager.services.registration.NodeRegistrationService;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;
import org.nebulaframework.core.support.ID;

public class GridNode {

	private static Log log = LogFactory.getLog(GridNode.class);

	private UUID id;
	private Registration registration;
	private GridNodeProfile profile;
	private NodeRegistrationService clusterRegistrationService;
	
	public GridNode(GridNodeProfile profile) {
		super();
		this.id = ID.getId();
		this.profile = profile;
		log.debug("Node " + id + " created");
	}

	public UUID getId() {
		return id;
	}

	public void setClusterRegistrationService(NodeRegistrationService clusterManager) {
		this.clusterRegistrationService = clusterManager;
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
		
		this.registration = clusterRegistrationService.registerNode(id);

		log.info("Node " + id + " registered in Cluster "
				+ registration.getClusterId());
		log.debug("Broker URL : " + registration.getBrokerUrl());
	}

	public void unregister() {
		
		if (registration == null) {
			throw new IllegalStateException("Unable to unregister. Not registered with any Cluster");
		}
		
		this.clusterRegistrationService.unregisterNode(this.id);
		log.info("Node " + id + " unregistered from Cluster");
	}

	public void onServiceMessage(ServiceMessage obj) {
		log.info(obj);
	}

}
