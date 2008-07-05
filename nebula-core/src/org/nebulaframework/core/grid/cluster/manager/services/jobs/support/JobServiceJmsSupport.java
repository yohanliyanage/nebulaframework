/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.nebulaframework.core.grid.cluster.manager.services.jobs.support;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.core.job.future.GridJobFutureImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

/**
 * Provides support methods which assists {@link ClusterJobServiceImpl} in 
 * creating JMS Resources attached with {@link GridJob}s.
 * 
 * <p><i>Spring Managed</i></p>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobServiceImpl
 */
public class JobServiceJmsSupport {

	private ConnectionFactory connectionFactory;

	/**
	 * Sets the JMS {@link ConnectionFactory} used by this class to 
	 * communicate with Cluster Broker.
	 * 
	 * <p><b>Note :</b>This is a <b>required</b> dependency.</p>
	 * 
	 * <p><i>Spring Injected</i></p>
	 * 
	 * @param connectionFactory {@link ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Creates an returns the TaskQueue for given Job.
	 * 
	 * @param jobId JobId
	 * @return {@link ActiveMQQueue} TaskQueue
	 */
	public ActiveMQQueue createTaskQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getTaskQueueName(jobId));
	}

	/**
	 * Creates an returns the ResultQueue for given Job.
	 * 
	 * @param jobId JobId
	 * @return {@link ActiveMQQueue} ResultQueue
	 */
	public ActiveMQQueue createResultQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getResultQueueName(jobId));
	}

	/**
	 * Creates an returns the Queue to be used for GridJobFuture 
	 * communication of given Job.
	 * 
	 * @param jobId JobId
	 * @return {@link ActiveMQQueue} FutureQueue
	 */
	public ActiveMQQueue createFutureQueue(String jobId) {
		return new ActiveMQQueue(JMSNamingSupport.getFutureQueueName(jobId));
	}
	
	/**
	 * Returns {@link GridJobFutureImpl} for given Job, and also remote-enables
	 * the GridJobFuture using Spring's JMS Remoting facilities.
	 * 
	 * @param jobId JobId
	 * @return {@link GridJobFutureImpl} Future
	 */
	public GridJobFutureImpl createFuture(String jobId) {
		
		GridJobFutureImpl future = new GridJobFutureImpl(jobId);
		future.setState(GridJobState.WAITING);
		
		// Export the Future to client side through Spring JMS Remoting
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(GridJobFuture.class);
		exporter.setService(future);
		exporter.afterPropertiesSet();
		
		// Create Message Listener Container
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestination(createFutureQueue(jobId));
		container.setMessageListener(exporter);
		container.afterPropertiesSet();
		
		return future;
	}
}
