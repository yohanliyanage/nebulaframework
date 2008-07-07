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

package org.nebulaframework.deployment.classloading.node.exporter;

import org.nebulaframework.deployment.classloading.service.ClassLoadingService;


/**
 * {@code GridNodeClassExporter} allows a {@code GridNode} which submits
 * a {@code GridJob} to export classes to worker {@code GridNode}s which
 * cannot load a required class locally. This is a remote enabled service,
 * which runs on each {@code GridNode}.
 * <p>
 * This is done through the {@code ClassLoadingService}, which is implemented
 * as a remote service at the {@code ClusterManager}. When a worker node
 * requests a class from the {@code ClassLoadingService}, it calls the
 * {@link GridNodeClassExporter} service remotely, using a client proxy,
 * and attempts to obtain the class file. 
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClassLoadingService
 */
public interface GridNodeClassExporter {
	
	/**
	 * Exports a requested class to the {@link ClassLoadingService},
	 * which in turn would send it to a requesting worker node.
	 * 
	 * @param name binary name of the class file
	 * @return The {@code byte[]} of the class file
	 * @throws ClassNotFoundException if class cannot be found
	 */
	public byte[] exportClass(String name) throws ClassNotFoundException;
}
