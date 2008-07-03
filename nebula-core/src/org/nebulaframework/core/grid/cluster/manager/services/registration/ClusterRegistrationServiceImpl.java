package org.nebulaframework.core.grid.cluster.manager.services.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.node.delegate.GridNodeDelegate;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationImpl;
import org.nebulaframework.deployment.classloading.node.exporter.support.GridNodeClassExporterSupport;
import org.springframework.beans.factory.annotation.Required;

public class ClusterRegistrationServiceImpl implements ClusterRegistrationService {

	private static Log log = LogFactory
			.getLog(ClusterRegistrationServiceImpl.class);

	private ClusterManager cluster;
	private ConnectionFactory connectionFactory;
	
	private Map<UUID, GridNodeDelegate> clusterNodes = new HashMap<UUID, GridNodeDelegate>();


	public ClusterRegistrationServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}


	/**
	 * {@inheritDoc}
	 */
	public Registration registerNode(UUID nodeId) {

		// Set the Registration Data
		RegistrationImpl reg = new RegistrationImpl();
		reg.setBrokerUrl(this.cluster.getBrokerUrl());
		reg.setUuid(nodeId);
		reg.setClusterId(this.cluster.getClusterId());

		GridNodeDelegate delegate = new GridNodeDelegate(nodeId);
		delegate.setClassExporter(GridNodeClassExporterSupport.createServiceProxy(nodeId,connectionFactory));
		this.clusterNodes.put(nodeId, delegate);

		log.info("Node registered [ID:" + nodeId + "]");

		// Return Registration Data to Node
		return reg;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterNode(UUID id) {
		this.clusterNodes.remove(id);
		log.info("Node unregistered [ID:" + id + "]");
	}

	public GridNodeDelegate getGridNodeDelegate(UUID nodeId) {
		if (this.clusterNodes.containsKey(nodeId)) {
			return this.clusterNodes.get(nodeId);
		}
		else {
			throw new IllegalArgumentException("No such Node registered with Id " + nodeId);
		}
	}

	
}
