package org.nebulaframework.deployment.classloading;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.springframework.util.Assert;



public class GridNodeClassLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(GridNodeClassLoader.class);
	
	private String jobId;
	private ClassLoadingService classLoadingService;
	
	private static Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();
	
	public GridNodeClassLoader(String jobId,
			ClassLoadingService classLoadingService, final ClassLoader parent) {
		
		super(parent);
		
		Assert.notNull(jobId);
		Assert.notNull(classLoadingService);
		
		this.jobId = jobId;
		this.classLoadingService = classLoadingService;
	}

	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		log.debug("GridNodeClassLoader loading Class : " + name);
		return super.loadClass(name);
	}


	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		log.debug("GridNodeClassLoader finding Class : " + name);
		
		// Check local cache
		synchronized(GridNodeClassLoader.class) {
			Class<?> cls = loaded.get(name);
			if (cls != null) {
				log.debug("Found Class in Local Cache : " + name);
				return cls;
			}
		}
		
		// If not, remote load
		
		try {
			
			log.debug("Attempting Remote Loading Class : " + name);
			
			// Get bytes for class from remote service
			byte[] bytes = classLoadingService.findClass(jobId, name);
			Class<?> cls = defineClass(name, bytes, 0, bytes.length);
			
			log.debug("Remote Loaded Class : " + name);
			
			// Put into local cache
			synchronized(GridNodeClassLoader.class) {
				loaded.put(name, cls);
			}
			
			return cls;
		} catch (Exception ex) {
			log.warn("Exception while loading remote class", ex);
			throw new ClassNotFoundException("ClassNotFound due to Exception",ex);
		}
	}

	
}
