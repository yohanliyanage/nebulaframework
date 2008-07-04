package org.nebulaframework.core.grid.cluster.manager.services.jobs.support;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;
import org.springframework.util.Assert;

public class JMSSupport {

	private ConnectionFactory connectionFactory;
	
	
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ActiveMQQueue createTaskQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getTaskQueueName(jobId));
	}

	public ActiveMQQueue createResultQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getResultQueueName(jobId));
	}

	public ActiveMQQueue createFutureQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getFutureQueueName(jobId));
	}
	
	public GridJobFutureImpl createFuture(String jobId) {
		
		GridJobFutureImpl future = new GridJobFutureImpl(jobId);
		future.setState(GridJobState.WAITING);
		
		// Export the Future to client side through Spring JMS Remoting
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(GridJobFuture.class);
		exporter.setService(future);
		
		exporter.afterPropertiesSet();
		
		Assert.notNull(future);
		
		// Create Message Listener Container
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestination(createFutureQueue(jobId));
		container.setMessageListener(exporter);
		container.afterPropertiesSet();
		return future;
	}
}
