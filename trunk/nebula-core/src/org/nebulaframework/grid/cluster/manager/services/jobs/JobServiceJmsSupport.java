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

package org.nebulaframework.grid.cluster.manager.services.jobs;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.future.GridJobFutureServerImpl;
import org.nebulaframework.core.job.future.InternalGridJobFuture;
import org.nebulaframework.grid.cluster.manager.support.CleanUpSupport;
import org.nebulaframework.util.jms.JMSNamingSupport;
import org.nebulaframework.util.jms.JMSRemotingSupport;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides support methods which assists {@code ClusterJobServiceImpl} in
 * creating JMS Resources attached with {@code GridJob}s.
 * <p>
 * <i>Spring Managed</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ClusterJobServiceImpl
 */
public class JobServiceJmsSupport {

	private ConnectionFactory connectionFactory;

	/**
	 * Sets the JMS {@code ConnectionFactory} used by this class to communicate
	 * with Cluster Broker.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory
	 *            {@code ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Creates an returns the TaskQueue for given Job.
	 * 
	 * @param jobId
	 *            JobId
	 * @return {@code ActiveMQQueue} TaskQueue
	 */
	public String createTaskQueue(String jobId) {
		String queueName = JMSNamingSupport.getTaskQueueName(jobId);
		new ActiveMQQueue(queueName);
		
		// Clean Up Hook
		CleanUpSupport.removeQueueWhenFinished(jobId, queueName);
		
		return queueName;
	}

	/**
	 * Creates an returns the ResultQueue for given Job.
	 * 
	 * @param jobId
	 *            JobId
	 * @return {@code ActiveMQQueue} ResultQueue
	 */
	public String createResultQueue(String jobId) {
		String queueName = JMSNamingSupport.getResultQueueName(jobId);
		new ActiveMQQueue(queueName);
		
		// Clean Up Hook
		CleanUpSupport.removeQueueWhenFinished(jobId, queueName, null);
		
		return queueName;
	}

	/**
	 * Creates an returns the Queue to be used for GridJobFuture communication
	 * of given Job.
	 * 
	 * @param jobId
	 *            JobId
	 * @return {@code ActiveMQQueue} FutureQueue
	 */
	public String createFutureQueue(String jobId) {
		String queueName = JMSNamingSupport.getFutureQueueName(jobId);
		new ActiveMQQueue(queueName);
		return queueName;
	}

	/**
	 * Returns {@code GridJobFutureImpl} for given Job, and also remote-enables
	 * the GridJobFuture using Spring's JMS Remoting facilities.
	 * 
	 * @param jobId
	 *            JobId
	 * @return {@code GridJobFutureImpl} Future
	 */
	public GridJobFutureServerImpl createFuture(String jobId,
			InternalClusterJobService jobService) {

		GridJobFutureServerImpl future = new GridJobFutureServerImpl(jobId,
				jobService);
		future.setState(GridJobState.WAITING);

		// Export the Future to client side through Spring JMS Remoting
		String queueName = createFutureQueue(jobId);

		// Clean Up Hook
		CleanUpSupport
				.removeQueueWhenFinished(jobId, queueName, JMSRemotingSupport
						.createService(connectionFactory, queueName, future,
										InternalGridJobFuture.class));

		return future;
	}
	
	/**
	 * Creates and returns a {@code ResultCallback} proxy which can be used
	 * to access the {@code ResultCallback} exposed through the given
	 * JMS Queue Name.
	 * 
	 * @param jobId JobId of Result Queue
	 * @param resultCallbackQueue JMS Queue Name
	 * 
	 * @return proxy for {@code ResultCallback} 
	 */
	public  ResultCallback createResultCallbackProxy(String jobId, String resultCallbackQueue) {
		
		// Clean Up Hook
		CleanUpSupport.removeQueueWhenFinished(jobId, resultCallbackQueue);
		
		return JMSRemotingSupport.createProxy(connectionFactory, resultCallbackQueue, ResultCallback.class);
	}
	
	
}
