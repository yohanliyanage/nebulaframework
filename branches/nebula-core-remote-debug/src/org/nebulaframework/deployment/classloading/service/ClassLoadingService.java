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

package org.nebulaframework.deployment.classloading.service;

import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.nebulaframework.grid.cluster.manager.ClusterManager;



/**
 * {@code ClassLoadingService} is a support service for {@code GridNodeClassLoader}
 * which communicates with {@code GridJob} submitter {@code GridNode}'s
 * {@code GridNodeClassExporter} service to obtain a class file which is not
 * available for a worker {@code GridNode}.
 * <p>
 * The service attempts to retrieve the class file from the relevant {@code GridNode}
 * by communicating with the Grid Job registry of the {@link ClusterManager}. Once
 * the submitter node is resolved, the proxy for its {@code GridNodeClassExporter}
 * remote service is invoked to obtain the required class. 
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridNodeClassLoader
 * @see GridNodeClassExporter
 */
public interface ClassLoadingService {
	
	/**
	 * Attempts to find the class definition for a given class, of a given 
	 * {@code GridJob} through the {@code GridNodeClassExporter} service
	 * of the submitter {@code GridNode}.
	 * 
	 * @param jobId JobId of {@code GridJob}
	 * @param name Class Name
	 * 
	 * @return A {@code byte[]} of the class definition
	 * 
	 * @throws ClassNotFoundException if class is not found
	 * @throws IllegalArgumentException if any of the arguments is {@code null}
	 */
	public byte[] findClass(String jobId, String name) 
			throws ClassNotFoundException, IllegalArgumentException;

}
