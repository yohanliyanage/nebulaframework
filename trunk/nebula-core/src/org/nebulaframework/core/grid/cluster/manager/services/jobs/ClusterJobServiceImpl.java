package org.nebulaframework.core.grid.cluster.manager.services.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator.AggregatorService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter.SplitterService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSSupport;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.nebulaframework.core.servicemessage.ServiceMessage;
import org.nebulaframework.core.servicemessage.ServiceMessageType;

public class ClusterJobServiceImpl implements ClusterJobService  {

	private ClusterManager cluster;
	private JMSSupport jmsSupport;
	
	private Map<String, GridJobProfile> jobs = new HashMap<String, GridJobProfile>();
	
	private SplitterService splitterService;
	private AggregatorService aggregatorService;
	
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
		
		splitterService.startSplitter(profile);
		aggregatorService.startAggregator(profile);
		
		//Notify Job Start
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_START);

		cluster.getServiceMessageSender().sendServiceMessage(message);
		return jobId;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean requestJob(String jobId) {
		// FIXME hard coded to return true.
		return true;
	}

	public void setJmsSupport(JMSSupport jmsSupport) {
		this.jmsSupport = jmsSupport;
	}


	public SplitterService getSplitterService() {
		return splitterService;
	}


	public void setSplitterService(SplitterService splitterService) {
		this.splitterService = splitterService;
	}


	public AggregatorService getAggregatorService() {
		return aggregatorService;
	}


	public void setAggregatorService(AggregatorService aggregatorService) {
		this.aggregatorService = aggregatorService;
	}




	

	

}
