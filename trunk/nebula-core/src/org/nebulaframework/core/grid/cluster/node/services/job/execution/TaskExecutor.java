package org.nebulaframework.core.grid.cluster.node.services.job.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSNamingSupport;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResultImpl;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.deployment.classloading.GridNodeClassLoader;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class TaskExecutor {

	private static Log log = LogFactory.getLog(TaskExecutor.class);

	private static Map<String, TaskExecutor> executors = new HashMap<String, TaskExecutor>();
	
	private GridNode node;
	private ConnectionFactory connectionFactory;
	private String jobId;
	private JmsTemplate jmsTemplate;
	private DefaultMessageListenerContainer container;
	
	private int taskCount = 0;		// For statistical analysis
	
	private TaskExecutor(String jobId, GridNode node,
			ConnectionFactory connectionFactory) {
		super();
		this.node = node;
		this.connectionFactory = connectionFactory;
		this.jobId = jobId;
		log.debug("Task Executor Created for Job " + jobId);
	}


	public static void startForJob(final String jobId, final GridNode node,
			final ConnectionFactory connectionFactory, final ClassLoadingService classLoadingService, final GridArchive archive) {
		new Thread(new Runnable() {

			public void run() {
				
				// Configure Thread Context Class Loader to use GridNodeClassLoader,
				ClassLoader classLoader = new GridNodeClassLoader(jobId,classLoadingService, Thread.currentThread().getContextClassLoader());
				
				if (archive!=null) {
					// If its an archived Job, configure to use GridArchvieClassLoader
					// chained to GridNodeClassLoader
					ClassLoader archiveLoader = new GridArchiveClassLoader(archive, classLoader);
					classLoader = archiveLoader;
				}
				
				Thread.currentThread().setContextClassLoader(classLoader);
				
				// Create Executor
				TaskExecutor executor = new TaskExecutor(jobId, node, connectionFactory);
				synchronized (TaskExecutor.class) {
					TaskExecutor.executors.put(jobId, executor);
				}
				
				executor.start();
			}

		}).start();
	}	
	
	public static void stopForJob(final String jobId) {
		try {
			synchronized (TaskExecutor.class) {
				TaskExecutor.executors.get(jobId).stop();
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("No TaskExecutor found for JobId " + jobId);
		}
	}
	
	protected void start() {

		// Create Local Listeners for TaskQueue / ResultQueue
		initializeResultQueueWriter();
		initializeTaskQueueListener();

		log.debug("TaskExecutor Started for Job " + jobId);
	}

	protected void stop() {
		container.shutdown();
		synchronized (TaskExecutor.class) {
			TaskExecutor.executors.remove(this.jobId);
		}
		log.debug("TaskExecutor stopped for Job " + jobId);
		log.debug("TaskExecutor Stats : Executed " + taskCount + " tasks");
	}
	
	private void initializeTaskQueueListener() {
		container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestinationName(JMSNamingSupport.getTaskQueueName(jobId));
		container.setMessageListener(new TaskMessageListener());
		container.afterPropertiesSet();
	}

	private void initializeResultQueueWriter() {
		jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setDefaultDestinationName(JMSNamingSupport
				.getResultQueueName(jobId));
	}

	/**
	 * Executes the given Task and returns the result to ResultQueue.
	 * 
	 * @param taskId
	 *            Task Id
	 * @param task
	 *            GridTask
	 */
	protected void onTask(int taskId, GridTask<? extends Serializable> task) {
		log.debug("Starting execution of task " + taskId);
		
		taskCount++;	// Stats
		
		GridTaskResultImpl taskResult = new GridTaskResultImpl(jobId, taskId,
				node.getId());

		try {
			Serializable result = task.execute();
			taskResult.setResult(result);
			taskResult.setComplete(true);
		} catch (Exception e) {
			log.warn("Exception while executing GridTask", e);
			taskResult.setException(e);
			taskResult.setComplete(false);
		} finally {
			sendResult(taskResult);
		}
	}

	private void sendResult(GridTaskResultImpl result) {
		jmsTemplate.convertAndSend(result);
	}

	/**
	 * Internal class which implements {@link javax.jms.MessageListener}
	 * interface to act as the message listener for Task Messages. Provides
	 * mechanisms to extract the taskId from the incoming Message.
	 * 
	 * @author Yohan Liyanage
	 * 
	 */
	private class TaskMessageListener implements MessageListener {


		@SuppressWarnings("unchecked")
		// Ignore Generics
		public void onMessage(Message message) {
			try {
				int taskId = message.getIntProperty("taskId");
				GridTask task = (GridTask) ((ObjectMessage) message)
						.getObject();

				// Do Execution
				TaskExecutor.this.onTask(taskId, task);
			} catch (JMSException e) {
				log.warn("Exception while reading TaskMessage", e);
			}
		}
	}

}
