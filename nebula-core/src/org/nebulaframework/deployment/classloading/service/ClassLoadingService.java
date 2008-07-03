package org.nebulaframework.deployment.classloading.service;

public interface ClassLoadingService {
	public byte[] findClass(String jobId, String name) throws ClassNotFoundException;

}
