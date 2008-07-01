package org.nebulaframework.core.grid.cluster.node.services.registration;

import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationService;
import org.nebulaframework.core.grid.cluster.registration.Registration;
import org.nebulaframework.core.grid.cluster.registration.RegistrationException;

public interface NodeRegistrationService {

	public abstract void setClusterRegistrationService(
			ClusterRegistrationService clusterManager);

	public abstract Registration getRegistration();

	public abstract void register() throws RegistrationException;

	public abstract void unregister();

}