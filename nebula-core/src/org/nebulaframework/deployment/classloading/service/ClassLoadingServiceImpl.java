package org.nebulaframework.deployment.classloading.service;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationServiceImpl;
import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;
import org.springframework.beans.factory.annotation.Required;

public class ClassLoadingServiceImpl implements ClassLoadingService {

	private static Log log = LogFactory.getLog(ClassLoadingServiceImpl.class);
	private ClusterJobServiceImpl jobServiceImpl;
	private ClusterRegistrationServiceImpl regServiceImpl;
	
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

	public byte[] findClass(String jobId, String name) throws ClassNotFoundException {
		try {
			log.debug("ClassLoadingServiceImpl finding class " + name);
			UUID ownerId = jobServiceImpl.getProfile(jobId).getOwner();
			GridNodeClassExporter exporter = regServiceImpl.getGridNodeDelegate(ownerId).getClassExporter();
			return exporter.exportClass(name);
		} catch (NullPointerException e) {
			throw new ClassNotFoundException("ClassLoaderService cannot locate class", e);
		}
	}

}
