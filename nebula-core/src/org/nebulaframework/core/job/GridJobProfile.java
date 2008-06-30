package org.nebulaframework.core.job;

public class GridJobProfile {

	private String jobId;
	private String jar;
	private String taskQueueName;
	private String resultQueueName;
	private String jobTopicName;
	
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJar() {
		return jar;
	}

	public void setJar(String jar) {
		this.jar = jar;
	}

	public String getTaskQueueName() {
		return taskQueueName;
	}

	public void setTaskQueueName(String taskQueueName) {
		this.taskQueueName = taskQueueName;
	}

	public String getResultQueueName() {
		return resultQueueName;
	}

	public void setResultQueueName(String resultQueueName) {
		this.resultQueueName = resultQueueName;
	}

	public String getJobTopicName() {
		return jobTopicName;
	}

	public void setJobTopicName(String jobTopicName) {
		this.jobTopicName = jobTopicName;
	}

}
