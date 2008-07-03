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

public class ClusterJobServiceImpl implements ClusterJobService {

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

		String jobId = this.cluster.getClusterId() + "." + owner + "."
				+ UUID.randomUUID();

		GridJobProfile profile = new GridJobProfile();

		profile.setTaskQueueRef(jmsSupport.createTaskQueue(jobId));
		profile.setResultQueueRef(jmsSupport.createResultQueue(jobId));
		profile.setFutureQueueRef(jmsSupport.createFutureQueue(jobId));
		
		GridJobFutureImpl future = jmsSupport.createFuture(jobId, profile.getFutureQueueRef());

		profile.setJobId(jobId);
		profile.setOwner(owner);
		profile.setJob(job);
		profile.setFuture(future);
		profile.initCleanUpHandlers();
		
		this.jobs.put(jobId, profile);

		splitterService.startSplitter(profile);
		aggregatorService.startAggregator(profile);

		// Notify Job Start
		notifyJobStart(jobId);

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

	public void notifyJobStart(String jobId) {
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_START);

		cluster.getServiceMessageSender().sendServiceMessage(message);
	}

	public void notifyJobEnd(String jobId) {
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_END);

		cluster.getServiceMessageSender().sendServiceMessage(message);
	}

	public void notifyJobCancel(String jobId) {
		ServiceMessage message = new ServiceMessage(jobId);
		message.setType(ServiceMessageType.JOB_CANCEL);

		cluster.getServiceMessageSender().sendServiceMessage(message);
	}

	public GridJobProfile getProfile(String jobId) {
		return jobs.get(jobId);
	}

	public String requestJobClassName(String jobId) {
		try {
			return jobs.get(jobId).getJob().getClass().getName();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("No such Job with Id " + jobId);
		}
	}
}
