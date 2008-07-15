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


/**
 * Internal interface definition for {@code RemoteClusterJobService}, which
 * allows a {@code ClusterManager} to request a {@code GridJob} which is 
 * managed by a remote cluster.
 * <p>
 * Internal interface extends the public interface,
 * but allows to access operations which are not exposed by the public API.
 * <p>
 * <b>Warning : </b>This is to be used by the internal system only, and is not a 
 * part of the public API. Use of this API is strongly discouraged. For the 
 * public API of this service, refer to {@link RemoteClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see RemoteClusterJobService
 */
public interface InternalRemoteClusterJobService extends RemoteClusterJobService {

	/**
	 * Shutdowns the {@code RemoteClusterJobService} of this
	 * {@code ClusterManager}, by stopping the JMS Message Listener
	 * container.
	 */
	public void shutdown();
}
