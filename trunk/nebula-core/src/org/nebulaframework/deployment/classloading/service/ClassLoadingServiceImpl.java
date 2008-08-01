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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.registration.InternalClusterRegistrationService;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.util.Assert;

/**
 * Implementation of the {@code ClassLoadingService}, which communicates 
 * with {@code GridJob} submitter {@code GridNode}'s {@code GridNodeClassExporter} 
 * service to obtain a class file which is not available for a worker {@code GridNode}.
 * <p>
 * The service attempts to retrieve the class file from the relevant {@code GridNode}
 * by communicating with the Grid Job registry of the {@link ClusterManager}. Once
 * the submitter node is resolved, the proxy for its {@code GridNodeClassExporter}
 * remote service is invoked to obtain the required class. 
 * <p>
 * The implementation consists of a local cache of remotely fetched Class definitions,
 * to improve performance. If a request for a class which is in the cache is received,
 * the class definition from the cache will be returned directly, without having to
 * request again from the {@code GridNode} again. This is highly likely as
 * multiple worker nodes may request for the same class repetitively, during
 * execution.
 * <p>
 * Note that the local cache will be cleared once the relevant GridJob has
 * finished execution. This is to allow hot-deployment support, and also to
 * ensure proper resource management.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClassLoadingService
 * @see GridNodeClassLoader
 * @see GridNodeClassExporter
 */
public class ClassLoadingServiceImpl implements ClassLoadingService {

	private static Log log = LogFactory.getLog(ClassLoadingServiceImpl.class);
	
	// Local Cache
	private Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
	
	private InternalClusterJobService jobService;			
	private InternalClusterRegistrationService regService;	

	/**
	 * Constructs a {@code ClassLoadingServiceImpl}.
	 * 
	 * @param jobService Internal JobService Reference
	 * @param regService Internal Registration Service Implementation Reference
	 * 
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public ClassLoadingServiceImpl(InternalClusterJobService jobService,
			InternalClusterRegistrationService regService) 
			throws IllegalArgumentException {
		
		super();
		
		// Check for null
		Assert.notNull(jobService);
		Assert.notNull(regService);
		
		this.jobService = jobService;
		this.regService = regService;
	}




	/**
	 * {@inheritDoc}
	 */
	public byte[] findClass(String jobId, String name)
			throws ClassNotFoundException {
		
		UUID ownerId = null;
		
		try {
			// Get the owner node
			ownerId = jobService.getProfile(jobId).getOwner();
		} catch (IllegalArgumentException e) {
			throw new ClassNotFoundException("Unable to load class " + name, e);
		}
		
		try {
			log.debug("[ClassLoadingService] Finding Class " + name);

			// Check for nulls
			Assert.notNull(ownerId);
			Assert.notNull(jobId);
			Assert.notNull(name);
			
			// Check in cache, if found, return
			synchronized (this) {
				if (this.cache.containsKey(name)) {
					
					CacheEntry entry = this.cache.get(name);
					byte[] bytes = entry.getBytes();
					
					// If same job, return class
					if (jobId.equals(entry.getJobId())) {
						return bytes;
						
					}
					
				}
			}
			
			byte[] bytes = findClass(ownerId, name);
			
			// If found, put to local cache
			if (bytes != null) {
				synchronized (this) {
					cache.put(name, new CacheEntry(jobId, name, bytes));
				}
				return bytes;
			}
			
			throw new ClassNotFoundException("Unable to find class");
			
		} catch (Exception e) { 
			log.debug("[ClassLoadingService] Cannot Find Class Due to Exception");
			throw new ClassNotFoundException(
					"ClassLoaderService Cannot find class due to exception", e);
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] findClass(UUID ownerId, String name)
			throws ClassNotFoundException, IllegalArgumentException {

		/* -- Remote Loading -- */

		// Get ClassExporter of owner node
		GridNodeClassExporter exporter = regService
				.getGridNodeDelegate(ownerId).getClassExporter();

		// Request class export
		byte[] bytes = exporter.exportClass(name);

		if (bytes!=null) {
			return bytes;
		}
		
		// Class Not Found
		log.debug("[ClassLoadingService] Cannot Find Class");
		throw new ClassNotFoundException(
				"ClassLoaderService cannot locate class");
		
	}
	
	/**
	 * Nested wrapper class for class definitions which are stored
	 * in the local cache. Allows to store meta data along
	 * with the class definition {@code bytes[]}.
	 * 
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	protected class CacheEntry  {
		
		private byte[] bytes;	// Class definition bytes
		private String name;	// Fully qualified class name
		private String jobId;	// JobId of the request

		/**
		 * Constructs a {@code CacheEntry} instance.
		 * 
		 * @param jobId JobId of request
		 * @param name Fully qualified name of Class
		 * @param bytes {@code byte[]} of class definition
		 * 
		 * @throws IllegalArgumentException if any argument is {@code null}.
		 */
		protected CacheEntry(final String jobId, final String name, final byte[] bytes) 
				throws IllegalArgumentException{
			
			super();
			
			// Check for null values
			Assert.notNull(jobId);
			Assert.notNull(name);
			Assert.notNull(bytes);
			
			this.bytes = bytes;
			this.name = name;
			this.jobId = jobId;
			
			// Cleanup Hook
			ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

				public void onServiceEvent(ServiceMessage message) {
					log.debug("[ClassLoadingService] Removed class from cache : " + name);
					cache.remove(name);
				}
				
			}, jobId, ServiceMessageType.JOB_END, ServiceMessageType.JOB_CANCEL);
		}

		/**
		 * Returns the {@code byte[]} of class definition
		 * 
		 * @return the {@code byte[]} of class definition
		 */
		protected byte[] getBytes() {
			return bytes;
		}

		/**
		 * Returns the size of the class definition, in bytes
		 * 
		 * @return size of class definition, in bytes
		 */
		protected int getByteSize() {
			return bytes.length;
		}
		
		/**
		 * Returns the fully qualified class name of the
		 * class.
		 * 
		 * @return fully qualified class name
		 */
		protected String getName() {
			return name;
		}

		/**
		 * Returns the JobID of the original request for class
		 * 
		 * @return JobID of request
		 */
		protected String getJobId() {
			return jobId;
		}
	}


}
