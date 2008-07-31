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
package org.nebulaframework.grid.cluster.manager.services.jobs.unbounded;

import java.io.Serializable;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.annotations.UnboundedProcessingSettings;
import org.nebulaframework.core.job.exceptions.InvalidResultException;
import org.nebulaframework.core.job.exceptions.SecurityViolationException;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.grid.cluster.manager.services.jobs.ResultCollectionSupport;
import org.nebulaframework.grid.cluster.manager.support.CleanUpSupport;
import org.nebulaframework.util.jms.JMSNamingSupport;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.util.Assert;

/**
 * Manages the execution of {@code UnboundedGridJob}s. For each active 
 * {@code UnboundedGridJob} an instance of this class will be created by 
 * the {@code ClusterManager}.
 * <p>
 * This class is responsible for enqueue tasks for the {@code UnboundedGridJob} 
 * and also to retrieve results for enqueued tasks. Furthermore, it invokes 
 * the {@link ResultCallback}s for intermediate results, if such a callback 
 * is available.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see UnboundedGridJob
 */
public class UnboundedJobProcessor extends ResultCollectionSupport {

	private static Log log = LogFactory.getLog(UnboundedJobProcessor.class);

	public static final int MAX_CONSECUTIVE_NODE_FAILS = 3;
	
	private GridJobProfile profile;		// Profile of GridJob
	private UnboundedGridJob<?> job;	// GridJob

	private boolean canceled = false;
	
	private JmsTemplate jmsTemplate;
	private ConnectionFactory connectionFactory;
	private InternalClusterJobService jobService;
	private DefaultMessageListenerContainer container;
	
	
	/* -- Default Processing Settings -- */
	
	/** Maximum number of tasks in TaskQueue at a given time, without slowing
	 *  task generation
	 */
	private int maxTaskConstant = 100;
	
	/**
	 * Factor (time) by which the task generation is slowed per task which is over 
	 * maxTaskConstant (in milliseconds)
	 */
	private int reductionFactorConstant = 50;	
	
	/**
	 * Indicates whether to stop task generation if a null task is returned
	 * after invoking task() method on UnboundedGridJob
	 */
	private boolean stopOnNullTask = true;
	
	/**
	 * Indicates whether the tasks generated for the current UnboundedGridJob
	 * are mutually exclusive, which can be used to increase performance
	 * and resource utilization
	 */
	private boolean mutuallyExclusiveTasks = false;
	
	
	/**
	 * Constructs a {@code UnboundedJobProcessor} which
	 * will manage the execution of {@code UnboundedGridJob}
	 * represented by the {@code GridJobProfile}.
	 *  
	 * @param profile 
	 * @param connectionFactory
	 * @param jobService
	 */
	public UnboundedJobProcessor(GridJobProfile profile) {
		
		super();
		
		// Validate Arguments
		Assert.notNull(profile);

		if (!(profile.getJob() instanceof UnboundedGridJob<?>)) {
			throw new IllegalArgumentException("GridJob is not a UnboundedGridJob");
		}
		
		this.profile = profile;
		this.job = (UnboundedGridJob<?>) profile.getJob();
		
		// Use Reflection to extract any processing instructions
		extractProcessingSettings(job);
		
		this.connectionFactory = ClusterManager.getInstance().getConnectionFactory();
		this.jobService = ClusterManager.getInstance().getJobService();
	}

	/**
	 * Extracts processing settings for a given {@code UnboundedGridJob}
	 * from annotations of the class, if available.
	 * 
	 * @param job {@code UnboundedGridJob} job
	 */
	private void extractProcessingSettings(UnboundedGridJob<?> job) {
		
		Class<?> clazz = job.getClass();
		
		// Get reference to ProcessingSettings annotation
		UnboundedProcessingSettings settings = clazz.getAnnotation(UnboundedProcessingSettings.class);
		
		// If not annotated, return
		if (settings==null) return;
		
		// If available, retrieve settings
		this.maxTaskConstant = settings.maxTasksInQueue();
		this.reductionFactorConstant = settings.reductionFactor();
		this.stopOnNullTask = settings.stopOnNullTask();
		this.mutuallyExclusiveTasks = settings.mutuallyExclusiveTasks();
		
		log.debug("[UnboundedJobProcessor] Using Custom Processing Settings from Annotation");
		
	}

	/**
	 * Starts this {@code UnboundedJobProcessor} instance by 
	 * initializing JMS resources and starting task generation.
	 */
	public void start() {
		initialize();
		generateTasks();
	}

	/**
	 * Initializes this {@code UnboundedJobProcessor}'s JMS
	 * resources.
	 */
	private void initialize() {
		initializeResultListener();
		initializeTaskWritier();
	}

	/**
	 * Register's {@link #onResult(GridTaskResult)} method as the listener
	 * method for ResultQueue.
	 */
	private void initializeResultListener() {

		MessageListenerAdapter adapter = new MessageListenerAdapter(this);
		adapter.setDefaultListenerMethod("onResult");

		container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestinationName(JMSNamingSupport
				.getResultQueueName(profile.getJobId()));
		container.setMessageListener(adapter);
		container.afterPropertiesSet();
		
		// Clean Up Hook
		CleanUpSupport.shutdownContainerWhenFinished(profile.getJobId(), container);

	}

	/**
	 * Creates the {@code JmsTemplate} used to write
	 * tasks to TaskQueue.
	 */
	private void initializeTaskWritier() {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
	}
	
	/**
	 * Starts generation of {@code GridTask}s by repetitively
	 * invoking the {@link UnboundedGridJob#task()} method.
	 */
	private void generateTasks() {
		log.info("[UnboundedJobProcessor] Started Generating Tasks");
		
		// Start on a new thread
		new Thread(new Runnable() {

			public void run() {

				// Start Tracker
				profile.getTaskTracker().start();
				
				// Update State
				profile.getFuture().setState(GridJobState.EXECUTING);
				
				int taskId = 0;
				while (true) {

					// If Job is Canceled, Stop
					if (isCanceled()) {
						log.debug("[UnboundedJobProcessor] Task was cancelled. Stopping Task Generation");
						break;
					}
					GridTask<?> task = null;
					
					try {
						// Get next task to be enqueued
						task = job.task();
					} catch (Exception e) {
						log.error("[UnboundedJobProcessor] Exception while invoking task()",e);
						log.error("[UnboundedJobProcessor] Stopping Task Generation");
						break;
					}

					// If Task was null
					if (task == null) {
						if (stopOnNullTask) {
							log.info("[UnboundedJobProcessor] Task was Null. Stopping Task Generation");
							break;
						}
						else {
							log.warn("[UnboundedJobProcessor] Task was Null. Ignoring");
							continue;
						}
					}
					
					// Enqueue Task
					enqueueTask(profile.getJobId(), ++taskId, task);
					
					if (!mutuallyExclusiveTasks) {
						// If tasks are not mutually exclusive keep track of real task
						profile.addTask(taskId, task);
					}
					else {
						// If tasks are mutually exclusive, save memory by storing a null
						profile.addTask(taskId, null);
					}
					
					// Slow down for some duration if more than 'maxTaskConstant' tasks are 
					// there to ensure TaskQueue won't overload
					try {
						if (profile.getTaskCount() > maxTaskConstant) {
							Thread.sleep(profile.getTaskCount() * reductionFactorConstant);
						}
					} catch (InterruptedException e) {
						log.error(e);
					}
				}

				// Stop Job (Exception / Task Null)
				stopJob();
			}

		}).start();
	}

	/**
	 * Enqueues a given Task with in the {@code TaskQueue}.
	 * 
	 * @param jobId
	 *            String JobId
	 * @param taskId
	 *            int TaskId (Sequence Number of Task)
	 * @param task
	 *            {@code GridTask} task
	 */
	private void enqueueTask(final String jobId, final int taskId,
			GridTask<?> task) {

		String queueName = JMSNamingSupport.getTaskQueueName(jobId);
		
		// Post Process to include Meta Data
		MessagePostProcessor postProcessor = new MessagePostProcessor() {

			public Message postProcessMessage(
					Message message)
					throws JMSException {

				// Set Correlation ID to Job Id
				message.setJMSCorrelationID(jobId);
				
				// Put taskId as a property
				message.setIntProperty("taskId",taskId); 
				
				log.debug("Enqueued Task : "+taskId);
				
				return message;
			}
		};
		
		// Send GridTask as a JMS Object Message to TaskQueue
		jmsTemplate.convertAndSend(queueName, task, postProcessor);
		
		// Update Task Tracker
		profile.getTaskTracker().taskEnqueued(taskId);
									
	}

	/**
	 * Re-enqueues the {@code GridTask} denoted by {@code taskId}.
	 * 
	 * @param taskId {@code GridTask} Id
	 */
	public void reEnqueueTask(final int taskId) {
		
		log.debug("Re-enqueueing Task : " + taskId);
		
		if (!mutuallyExclusiveTasks) {
			enqueueTask(profile.getJobId(), taskId, job.task());
		}
		else {
			GridTask<?> task = profile.getTask(taskId);
			
			// If Task Not in Profile (Completed)
			if (task==null) {
				log.debug("[Processor] Unable to re-enqueue, task possibly complete" + 
				          profile.getJobId() + "|" + taskId);
				return;
			}
			
			enqueueTask(profile.getJobId(), taskId, task);
		}
		
	}

	/**
	 * Stops execution of the {@code UnboundedGridJob},
	 * and notifies the workers.
	 */
	protected void stopJob() {
		log.info("[UnboundedJobProcessor] Stopping Job Execution");
		// Notify Workers
		jobService.notifyJobEnd(profile.getJobId());

		// Update Future and return Result (null for unbounded)
		profile.getFuture().setResult(null);
		profile.getFuture().setState(GridJobState.COMPLETE);

		// Destroy this instance
		destroy();
	}

	/**
	 * Invoked by the JMS Message Listener Container when a result
	 * is available in the ResultQueue.
	 * 
	 * @param taskResult Result of Task
	 */
	public void onResult(final GridTaskResult taskResult) {
		
		// Result is Valid / Complete
		if (taskResult.isComplete()) { 

			log.debug("[UnboundedJobProcessor] Received Result : Task "
					+ taskResult.getTaskId());

			// Update Tracker
			profile.getTaskTracker().resultReceived(taskResult.getTaskId(), 
			                                        taskResult.getExecutionTime());
			
			// Post Process Result
			Serializable result;
			try {
				
				result = job.processResult(taskResult.getResult());
				
			} catch (InvalidResultException e) {
				
				// Result was invalid, re-enqueue
				log.debug("[UnboundedJobProcessor] Invalid Result Exception");
				
				// Update Profile
				profile.failedTaskReceived();
				
				// Add Failure Trace
				addFailureTrace(taskResult.getWorkerId());
				
				reEnqueueTask(taskResult.getTaskId());
				return;
				
			} catch (Exception e) {
				log.error("[UnboundedJobProcessor] Exception while Processing Result",e);
				log.error("[UnboundedJobProcessor] Stopping Job Execution");
				stopJob();
				return;
			}

			// Fire intermediate results callback
			profile.fireCallback(result);
			
			// Clear Failure Traces
			clearFailureTrace(taskResult.getWorkerId());
			
			// Task completed, remove it from TaskMap
			// Add Dummy Place-holder for Result List
			profile.addResultAndRemoveTask(taskResult.getTaskId(), null);

		} else { // Result Not Valid / Exception
			
			// Check for Security Violations (Fails Job)
			if (taskResult.getException() instanceof SecurityException) {
				
				log.error("[UnboundedJobProcessor] Security Violation detected. Terminating GridJob" + 
				          profile.getJobId());
				
				// Fail the Job
				profile.getFuture().fail(new SecurityViolationException("Security Violation Detected", 
				                                                        taskResult.getException()));
				// Stop Result Collector
				destroy();
				
				return;
			}
			
			// Update Profile
			profile.failedTaskReceived();
			
			// Add Failure Trace
			addFailureTrace(taskResult.getWorkerId());
			
			log.warn("[UnboundedJobProcessor] Result Failed ["
					+ taskResult.getTaskId() + "], ReEnqueueing - "
					+ taskResult.getException());

			// Request re-enqueue of Task
			reEnqueueTask(taskResult.getTaskId());
		}
	}

	/**
	 * Shutdowns the JMS Message Listener to
	 * avoid processing any more results.
	 */
	protected void destroy() {
		if (container != null)
			container.shutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean cancel() {
		
		// Mark that this Job is canceled
		this.setCanceled(true);
		
		// Stop listener
		try {
			destroy();
		} catch (Exception e) {
			log.warn("[UnboundedJobProcessor] Unable to shutdown container",e);
			return false;
		}
		
		return true;
	}

	/**
	 * Indicates whether this {@code UnboundedJobProcessor}
	 * is canceled.
	 * 
	 * @return if canceled, {@code true}, otherwise {@code false}
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Sets the canceled state to the given {@code boolean} value
	 * 
	 * @param canceled state
	 */
	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
