package org.nebulaframework.core.grid.cluster.manager.services.jobs.support;

public class JMSNamingSupport {
	
	public static String getTaskQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".task.queue";
	}

	public static String getResultQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".result.queue";
	}

	public static String getFutureQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".future.queue";
	}
}
