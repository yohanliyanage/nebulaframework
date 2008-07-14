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

import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.ClusterJobService;
import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * {@code ClusterManagerServicesFacade} is the facade for services of {@link ClusterManager}, 
 * which are to be exposed to the {@link GridNode}s. {@code GridNode}s access these services through the 
 * facade implementation.
 * <p>
 * The {@code ClusterManagerServicesFacade} implementation is exposed to the {@code GridNode}s as a 
 * JMS Remote Service, through Spring Framework's JMS Remoting support.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterManager
 * @see GridNode
 */
public interface ClusterManagerServicesFacade extends ClusterJobService {
	// Methods are inherited from extended interfaces
}
