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

package org.nebulaframework.grid.cluster.manager.services.jobs.splitter;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.SplitAggregateGridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.exceptions.SplitException;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

/**
 * Implementation of {@code SplitterService}.
 * <p>
 * {@code SplitterService} splits a given {@code GridJob} to {@code GridTask}s which can be
 * executed on remote {@code GridNode}s. Furthermore, it enqueues the {@code GridTask}s in the
 * {@code TaskQueue} for the {@code GridJob}.
 * <p>
 * <i>Spring Managed</i>
 *  
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see SplitterService
 * @see JMSNamingSupport
 */
public class SplitterServiceImpl implements SplitterService {

	private static Log log = LogFactory.getLog(SplitterServiceImpl.class);
	private JmsTemplate jmsTemplate;
	private InternalClusterJobService jobServiceImpl;
	
	/**
	 * Constructor for {@code SplitterServiceImpl}. This constructs a SplitterServiceImpl
	 * for given JobService Implementation.
	 * 
	 * @param jobServiceImpl {@code ClusterJobServiceImpl) owner
	 */
	public SplitterServiceImpl(InternalClusterJobService jobServiceImpl) {
		super();
		this.jobServiceImpl = jobServiceImpl;
	}

	/**
	 * Sets the JMS {@code ConnectionFactory} for the Cluster. This will be used to
	 * instantiate the {@code JmsTemplate} used by the class.
	 * <p>
	 * <b>Note : </b>This is a <b>required</b> dependency.
	 * <p>
	 * <i>Spring Injected</i>
	 * 
	 * @param connectionFactory JMS {@code ConnectionFactory}
	 */
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method simply delegates to {@code #doStartSplitter(GridJobProfile)} method,
	 * invoked on a separate {@code Thread}.
	 */
	public void startSplitter(final GridJobProfile profile) {
		
		// Start splitting on a separate Thread
		new Thread(new Runnable() {
			public void run() {
				doStartSplitter(profile);
			}
		}).start();

	}

	/**
	 * Internal method which handles splitting of a {@code GridJob}.
	 * 
	 * @param profile {@code GridJobProfile} of the {@code GridJob} to be splitted.
	 */
	protected void doStartSplitter(final GridJobProfile profile) {

		// Update Future state to 'Initializing'
		profile.getFuture().setState(GridJobState.INITIALIZING);

		try {
			SplitAggregateGridJob<?,?> job = (SplitAggregateGridJob<?, ?>) profile.getJob();

			// Split to Tasks
			log.debug("[Splitter] Splitting Tasks");
			
			List<? extends GridTask<?>> taskList = job.split();

			//Enqueue Tasks in TaskQueue
			log.debug("[Splitter] Enqueueing Tasks");
			
			
			for (int i = 0; i < taskList.size(); i++) {
				if (profile.isStopped()) return;
				GridTask<?> task = (GridTask<?>) taskList.get(i);
				profile.addTask(i, task);					// Put copy to TaskMap in Profile
				enqueueTask(profile.getJobId(), i, task); 	// Put to Task Queue
			}	
			
		} catch (Exception e) {
			// Exception during Split
			log.warn("Exception while Splitting Job " + profile.getJobId(), e);
			
			// Update Future State to 'Failed'
			profile.getFuture().setException(new SplitException(e));
			profile.getFuture().setState(GridJobState.FAILED);
			
			// Notify Workers
			jobServiceImpl.notifyJobCancel(profile.getJobId());
			
			return;
		}

		// Update Future State to 'Executing'
		profile.getFuture().setState(GridJobState.EXECUTING);
		
		log.debug("[Splitter] Tasks enqueued. Waiting Execution");
	}

	/**
	 * Enqueues a given Task with in the {@code TaskQueue}.
	 * 
	 * @param jobId String JobId
	 * @param taskId int TaskId (Sequence Number of Task)
	 * @param task {@code GridTask} task
	 */
	private void enqueueTask(final String jobId, final int taskId, GridTask<?> task) {

		// Send GridTask as a JMS Object Message to TaskQueue
		jmsTemplate.convertAndSend(JMSNamingSupport.getTaskQueueName(jobId),
				task, new MessagePostProcessor() {
			
					public Message postProcessMessage(Message message)
							throws JMSException {
						
						// Post Process to include Meta Data
						message.setJMSCorrelationID(jobId); 		// Set Correlation ID to Job Id
						message.setIntProperty("taskId", taskId); 	// Put taskId as a property
						log.debug("Enqueued Task : " + taskId);
						return message;
					}
				});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Simply delegates to {@link #enqueueTask(String, int, GridTask) method.
	 */
	public void reEnqueueTask(final String jobId, final int taskId, GridTask<?> task) {
		enqueueTask(jobId, taskId, task);
	}
	
	

}
