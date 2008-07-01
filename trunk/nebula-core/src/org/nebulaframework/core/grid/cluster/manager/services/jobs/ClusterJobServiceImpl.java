package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSSupport;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFutureImpl;

public class ClusterJobServiceImpl implements ClusterJobService  {

	private ClusterManager cluster;
	private JMSSupport jmsSupport;
	
	private Map<String, GridJobProfile> jobs = new HashMap<String, GridJobProfile>();
	
	
	public ClusterJobServiceImpl(ClusterManager cluster) {
		super();
		this.cluster = cluster;
	}


	/**
	 * {@inheritDoc}
	 */
	public String submitJob(UUID owner, GridJob<? extends Serializable> job) {
		
		String jobId = this.cluster.getClusterId() + "." + owner + "." + UUID.randomUUID();
		
		GridJobProfile profile = new GridJobProfile();
		
		jmsSupport.createTaskQueue(jobId);
		jmsSupport.createResultQueue(jobId);
		GridJobFutureImpl future = jmsSupport.createFuture(jobId);
		
		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setJob(job);
		profile.setFuture(future);
		
		this.jobs.put(jobId, profile);
		
		return jobId;
	}


	public void setJmsSupport(JMSSupport jmsSupport) {
		this.jmsSupport = jmsSupport;
	}

	

}
