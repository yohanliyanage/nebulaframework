package org.nebulaframework.core.grid.cluster.node.services.jobs;

import java.io.Serializable;

import javax.jms.ConnectionFactory;

import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;

public class JobSubmissionServiceImpl implements JobSubmissionService {

	private GridNode node;
	private ConnectionFactory connectionFactory;

	public JobSubmissionServiceImpl(GridNode node) {
		super();
		this.node = node;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public GridJobFuture submitJob(GridJob<? extends Serializable> job) {
		
		//Submit Job to Cluster and retrieve JobId
		String jobId = this.node.getServicesFacade().submitJob(this.node.getId(), job);
		
		// Create local proxy to interface remote service
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(connectionFactory);
		proxyFactory.setQueueName("nebula.jobs." + jobId + ".future.queue");
		proxyFactory.setServiceInterface(GridJobFuture.class);
		
		return (GridJobFuture) proxyFactory;
	}

}
