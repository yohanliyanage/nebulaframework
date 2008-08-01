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

package org.nebulaframework.grid.cluster.manager.services.jobs.splitaggregate;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.exceptions.SplitException;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.util.jms.JMSNamingSupport;
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
	 * Constructor for {@code SplitterServiceImpl}.
	 * 
	 */
	public SplitterServiceImpl() {
		super();

	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method simply delegates to {@code #doStartSplitter(GridJobProfile)} method,
	 * invoked on a separate {@code Thread}.
	 */
	public synchronized void startSplitter(final GridJobProfile profile) {
		
		// TODO Debug Code. Remove
		if (profile.getTaskTracker()==null) {
			throw new IllegalStateException("TaskTracker not set");
		}
		
		if (this.jobServiceImpl==null) {
			this.jobServiceImpl = ClusterManager.getInstance().getJobService();
		}
		if (this.jmsTemplate==null) {
			this.jmsTemplate = new JmsTemplate(ClusterManager.getInstance().getConnectionFactory());
		}
		
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
				enqueueTask(profile, i, task); 	// Put to Task Queue
			}	
			
		} catch (SecurityException e) {
			// Security Violation during Split
			log.error("[Splitter] Security Violation while Splitting Job " + profile.getJobId(), e);
			
			// Notify Workers
			jobServiceImpl.notifyJobCancel(profile.getJobId());
			
			// Update Future State to 'Failed'
			profile.getFuture().setException(e);
			profile.getFuture().setState(GridJobState.FAILED);
			
			return;
			
		} catch (Exception e) {
			// Exception during Split
			log.warn("[Splitter] Exception while Splitting Job " + profile.getJobId(), e);
			
			// Notify Workers
			jobServiceImpl.notifyJobCancel(profile.getJobId());
			
			// Update Future State to 'Failed'
			profile.getFuture().setException(new SplitException(e));
			profile.getFuture().setState(GridJobState.FAILED);
			
			return;
		}

		// Start Task Tracker
		profile.getTaskTracker().start();
		
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
	private void enqueueTask(final GridJobProfile profile, final int taskId, GridTask<?> task) {

		final String jobId = profile.getJobId();
		
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
		
		profile.getTaskTracker().taskEnqueued(taskId);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Simply delegates to {@link #enqueueTask(String, int, GridTask) method.
	 */
	public void reEnqueueTask(final String jobId, final int taskId) {
		GridTask<?> task = jobServiceImpl.getProfile(jobId).getTask(taskId);
		if (task!=null) {
			enqueueTask(jobServiceImpl.getProfile(jobId), taskId, task);
		}
		else {
			log.debug("[Splitter] Unable to Re-enqueue Task " + jobId + "|" + taskId);
		}
	}

}
