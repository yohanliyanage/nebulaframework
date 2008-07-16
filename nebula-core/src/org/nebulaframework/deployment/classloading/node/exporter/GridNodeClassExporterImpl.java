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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.nebulaframework.util.io.IOSupport;

/**
 * Implementation of {@code GridNodeClassExporter},  that allows the local 
 * {@code GridNode} to export classes to worker {@code GridNode}s which
 * cannot load a required class locally, for a job submitted this local node. 
 * This is a remote enabled service, which runs on each {@code GridNode}.
 * <p>
 * This is done through the {@code ClassLoadingService}, which is implemented
 * as a remote service at the {@code ClusterManager}. When a worker node
 * requests a class from the {@code ClassLoadingService}, it calls the
 * {@link GridNodeClassExporter} service remotely, using a client proxy,
 * and attempts to obtain the class file. 
 * <p>
 * Note that visibility of this class is <b>default</b> (package private) scope
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridNodeClassExporter
 * @see ClassLoadingService
 */
class GridNodeClassExporterImpl implements GridNodeClassExporter {

	private static Log log = LogFactory.getLog(GridNodeClassExporterImpl.class);
	
	/**
	 * {@inheritDoc}
	 */
	public byte[] exportClass(String name) throws ClassNotFoundException {
		try {
			
			// Build the physical class name
			String resName = "/" + name.replaceAll("\\.", "/") + ".class";
			
			// Attempt to get the input stream for the class file
			InputStream is = Class.forName(name).getResourceAsStream(resName);
			
			if (is==null) {
				log.warn("[GridNodeClassExporter] InputStream NULL : " + resName);
				throw new IOException("Error retreiving InputStream for Class file, NULL");
			}

			log.debug("[GridNodeClassExporter] Exporting " + name);
			return IOSupport.readBytes(is);
		}
		catch (IOException ex) {
			log.warn("Unable to export class due to IOException", ex);
			throw new ClassNotFoundException("Unable to export class due to IOException", ex);
		}
		catch (NullPointerException ex) {
			log.warn("Unable to locate class with in Node", ex);
			throw new ClassNotFoundException("Unable to locate class with in Node", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String classHash(String name) throws ClassNotFoundException {
		
		// Fetch Class
		byte[] bytes = exportClass(name);
		
		// Generate Hash and Return
		return SHA1Generator.generateAsString(bytes);
	}

}
