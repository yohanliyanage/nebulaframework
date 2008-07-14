package org.nebulaframework.grid.cluster.manager.services.registration;

import java.util.UUID;

import org.nebulaframework.grid.cluster.node.delegate.GridNodeDelegate;

// TODO Fix Doc
public interface InternalClusterRegistrationService extends
		ClusterRegistrationService {

	/**
	 * Returns a reference to the {@code GridNodeDelegate} instance
	 * which represents a given {@code GridNode}, identified by the
	 * {@code nodeId}.
	 * 
	 * @param nodeId {@code UUID} Node Id
	 * 
	 * @return {@code GridNodeDelegate} delegate
	 * 
	 * @throws IllegalArgumentException if no such node exists
	 */
	public GridNodeDelegate getGridNodeDelegate(UUID nodeId);

}
