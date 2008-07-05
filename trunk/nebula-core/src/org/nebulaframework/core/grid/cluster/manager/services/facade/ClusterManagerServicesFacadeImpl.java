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

package org.nebulaframework.core.grid.cluster.manager.services.facade;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;

/**
 * <p>This is the implementation of {@link ClusterManagerServicesFacade} interface. This facade allows
 * {@link GridNode}s of the cluster to access the services offered by {@link ClusterManager}.<p> 
 * 
 * <p>This class is exposed to the <tt>GridNode</tt>s as a JMS Remote Service, through 
 * Spring Framework's JMS Remoting support.</p>
 * 
 * <p><i>Spring Managed</i></p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManagerServicesFacade
 */
public class ClusterManagerServicesFacadeImpl implements ClusterManagerServicesFacade{

	private ClusterManager cluster;	// Reference to ClusterManager instance
	
	/**
	 * Instantiates an instance of <tt>ClusterManagerServicesFacadeImpl</tt> for the 
	 * given {@link ClusterManager}.
	 * 
	 * @param cluster {@link ClusterManager} of the Cluster.
	 */
	public ClusterManagerServicesFacadeImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}

	/**
	 * Delegates to {@link ClusterManager}'s {@link ClusterJobService} to submit a given {@link GridJob}.
	 * 
	 * @see ClusterJobService#submitJob(UUID, GridJob)
	 */
	public String submitJob(UUID owner, GridJob<? extends Serializable> job) throws  GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, job);
	}

	/**
	 * Delegates to {@link ClusterManager}'s {@link ClusterJobService} to submit a given {@link GridJob},
	 * which is included in the given {@link GridArchive}.
	 * 
	 * @see ClusterJobService#submitJob(UUID, GridJob, GridArchive)
	 */
	public String submitJob(UUID owner, GridJob<? extends Serializable> job,
			GridArchive archive) throws GridJobRejectionException {
		return this.cluster.getJobService().submitJob(owner, job, archive);
	}

	/**
	 * Delegates to {@link ClusterManager}'s {@link ClusterJobService} to request permission to participate
	 * for a specified {@link GridJob}.
	 * 
	 * @see ClusterJobService#requestJob(String)
	 */
	public GridJobInfo requestJob(String jobId) throws GridJobPermissionDeniedException {
		return this.cluster.getJobService().requestJob(jobId);
	}

}
