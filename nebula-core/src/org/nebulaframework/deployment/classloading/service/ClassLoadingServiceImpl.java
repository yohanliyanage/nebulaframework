package org.nebulaframework.deployment.classloading.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationServiceImpl;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;

public class ClassLoadingServiceImpl implements ClassLoadingService {

	private static final int MAX_CACHE_SIZE_KB = 512;
	private static final int CACHE_GC_INITIAL_DELAY_SECS = 10;
	private static final int CACHE_GC_SEQ_DELAY_SECS = 10;
	
	private static Log log = LogFactory.getLog(ClassLoadingServiceImpl.class);
	private ClusterJobServiceImpl jobServiceImpl;
	private ClusterRegistrationServiceImpl regServiceImpl;
	
	private Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
	private int cacheSize = 0;
	private ClassCacheGC cacheGC = new ClassCacheGC();
	
	public ClassLoadingServiceImpl(ClusterManager cluster) {
		super();
	}

	@Required
	public void setJobServiceImpl(ClusterJobServiceImpl jobServiceImpl) {
		this.jobServiceImpl = jobServiceImpl;
	}

	@Required
	public void setRegServiceImpl(ClusterRegistrationServiceImpl regServiceImpl) {
		this.regServiceImpl = regServiceImpl;
	}

	public byte[] findClass(String jobId, String name)
			throws ClassNotFoundException {
		try {
			log.debug("ClassLoadingServiceImpl finding class " + name);

			if (this.cache.containsKey(name)) {
				synchronized (this) {
					CacheEntry entry = this.cache.get(name);
					return entry.getBytes();
				}
			}
			
			// Get the owner node
			UUID ownerId = jobServiceImpl.getProfile(jobId).getOwner();

			// Get ClassExporter of owner node
			GridNodeClassExporter exporter = regServiceImpl
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
			}

			return bytes;
		} catch (NullPointerException e) {
			throw new ClassNotFoundException(
					"ClassLoaderService cannot locate class", e);
		}
	}


	private class CacheEntry  {
		
		private byte[] bytes;
		private String name;
		private String jobId;

		public CacheEntry(String jobId, String name, byte[] bytes) {
			super();
			this.bytes = bytes;
			this.name = name;
			this.jobId = jobId;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public int getByteSize() {
			return bytes.length;
		}
		
		public String getName() {
			return name;
		}

		public String getJobId() {
			return jobId;
		}


	}

	private class ClassCacheGC implements Runnable {

		ScheduledThreadPoolExecutor executorService;
		
		public ClassCacheGC() {
			super();
			log.debug("[Cache GC] Created");
			executorService = new ScheduledThreadPoolExecutor(1);
			executorService.scheduleWithFixedDelay(this, CACHE_GC_INITIAL_DELAY_SECS, CACHE_GC_SEQ_DELAY_SECS, TimeUnit.SECONDS);
		}

		protected void forceGarbageCollection() {
			// Remove Current Schedules
			executorService.remove(this);
			
			// Execute Now
			executorService.execute(this);
			
			// Reschedule Future Executions
			executorService.scheduleWithFixedDelay(this, CACHE_GC_SEQ_DELAY_SECS, CACHE_GC_SEQ_DELAY_SECS, TimeUnit.SECONDS);
		
		}
		
		public void run() {
			log.debug("[Cache GC] Running");
			
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
					if (!jobServiceImpl.isActiveJob(entry.getJobId())) {
						cacheSize -= entry.getByteSize();
						released += entry.getByteSize();
						iterator.remove(); // Remove from cache
					}
				}
			}
			
			stopWatch.stop();
			log.debug("[Cache GC] Finished [Released " + released + " bytes. "+ stopWatch.getTotalTimeMillis() +" ms]");
		}
		
	}
}
