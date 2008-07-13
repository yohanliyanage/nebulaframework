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

package org.nebulaframework.core.grid.cluster.node.services.job.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResultImpl;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * {@code TaskExecutor} executes {@code GridTask}s for a given {@code GridJob}.
 * <p>
 * Spring JMS constructs are used to listen to available {@code GridTask}s in
 * the {@code TaskQueue}, then does the execution of the {@code GridTask} by
 * calling {@link GridTask#execute()} method, and finally writes the result of
 * execution to the {@code ResultQueue}, wrapped in a {@code GridTaskResult}.
 * <p>
 * Furthermore, special Class Loading mechanisms are also utilized, according to
 * the type of {@code GridJob}, as follows:
 * <p>
 * <table border='1'>
 * <tr>
 * <td><b>Type</b></td>
 * <td><b>ClassLoader</b></td>
 * </tr>
 * <tr>
 * <td>Non-archived {@code GridJob}</td>
 * <td>{@link GridNodeClassLoader}</td>
 * </tr>
 * <tr>
 * <td>Archived {@code GridJob}</td>
 * <td>{@link GridArchiveClassLoader}</td>
 * </tr>
 * </table>
 * <p>
 * The relevant {@code ClassLoader} is attached to the thread of execution for
 * the {@code GridTask} as the {@code contextClassLoader} so that it will be
 * utilized for necessary Class Loading during the execution of Task.
 * <p>
 * Also, {@code TaskExecutor} keeps a reference of each active
 * {@code TaskExecutor} instance, against the {@code JobId} so that the
 * allocated resources could be released at the end of Job Execution.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridTask
 * @see GridJob
 * @see GridArchive
 * @see GridNodeClassLoader
 * @see GridArchiveClassLoader
 */
//public class TaskExecutor {
//
//	private static Log log = LogFactory.getLog(TaskExecutor.class);
//
//	// Active TaskExecutors, against JobId
//	private static Map<String, TaskExecutor> executors = new HashMap<String, TaskExecutor>();
//
//	private GridNode node; // Owner Node
//	private ConnectionFactory connectionFactory;
//	private String jobId; // JobID of Instance
//	private JmsTemplate resultTemplate; // Sending Results
////	private TaskMessageListener listener; // For receiving Tasks
//	private AtomicInteger taskCount = new AtomicInteger(); // # of Tasks Executed
//	private JmsTemplate taskTemplate;
//	private boolean stopped;
//	
//	
//	/**
//	 * Constructs a TaskExecutor for given {@code JobId}, Owner
//	 * {@code GridNode} and JMS {@code ConnectionFactory}.
//	 * <p>
//	 * Note that the constructor is <b>private</b>. Thus, no external
//	 * instantiation is allowed.
//	 * 
//	 * @param jobId
//	 *            JobId of {@code GridJob}
//	 * @param node
//	 *            {@code GridNode} owner
//	 * @param connectionFactory
//	 *            JMS {@code ConnectionFactory}
//	 */
//	private TaskExecutor(String jobId, GridNode node,
//			ConnectionFactory connectionFactory) {
//
//		super();
//
//		this.node = node;
//		this.connectionFactory = connectionFactory;
//		this.jobId = jobId;
//
//		log.debug("[TaskExecutor] Created for Job {" + jobId + "}");
//	}
//
//	/**
//	 * Creates and starts a {@code TaskExecutor} instance for the given
//	 * {@code GridJob}, denoted by {@code JobId}. Each {@code TaskExecutor} is
//	 * started on a separate {@code Thread}.
//	 * <p>
//	 * Furthermore, it also configures and attaches the proper custom
//	 * {@code ClassLoader} to the thread context.
//	 * 
//	 * @param jobId
//	 *            JobId of {@code GridJob}
//	 * @param node
//	 *            Owner {@code GridNode}
//	 * @param connectionFactory
//	 *            JMS {@code ConnectionFactory}
//	 * @param classLoadingService
//	 *            Proxy for {@code ClusterManager}s {@code ClassLoadingService}
//	 * @param archive
//	 *            {@code GridArchive}, if exists, or {@code null} otherwise.
//	 */
//	public static void startForJob(final String jobId, final GridNode node,
//			final ConnectionFactory connectionFactory,
//			final ClassLoadingService classLoadingService,
//			final GridArchive archive) {
//
//		new Thread(new Runnable() {
//
//			public void run() {
//
//				// Configure Thread Context Class Loader to use
//				// GridNodeClassLoader,
//				ClassLoader classLoader = new GridNodeClassLoader(jobId,
//						classLoadingService, Thread.currentThread()
//								.getContextClassLoader());
//
//				if (archive != null) {
//					// If its an archived Job, configure to use
//					// GridArchvieClassLoader
//					// chained to GridNodeClassLoader
//					ClassLoader archiveLoader = new GridArchiveClassLoader(
//							archive, classLoader);
//					classLoader = archiveLoader;
//				}
//
//				// Set ClassLoader as Thread Context Class Loader
//				Thread.currentThread().setContextClassLoader(classLoader);
//
//				// Create Executor
//				TaskExecutor executor = new TaskExecutor(jobId, node,
//						connectionFactory);
//
//				// Put to active executors Map
//				synchronized (TaskExecutor.class) {
//					TaskExecutor.executors.put(jobId, executor);
//				}
//
//				// Start Executor
//				executor.start();
//			}
//
//		}).start();
//	}
//
//	/**
//	 * Stops the {@code TaskExecutor} for the given {@code GridJob}.
//	 * 
//	 * @param jobId
//	 *            {@code GridJob} Identifier
//	 */
//	public static void stopForJob(final String jobId) {
//		try {
//			synchronized (TaskExecutor.class) {
//				// Invoke stop() instance method on proper TaskExecutor
//				TaskExecutor.executors.get(jobId).stop();
//			}
//		} catch (NullPointerException e) {
//			// No TaskExecutor for given Job
//			throw new IllegalArgumentException(
//					"No TaskExecutor found for JobId " + jobId);
//		}
//	}
//
//	/**
//	 * Starts execution of this {@code TaskExecutor} instance after initializing
//	 * required resources.
//	 * <p>
//	 * Once initialized, the instance will start Task Execution.
//	 */
//	protected void start() {
//
//		log.debug("[TaskExecutor] Started Job {" + jobId + "}");
//		// Initialize the JMS Resources
//		initialize();
//
//		
//	}
//
//	/**
//	 * Stops execution of this {@code TaskExecutor} instance. The
//	 * {@code TaskMessageListener} will be stopped, thus stopping listening to
//	 * new Tasks, and the {@code TaskExecutor} instance will be removed from the
//	 * active {@code TaskExecutor}s Map.
//	 */
//	protected void stop() {
//
//		// Stop TaskMessageListener 
//		//listener.stop();
//		stopped = true;
//		
//		// Remove from Active TaskExecutors
//		synchronized (TaskExecutor.class) {
//			TaskExecutor.executors.remove(this.jobId);
//		}
//
//		// Log
//		log.info("[TaskExecutor] Stopped Job {" + jobId + "}");
//		log.info("[TaskExecutor] Stats : Executed " + taskCount.get()
//				+ " tasks");
//	}
//
//	/**
//	 * Initializes the ResultQueue writing facilities ({@code JmsTemplate})
//	 * and TaskQueueListener {@code MessageListener} facilities.
//	 */
//	private void initialize() {
//
//		// Create Local Listeners for ResultQueue and TaskQueue
//		// (Order is important)
//		initializeResultQueueWriter(); // First
//		initializeTaskQueueListener(); // Second
//	}
//
//	/**
//	 * Initializes the {@code TaskQueueListener} for this {@code TaskExecutor}.
//	 * Creates a {@link TaskMessageListener} and attaches it to a
//	 * {@code DefaultMessageListenerContainer}, which in turn listens to the
//	 * {@code TaskQueue}.
//	 * <p>
//	 * <b>Precondition :</b>Requires that
//	 * {@link #initializeResultQueueWriter()} is invoked before
//	 * <p>
//	 * Once invoked, the Task Execution will start.
//	 * 
//	 * @throws IllegalStateException
//	 *             If {@link #initializeResultQueueWriter()} is not invoked
//	 *             before invoking this method (precondition failure).
//	 */
//	private void initializeTaskQueueListener() throws IllegalStateException {
//
//		// Check if initializeResultQueueWriter() has been invoked
//		if (this.resultTemplate == null) {
//			throw new IllegalStateException("ResultQueueWriter not Initialized");
//		}
//
//		this.taskTemplate = new JmsTemplate(connectionFactory);
//		taskTemplate.setDefaultDestinationName(JMSNamingSupport.getTaskQueueName(jobId));
//		taskTemplate.setReceiveTimeout(2000);
//		
//		//listener = new TaskMessageListener(connectionFactory);
//		//listener.start();
//		receiveTasks(taskTemplate);
//
//	}
//
//	/**
//	 * Initializes the ResultWriter ({@code JMSTemplate}) for this
//	 * {@code TaskExecutor}. Configures the {@code JmsTemplate} to send
//	 * messages to the {@code ResultQueue} as its default destination.
//	 */
//	private void initializeResultQueueWriter() {
//		resultTemplate = new JmsTemplate(connectionFactory);
//		resultTemplate.setDefaultDestinationName(JMSNamingSupport
//				.getResultQueueName(jobId));
//	}
//
//	/**
//	 * Executes the given {@code GridTask} and returns the result to
//	 * {@code ResultQueue}, wrapped in a {@code GridTaskResult}. This method
//	 * is invoked by the {@link TaskMessageListener} when a {@code GridTask}
//	 * arrives.
//	 * 
//	 * @param taskId
//	 *            Task Id of {@code GridTask}
//	 * @param task
//	 *            {@code GridTask} to be executed
//	 */
//	protected void onTask(int taskId, GridTask<? extends Serializable> task) {
//
//		log.debug("[TaskExecutor] Runing Task " + taskId);
//
//		// Update Statistics
//		taskCount.incrementAndGet();
//
//		// Create Result Wrapper
//		GridTaskResultImpl taskResult = new GridTaskResultImpl(jobId, taskId,
//				node.getId());
//
//		try {
//			// Execute Task
//			Serializable result = task.execute();
//
//			// Put result into Result Wrapper
//			taskResult.setResult(result);
//
//		} catch (Exception e) {
//
//			log.warn("[TaskExecutor] Exception while executing GridTask", e);
//
//			// Exception, send exception details instead of result
//			taskResult.setException(e);
//
//		} finally {
//
//			// Send the result to ResultQueue
//			sendResult(taskResult);
//		}
//	}
//
//	/**
//	 * Sends the given {@code GridTaskResult} to {@code ResultQueue}.
//	 * 
//	 * @param result
//	 *            Result of {@code GridTask}
//	 */
//	private void sendResult(GridTaskResultImpl result) {
//		resultTemplate.convertAndSend(result);
//	}
//
//	private void receiveTasks(JmsTemplate template) {
//		
//		while (true) {
//			Message message = template.receive();
//			
//			while (message == null) {
//				log.debug("Message Null");
//				if (stopped) {
//					log.debug("Stopped");
//					return;
//				}
//				else {
//					message = template.receive();
//				}
//			}
//			
//			if (!(message instanceof ObjectMessage)) {
//				// Non Object Messages cannot be in TaskQueue
//				log.warn("[TaskExecutor-Listener] Non-Object Message in TaskQueue");
//				continue;
//			}
//	
//			try {
//				int taskId = message.getIntProperty("taskId");
//				GridTask<?> task = (GridTask<?>) ((ObjectMessage) message)
//						.getObject();
//	
//				// Do Execution
//				TaskExecutor.this.onTask(taskId, task);
//			} catch (JMSException e) {
//				log.warn("[TaskExecutor-Listener] Exception while reading TaskMessage",
//								e);
//			}
//		}
//	}
//	
////	/**
////	 * Internal class which listens to {@code TaskMessage} using Spring
////	 * JMSTemplate. Uses synchronous reception to ensure that no further Task is
////	 * fetched until current Task is done, enforcing dynamic load balancing.
////	 * <p>
////	 * Provides mechanisms to extract the {@code taskId} from the incoming
////	 * Message. Once meta-data have been extracted, the {@code onMessage} method
////	 * will then invoke {@code onTask} method of the respective
////	 * {@code TaskExecutor} for the {@code GridJob}, thus executing the Task.
////	 * 
////	 * @author Yohan Liyanage
////	 * @version 1.0
////	 */
////	protected class TaskMessageListener implements MessageListener {
////
////		private JmsTemplate taskTemplate;
////		private boolean stopped;
////
////		/**
////		 * Constructs a {@code TaskMessageListener} which will listen to
////		 * incoming task messages.
////		 * 
////		 * @param connectionFactory
////		 *            JMS {@code ConnectionFactory}
////		 */
////		public TaskMessageListener(ConnectionFactory connectionFactory) {
////			taskTemplate = new JmsTemplate(connectionFactory);
////			taskTemplate.setDefaultDestinationName(JMSNamingSupport
////					.getTaskQueueName(jobId));
////			taskTemplate.setReceiveTimeout(2000);
////
////		}
////
////		/**
////		 * Starts listening to available Tasks in TaskQueue, and will delegate
////		 * to the TaskExecutor to execute each task as received.
////		 * <p>
////		 * The message reception occurs in a separate Thread.
////		 * 
////		 */
////		public void start() {
////			new Thread(new Runnable() {
////				public void run() {
////
////					// Infinite Loop : Broke when Task Stops
////					daLoop:
////					while (true) {
////						
////						try {
////							
////							Message message = null;
////							try {
////								// Break loop if Task stopped
////								if (isStopped()) {
////									break;
////								}
////								
////								// Take Task from Queue
////								message = taskTemplate.receive();
////								
////							} catch (JmsException ex) {
////								// Log
////								log.error("[TaskExecutor-Listener] Stopping due to JMSException : ", ex);
////								
////								// Stop TaskExecutor and Break off Loop
////								TaskExecutor.this.stop();
////								break;
////							}
////
////							// If timeout, check Job Stopped and try again
////							if (message == null) {
////								if (!isStopped()) {
////									log.debug("Message Null While Not Stopped");
////								}
////								continue daLoop;
////							}
////
////							// Process Message
////							onMessage(message);
////
////						} catch (Exception ex) {
////							log.warn("[TaskExecutor-Listener] Exception while listening to TaskQueue",
////											ex);
////						}
////					}
////					log.debug("[TaskExecutor-Listener] Stopped for job {"+ jobId + "}");
////				}
////			}).start();
////		}
////
////		public boolean isStopped() {
////			return stopped;
////		}
////
////		private void stop() {
////			this.stopped = true;
////		}
////
////		/**
////		 * Invoked when a JMS Message arrives.
////		 */
////		public void onMessage(Message message) {
////
////			if (!(message instanceof ObjectMessage)) {
////				// Non Object Messages cannot be in TaskQueue
////				log.warn("[TaskExecutor-Listener] Non-Object Message in TaskQueue");
////				return;
////			}
////
////			try {
////				int taskId = message.getIntProperty("taskId");
////				GridTask<?> task = (GridTask<?>) ((ObjectMessage) message)
////						.getObject();
////
////				// Do Execution
////				TaskExecutor.this.onTask(taskId, task);
////			} catch (JMSException e) {
////				log.warn("[TaskExecutor-Listener] Exception while reading TaskMessage",
////								e);
////			}
////		}
////	}
//
//}
public class TaskExecutor {

    private static Log log = LogFactory.getLog(TaskExecutor.class);

    // Active TaskExecutors, against JobId
    private static Map<String, TaskExecutor> executors = new HashMap<String, TaskExecutor>();
    
    private GridNode node;                                                          // Owner Node
    private ConnectionFactory connectionFactory;    
    private String jobId;                                                           // JobID of Instance
    private JmsTemplate jmsTemplate;                                        // Sending Results
    private DefaultMessageListenerContainer container;      // Receiving Tasks
    
    private int taskCount = 0;                                                      // # of Tasks Executed
    
    /**
     * Constructs a TaskExecutor for given {@code JobId},
     * Owner {@code GridNode} and JMS {@code ConnectionFactory}.
     * <p>
     * Note that the constructor is <b>private</b>. Thus, no external
     * instantiation is allowed.
     *
     * @param jobId JobId of {@code GridJob}
     * @param node {@code GridNode} owner
     * @param connectionFactory JMS {@code ConnectionFactory}
     */
    private TaskExecutor(String jobId, GridNode node, ConnectionFactory connectionFactory) {
    
            super();
            
            this.node = node;
            this.connectionFactory = connectionFactory;
            this.jobId = jobId;
            
            log.debug("[TaskExecutor] Created for Job {" + jobId + "}");
    }

    /**
     * Creates and starts a {@code TaskExecutor} instance for the given {@code GridJob},
     * denoted by {@code JobId}. Each {@code TaskExecutor} is started on a separate
     * {@code Thread}.
     * <p>
     * Furthermore, it also configures and attaches the proper custom {@code ClassLoader}
     * to the thread context.
     *  
     * @param jobId JobId of {@code GridJob}
     * @param node Owner {@code GridNode}
     * @param connectionFactory JMS {@code ConnectionFactory}
     * @param classLoadingService Proxy for {@code ClusterManager}s {@code ClassLoadingService}
     * @param archive {@code GridArchive}, if exists, or {@code null} otherwise.
     */
    public static void startForJob(final String jobId, final GridNode node,
                    final ConnectionFactory connectionFactory, final ClassLoadingService classLoadingService, final GridArchive archive) {
            
            new Thread(new Runnable() {

                    public void run() {
                            
                            // Configure Thread Context Class Loader to use GridNodeClassLoader,
                            ClassLoader classLoader = new GridNodeClassLoader(jobId, classLoadingService, Thread.currentThread().getContextClassLoader());
                            
                            if (archive != null) {
                                    // If its an archived Job, configure to use GridArchvieClassLoader
                                    // chained to GridNodeClassLoader
                                    ClassLoader archiveLoader = new GridArchiveClassLoader(archive, classLoader);
                                    classLoader = archiveLoader;
                            }
                            
                            // Set ClassLoader as Thread Context Class Loader
                            Thread.currentThread().setContextClassLoader(classLoader);
                            
                            // Create Executor
                            TaskExecutor executor = new TaskExecutor(jobId, node, connectionFactory);
                            
                            // Put to active executors Map
                            synchronized (TaskExecutor.class) {
                                    TaskExecutor.executors.put(jobId, executor);
                            }
                            
                            // Start Executor
                            executor.start();
                    }

            }).start();
    }      
    
    /**
     * Stops the {@code TaskExecutor} for the given {@code GridJob}.
     *
     * @param jobId {@code GridJob} Identifier
     */
    public static void stopForJob(final String jobId) {
            try {
                    synchronized (TaskExecutor.class) {
                            //Invoke stop() instance method on proper TaskExecutor
                            TaskExecutor.executors.get(jobId).stop();
                    }
            } catch (NullPointerException e) {
                    // No TaskExecutor for given Job
                    throw new IllegalArgumentException("No TaskExecutor found for JobId " + jobId);
            }
    }
    
    /**
     * Starts execution of this {@code TaskExecutor} instance. Initializes
     * the ResultQueue writing facilities ({@code JmsTemplate}) and
     * TaskQueueListener {@code MessageListener} facilities respectively.
     * <p>
     * Once initialized, the instance will start Task Execution.
     */
    protected void start() {

            // Create Local Listeners for ResultQueue and TaskQueue
            // (Order is important)
            initializeResultQueueWriter();  // First
            initializeTaskQueueListener();  // Second

            log.debug("[TaskExecutor] Started Job {" + jobId + "}");
    }

    /**
     * Stops execution of this {@code TaskExecutor} instance. The
     * {@code MessageListenerContainer} will be shutdown, thus stopping
     * listening to new Tasks, and the {@code TaskExecutor} instance will be
     * removed from the active {@code TaskExecutor}s Map.
     */
    protected void stop() {
            // Shutdown Container
            container.shutdown();
            
            // Remove from Active TaskExecutors
            synchronized (TaskExecutor.class) {
                    TaskExecutor.executors.remove(this.jobId);
            }
            
            // Log
            log.debug("[TaskExecutor] Stopped Job {" + jobId + "}");
            log.debug("[TaskExecutor] Stats : Executed " + taskCount + " tasks");
    }
    
    /**
     * Initializes the {@code TaskQueueListener} for this {@code TaskExecutor}.
     * Creates a {@link TaskMessageListener} and attaches it to a
     * {@code DefaultMessageListenerContainer}, which in turn listens to the
     * {@code TaskQueue}.
     * <p>
     * <b>Precondition :</b>Requires that {@link #initializeResultQueueWriter()}
     * is invoked before
     * <p>
     * Once invoked, the Task Execution will start.
     *
     * @throws IllegalStateException If {@link #initializeResultQueueWriter()} is not
     * invoked before invoking this method (precondition failure).
     */
    private void initializeTaskQueueListener() throws IllegalStateException {
            
            // Check if initializeResultQueueWriter() has been invoked
            if (this.jmsTemplate==null) {
                    throw new IllegalStateException("ResultQueueWriter not Initialized");
            }
            
            ActiveMQConnectionFactory cf = (ActiveMQConnectionFactory) connectionFactory;
            
            ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
            policy.setQueuePrefetch(1);
            cf.setPrefetchPolicy(policy);
            
            // Create Listener and Container
            container = new DefaultMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setDestinationName(JMSNamingSupport.getTaskQueueName(jobId));
            container.setMessageListener(new TaskMessageListener());
            
            // Start Container
            container.afterPropertiesSet();
    }


    /**
     * Initializes the ResultWriter ({@code JMSTemplate}) for this
     * {@code TaskExecutor}. Configures the {@code JmsTemplate} to send messages
     * to the {@code ResultQueue} as its default destination.
     */
    private void initializeResultQueueWriter() {
            jmsTemplate = new JmsTemplate(connectionFactory);
            jmsTemplate.setDefaultDestinationName(JMSNamingSupport
                            .getResultQueueName(jobId));
    }

    /**
     * Executes the given {@code GridTask} and returns the result to {@code ResultQueue},
     * wrapped in a {@code GridTaskResult}. This method is invoked by the
     * {@link TaskMessageListener} when a {@code GridTask} arrives.
     *
     * @param taskId Task Id of {@code GridTask}
     * @param task {@code GridTask} to be executed
     */
    protected void onTask(int taskId, GridTask<? extends Serializable> task) {
            
            log.debug("[TaskExecutor] Start Runing Task " + taskId);
            
            // Update Stats
            taskCount++;    
            
            // Create Result Wrapper
            GridTaskResultImpl taskResult =
                    new GridTaskResultImpl(jobId, taskId, node.getId());

            try {
                    // Execute Task
                    Serializable result = task.execute();
                    
                    // Put result into Result Wrapper
                    taskResult.setResult(result);
                    
            } catch (Exception e) {
                    
                    log.warn("[TaskExecutor] Exception while executing GridTask", e);

                    // Exception, send exception details instead of result
                    taskResult.setException(e);
                    
            } finally {
                    
            		log.debug("[TaskExecutor] Sending Result for Task " + taskId);
                    // Send the result to ResultQueue
                    sendResult(taskResult);
            }
    }

    /**
     * Sends the given {@code GridTaskResult} to {@code ResultQueue}.
     *
     * @param result Result of {@code GridTask}
     */
    private void sendResult(GridTaskResultImpl result) {
            jmsTemplate.convertAndSend(result);
    }

    /**
     * Internal class which implements {@code javax.jms.MessageListener}
     * interface to act as the message listener for Task Messages. Provides
     * mechanisms to extract the {@code taskId} from the incoming Message.
     * <p>
     * Once meta-data have been extracted, the {@code onMessage} method will
     * then invoke {@code onTask} method of the respective {@code TaskExecutor}
     * for the {@code GridJob}, thus executing the Task.
     *
     * @author Yohan Liyanage
     * @version 1.0
     */
    private class TaskMessageListener implements MessageListener {


            public void onMessage(Message message) {
                    try {
                            int taskId = message.getIntProperty("taskId");
                            GridTask<?> task = (GridTask<?>) ((ObjectMessage) message)
                                            .getObject();

                            // Do Execution
                            TaskExecutor.this.onTask(taskId, task);
                    } catch (JMSException e) {
                            log.warn("[TaskExecutor-Listener] Exception while reading TaskMessage", e);
                    }
            }
    }

}
