package org.nebulaframework.core.grid.cluster.node.services.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;

public class NodeRegistrationServiceImpl implements NodeRegistrationService {

	private static Log log = LogFactory.getLog(NodeRegistrationServiceImpl.class);
	
	private GridNode node;
	private Registration registration;
	private ClusterRegistrationService clusterRegistrationService;
	
	public NodeRegistrationServiceImpl(GridNode node) {
		super();
		this.node = node;
	}
	
	/* (non-Javadoc)
	 * @see org.nebulaframework.core.grid.cluster.node.services.registration.NodeRegistrationService#setClusterRegistrationService(org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService)
	 */
	public void setClusterRegistrationService(ClusterRegistrationService clusterManager) {
		this.clusterRegistrationService = clusterManager;
	}
	
	/* (non-Javadoc)
	 * @see org.nebulaframework.core.grid.cluster.node.services.registration.NodeRegistrationService#getRegistration()
	 */
	public Registration getRegistration() {
		return registration;
	}

	/* (non-Javadoc)
	 * @see org.nebulaframework.core.grid.cluster.node.services.registration.NodeRegistrationService#register()
	 */
	public void register() throws RegistrationException {
		
		if (registration != null) {
			throw new IllegalStateException("Unable to register. Already registered with a Cluster");
		}
		
		this.registration = clusterRegistrationService.registerNode(node.getId());

		log.info("Node " + node.getId() + " registered in Cluster "
				+ registration.getClusterId());
		log.debug("Broker URL : " + registration.getBrokerUrl());
	}

	/* (non-Javadoc)
	 * @see org.nebulaframework.core.grid.cluster.node.services.registration.NodeRegistrationService#unregister()
	 */
	public void unregister() {
		
		if (registration == null) {
			throw new IllegalStateException("Unable to unregister. Not registered with any Cluster");
		}
		
		this.clusterRegistrationService.unregisterNode(node.getId());
		log.info("Node " + node.getId() + " unregistered from Cluster");
	}	
}
