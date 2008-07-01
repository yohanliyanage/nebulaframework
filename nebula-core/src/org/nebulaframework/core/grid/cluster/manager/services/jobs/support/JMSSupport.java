package org.nebulaframework.core.grid.cluster.manager.services.jobs.support;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

public class JMSSupport {

	private ConnectionFactory connectionFactory;
	
	
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void createTaskQueue(String jobId) {
		new ActiveMQQueue("nebula.jobs." + jobId + ".task.queue");
	}

	public void createResultQueue(String jobId) {
		new ActiveMQQueue("nebula.jobs." + jobId + ".result.queue");
	}

	public GridJobFutureImpl createFuture(String jobId) {
		
		GridJobFutureImpl future = new GridJobFutureImpl(jobId);
		future.setState(GridJobState.ENQUEUED);
		
		// Export the Future to client side through Spring JMS Remoting
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(GridJobFuture.class);
		exporter.setService(future);
		
		// Create Message Listener Container
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestination(new ActiveMQQueue("nebula.jobs." + jobId + ".future.queue"));
		container.setMessageListener(exporter);
		
		return future;
	}

}
