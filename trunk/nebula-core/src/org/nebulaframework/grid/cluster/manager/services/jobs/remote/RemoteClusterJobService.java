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
package org.nebulaframework.grid.cluster.manager.services.jobs.remote;

import org.nebulaframework.core.job.deploy.GridJobInfo;
import org.nebulaframework.core.job.exceptions.GridJobPermissionDeniedException;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNodeProfile;

/**
 * {@code RemoteClusterJobService} allows a {@code ClusterManager} to request a
 * {@code GridJob} which is managed by a remote cluster.
 * <p>
 * When a {@code GridNode} requires to participate for a {@code GridJob} managed
 * by a remote cluster, the node first requests its {@link ClusterManager} to
 * request permission to participate for the {@code GridJob}. If the
 * {@code GridJob} is not in the locally managed {@code GridJob}s of that
 * {@code ClusterManager}, the ({@code ClusterManager}) determines the
 * cluster which manages the {@code GridJob} by parsing the {@code GridJob}'s
 * Job ID, and invokes this service of the ClusterManager which manages the
 * {@code GridJob}, on behalf of the {@code GridNode}. The outcome of this
 * operation is then forwarded to the {@code GridNode}.
 * <p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface RemoteClusterJobService {

	/**
	 * Requests permission to participate for the specified {@code GridJob},
	 * denoted by {@code jobId}, for a remote {@code GridNode}.
	 * 
	 * @param jobId {@code GridJob} ID
	 * @param nodeProfile {@code GridNode}'s Profile
	 * 
	 * @return If request was upheld, {@code GridJobInfo} for {@code GridJob}.
	 * 
	 * @throws GridJobPermissionDeniedException if request was denied
	 * @throws IllegalArgumentException if jobId is invalid
	 */
	GridJobInfo remoteJobRequest(String jobId,  GridNodeProfile nodeProfile)
			throws GridJobPermissionDeniedException, IllegalArgumentException;

}