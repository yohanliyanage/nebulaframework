package org.nebulaframework.core.grid.cluster.manager.services.registration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.ServiceMessage;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationImpl;

public class ClusterRegistrationServiceImpl implements ClusterRegistrationService {

	private static Log log = LogFactory
			.getLog(ClusterRegistrationServiceImpl.class);

	private ClusterManager cluster;
	private Set<UUID> clusterNodes = new HashSet<UUID>();


	public ClusterRegistrationServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
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

		this.clusterNodes.add(nodeId);

		log.info("Node registered [ID:" + nodeId + "]");

		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(2000);
					cluster.getServiceMessageSender().sendServiceMessage(
							new ServiceMessage("Welcome to Nebula Cluster "
									+ cluster.getClusterId()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}).start();
		
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

}
