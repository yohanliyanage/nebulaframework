/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.nebulaframework.grid.cluster.manager.services.facade;

import java.util.UUID;

import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;

/**
 * Implementation of {@code ClusterManagerServicesFacade} interface. Allows
 * {@code GridNode}s of the cluster to access the services offered by
 * {@code ClusterManager}.
 * <p>
 * This class is exposed to the {@code GridNode}s as a JMS Remote Service,
 * through Spring Framework's JMS Remoting support.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManagerServicesFacade
 */
public class ClusterManagerServicesFacadeImpl implements
		ClusterManagerServicesFacade {

	private ClusterManager cluster; // Reference to ClusterManager instance

	/**
	 * Instantiates an instance of {@code ClusterManagerServicesFacadeImpl} for
	 * the given {@code ClusterManager}.
	 * 
	 * @param cluster
	 *            {@code ClusterManager} of the Cluster.
	 */
	public ClusterManagerServicesFacadeImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * submit a given {@code GridJob}.
	 * 
	 * @see ClusterJobService#submitJob(UUID,  String, byte[], GridArchive)
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData)
			throws GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, className, classData);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * submit a given {@code GridJob}.
	 * 
	 * @see ClusterJobService#submitJob(UUID,  String, byte[], GridArchive, String)
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData,
			String resultCallbackQueue) throws GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, className, classData, resultCallbackQueue);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * submit a given {@code GridJob}, which is included in the given
	 * {@code GridArchive}.
	 * 
	 * @see GridArchive
	 * @see ClusterJobService#submitJob(UUID,  String, byte[], GridArchive, String)
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData, GridArchive archive,
			String resultCallbackQueue) throws GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, className, classData, archive, resultCallbackQueue);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * submit a given {@code GridJob}, which is included in the given
	 * {@code GridArchive}.
	 * 
	 * @see GridArchive
	 * @see ClusterJobService#submitJob(UUID, String, byte[], GridArchive)
	 */
	@Override
	public String submitJob(UUID owner, String className, byte[] classData, GridArchive archive)
			throws GridJobRejectionException {
		// If SplitAggregate this, if not other
		return this.cluster.getJobService().submitJob(owner, className, classData, archive);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * request permission to participate for a specified {@code GridJob}.
	 * 
	 * @see ClusterJobService#requestJob(String)
	 */
	@Override
	public GridJobInfo requestJob(String jobId, GridNodeProfile nodeProfile)
			throws GridJobPermissionDeniedException {
		return this.cluster.getJobService().requestJob(jobId, nodeProfile);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@code ClusterManager}'s {@code ClusterJobService} to
	 * request permission to participate for next available {@code GridJob}.
	 */
	@Override
	public GridJobInfo requestNextJob(GridNodeProfile nodeProfile) {
		return this.cluster.getJobService().requestNextJob(nodeProfile);
	}



}
