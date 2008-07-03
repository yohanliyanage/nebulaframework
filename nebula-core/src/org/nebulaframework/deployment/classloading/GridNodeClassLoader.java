package org.nebulaframework.deployment.classloading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.springframework.util.Assert;



public class GridNodeClassLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(GridNodeClassLoader.class);
	
	private String jobId;
	private ClassLoadingService classLoadingService;
	
	
	public GridNodeClassLoader(String jobId,
			ClassLoadingService classLoadingService) {
		super();
		
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
		try {
			byte[] bytes = classLoadingService.findClass(jobId, name);
			log.debug("Bytes : " + bytes);
			return defineClass(name, bytes, 0, bytes.length);
		} catch (Exception ex) {
			log.warn("Exception while loading remote class", ex);
			throw new ClassNotFoundException("ClassNotFound due to Exception",ex);
		}
	}

	
}
