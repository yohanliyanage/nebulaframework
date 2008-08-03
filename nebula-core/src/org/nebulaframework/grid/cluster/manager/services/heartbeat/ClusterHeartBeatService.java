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

import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * {@code ClusterHeartBeatService} keeps track of 
 * HeartBeats from local {@link GridNode}s. This service
 * is exposed as a remote service and all {@code GridNode}s
 * are to invoke the {@link #beat(UUID)} method in cycles of
 * {@link #BEAT_PERIOD}.
 * <p>
 * If a {@link GridNode} fails to provide {@link #MAX_MISS}
 * number of heart beats consecutively, the node is considered
 * to be failed, and a ServiceMessage will be dispatched 
 * regarding the event.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ClusterHeartBeatService {

	/** Period between heart beats in milliseconds */
	public static final long BEAT_PERIOD = 2500L;
	
	/** Maximum allowed missed beats before considering disconnected */
	public static final int MAX_MISS = 3;
	
	/**
	 * Invoked to notify a heartbeat from the {@code GridNode}
	 * with the given nodeId.
	 * 
	 * @param nodeId {@code GridNode} Id
	 */
	public void beat(UUID nodeId);
}
