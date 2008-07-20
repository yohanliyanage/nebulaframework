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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.registration.InternalClusterRegistrationService;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

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
 * However, maintaining a cache which contains class files across multiple jobs
 * results in resource management issues. To overcome this, a special timed 
 * clean up mechanism is also implemented, referred to as {@code ClassCacheGC}, 
 * (Class Cache Garbage Collector). See {@link ClassCacheGC} for information.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClassLoadingService
 * @see GridNodeClassLoader
 * @see GridNodeClassExporter
 */
public class ClassLoadingServiceImpl implements ClassLoadingService {

	/**
	 * Maximum Cache Size, in KB. The cache will be cleaned up
	 * by the garbage collector after reaching this amount, by
	 * forcefully removing class definitions from it.
	 */
	private static final int MAX_CACHE_SIZE_KB = 512;
	
	/**
	 * CacheGC Initial Delay of Garbage Collection (Seconds)
	 */
	private static final int CACHE_GC_INITIAL_DELAY_SECS = 5*60;
	
	/**
	 * CacheGC Sequential Delay of Garbage Collection (Seconds)
	 */
	private static final int CACHE_GC_SEQ_DELAY_SECS = 3*60;
	
	
	private static Log log = LogFactory.getLog(ClassLoadingServiceImpl.class);
	
	// Cache
	private Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
	
	private int cacheSize = 0;							// Current Cache Size (KB)
	private ClassCacheGC cacheGC = new ClassCacheGC();	// Garbage Collector
	
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
		
		// Check for nulls
		Assert.notNull(jobId);
		Assert.notNull(name);
		
		try {
			log.debug("[ClassLoadingService] Finding Class " + name);

			// Check in cache, if found, return
			synchronized (this) {
				if (this.cache.containsKey(name)) {
					CacheEntry entry = this.cache.get(name);
					byte[] bytes = entry.getBytes();
					
					// If same job, return class
					if (jobId.equals(entry.getJobId())) {
						return bytes;
						
					}
					// If old class, check if class has changed
					if (SHA1Generator.generateAsString(bytes).equals(getHash(jobId, name))) {
						// If Class has not changed (hash match)
						return bytes;
					}
					else {
						log.debug("[ClassLoadingService] Class Updated " + name);
					}
				}
			}
			
			/* -- Remote Loading -- */
			
			// Get the owner node
			UUID ownerId = jobService.getProfile(jobId).getOwner();

			// Get ClassExporter of owner node
			GridNodeClassExporter exporter = regService
					.getGridNodeDelegate(ownerId).getClassExporter();

			// Request class export
			byte[] bytes = exporter.exportClass(name);

			// If found, put to local cache
			if (bytes != null) {
				synchronized (this) {
					cache.put(name, new CacheEntry(jobId, name, bytes));
					cacheSize += bytes.length;
					
					// If cache size exceeds limit, force GC
					if (cacheSize > MAX_CACHE_SIZE_KB * 1024) {
						cacheGC.forceGarbageCollection();
					}
				}
				return bytes;
			}
			
			// Class Not Found
			log.debug("[ClassLoadingService] Cannot Find Class");
			throw new ClassNotFoundException(
					"ClassLoaderService cannot locate class");
			
		} catch (Exception e) { 
			log.debug("[ClassLoadingService] Cannot Find Class Due to Exception");
			throw new ClassNotFoundException(
					"ClassLoaderService Cannot find class due to exception", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHash(String jobId, String name)
			throws ClassNotFoundException, IllegalArgumentException {
		
		// Get the owner node
		UUID ownerId = jobService.getProfile(jobId).getOwner();

		// Get ClassExporter of owner node
		GridNodeClassExporter exporter = regService
				.getGridNodeDelegate(ownerId).getClassExporter();

		// Request class hash
		return exporter.classHash(name);
	}
	
	/**
	 * Nested wrapper class for class definitions which are stored
	 * in the local cache. Allows to store meta data along
	 * with the class definition {@code bytes[]}.
	 * 
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	protected static class CacheEntry  {
		
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
		protected CacheEntry(String jobId, String name, byte[] bytes) 
				throws IllegalArgumentException{
			
			super();
			
			// Check for null values
			Assert.notNull(jobId);
			Assert.notNull(name);
			Assert.notNull(bytes);
			
			this.bytes = bytes;
			this.name = name;
			this.jobId = jobId;
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

	/**
	 * Inner class which manages the garbage collection (clean up)
	 * of the local class cache. This class implements {@code Runnable}
	 * interface, and it is scheduled for execution at fixed rate using
	 * a ThreadPoolExecutor, using a single Thread, to ensure performance.
	 *  
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	private class ClassCacheGC implements Runnable {

		// Thread Executor
		ScheduledThreadPoolExecutor executorService;
		
		/**
		 * Constructors a new {@code ClassCacheGC} Garbage Collector,
		 * and configures the Thread Executor.
		 */
		public ClassCacheGC() {
			super();
			log.debug("[Class Cache GC] Created");
			
			// Construct Thread Pool Executor with 1 Thread
			executorService = new ScheduledThreadPoolExecutor(1);
			executorService.setThreadFactory(new ThreadFactory(){

				// Use a Custom ThreadFactory to make use 
				// of MIN_PRIORITY threads for GC
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setPriority(Thread.MIN_PRIORITY);
					return t;
				}
			
			});
			
			executorService.scheduleWithFixedDelay(this, CACHE_GC_INITIAL_DELAY_SECS, CACHE_GC_SEQ_DELAY_SECS, TimeUnit.SECONDS);
		}

		/**
		 * Forces garbage collection by requesting the garbage collection
		 * to occur as soon as possible, and also reschedules the 
		 * future execution schedules.
		 */
		protected void forceGarbageCollection() {
			// Remove Current Schedules
			executorService.remove(this);
			
			// Execute Now
			executorService.execute(this);
			
			// Reschedule Future Executions
			executorService.scheduleWithFixedDelay(this, CACHE_GC_SEQ_DELAY_SECS, CACHE_GC_SEQ_DELAY_SECS, TimeUnit.SECONDS);
		
		}
		
		/**
		 * The garbage collection process. Removes all inactive classes (inactive jobs).
		 */
		public void run() {
			
			// QoS Timing Statistics
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			// # of released bytes
			int released = 0;
			
			synchronized(ClassLoadingServiceImpl.this) { 
				
				Iterator<CacheEntry> iterator = cache.values().iterator();
				
				while(iterator.hasNext()) {

					CacheEntry entry = iterator.next();
					
					// If Job is not Active anymore
					if (!jobService.isActiveJob(entry.getJobId())) {
						cacheSize -= entry.getByteSize();
						released += entry.getByteSize();
						iterator.remove(); // Remove from cache
					}
				}
			}
			
			stopWatch.stop();
			log.debug("[Class Cache GC] Released " + released + " bytes. "+ stopWatch.getTotalTimeMillis() +" ms");
		}
		
	}

}
