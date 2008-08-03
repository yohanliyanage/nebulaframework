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
package org.nebulaframework.grid.cluster.manager.services.heartbeat;

import java.util.UUID;

import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * Internal interface definition for {@code ClusterHeartBeatService},
 * which keeps track of HeartBeats from local {@link GridNode}s.
 * <p>
 * Internal interface extends the public interface,
 * but allows access to operations which are not exposed by the public API.
 * <p>
 * <b>Warning : </b>This is to be used by the internal system only, and is not a 
 * part of the public API. Use of this API is strongly discouraged. For the 
 * public API of this service, refer to {@link ClusterJobService}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface InternalClusterHeartBeatService extends ClusterHeartBeatService {
	
	/**
	 * Adds a {@code GridNode} to the collection of
	 * {@code GridNode}s tracked by the service.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	void addNode(UUID nodeId);
	
	/**
	 * Removes a {@code GridNode} from the collection of
	 * {@code GridNode}s tracked by the service.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	void removeNode(UUID nodeId);
}
