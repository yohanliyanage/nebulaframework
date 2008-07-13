package org.nebulaframework.core.grid.cluster.manager.services.jobs.unbounded;

import java.io.Serializable;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.InternalClusterJobService;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.JMSNamingSupport;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.annotations.ProcessingSettings;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.core.task.GridTaskResult;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.util.Assert;

// TODO Fix Doc
public class UnboundedJobProcessor {

	private static Log log = LogFactory.getLog(UnboundedJobProcessor.class);

	private int maxTaskConstant = 100;
	private int reductionFactorConstant = 50;

	private GridJobProfile profile;
	private UnboundedGridJob<?, ?> job;

	private JmsTemplate jmsTemplate;
	private ConnectionFactory connectionFactory;
	private InternalClusterJobService jobService;
	private DefaultMessageListenerContainer container;

	public UnboundedJobProcessor(GridJobProfile profile,
			ConnectionFactory connectionFactory,
			InternalClusterJobService jobService) {
		super();

		Assert.notNull(profile);
		Assert.notNull(connectionFactory);
		Assert.notNull(jobService);

		this.profile = profile;
		this.job = (UnboundedGridJob<?, ?>) profile.getJob();
		
		// Use Reflection to extract any processing instructions
		extractProcessingInsructions(job);
		
		this.connectionFactory = connectionFactory;
		this.jobService = jobService;
	}

	private void extractProcessingInsructions(UnboundedGridJob<?, ?> job) {
		
		Class<?> clazz = job.getClass();
		ProcessingSettings settings = clazz.getAnnotation(ProcessingSettings.class);
		
		if (settings==null) return;
		
		this.maxTaskConstant = settings.maxTasksInQueue();
		this.reductionFactorConstant = settings.reductionFactor();
		
		log.debug("Custom Processing Instructions Found - maxTasks : " + 
		          maxTaskConstant + " | reduction factor : " + 
		          reductionFactorConstant);
		
	}

	public void start() {
		initialize();
		generateTasks();
	}

	private void generateTasks() {
		log.info("[UnboundedJobProcessor] Started Generating Tasks");
		new Thread(new Runnable() {

			public void run() {

				int taskId = 0;
				while (true) {

					GridTask<?> task = job.task();

					if (task == null) {
						log
								.info("[UnboundedJobProcessor] Task was Null. Stopping");
						break;
					}

					enqueueTask(profile.getJobId(), ++taskId, task);
					profile.addTask(taskId, task);

					// Pause for some duration if more than 100 tasks are there
					// to ensure TaskQueue won't overload
					try {
						if (profile.getTaskCount() > maxTaskConstant) {
							Thread.sleep(profile.getTaskCount() * reductionFactorConstant);
						}
					} catch (InterruptedException e) {
						log.error(e);
					}
				}

				// Task was null, Stop Job
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
		MessagePostProcessor postProcessor = new MessagePostProcessor() {

			public Message postProcessMessage(
					Message message)
					throws JMSException {

				/*-- Post Process to include Meta Data --*/
				
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
									
	}

	public void reEnqueueTask(final String jobId, final int taskId,
			GridTask<?> task) {
		enqueueTask(jobId, taskId, task);
	}

	protected void stopJob() {
		log.info("[UnboundedJobProcessor] Stopping Job Execution");
		// Notify Workers
		jobService.notifyJobEnd(profile.getJobId());

		// Update Future and return Result
		profile.getFuture().setResult(null);
		profile.getFuture().setState(GridJobState.COMPLETE);

		destroy();

	}

	private void initialize() {
		initializeResultListener();
		initializeTaskWritier();
	}

	private void initializeResultListener() {

		MessageListenerAdapter adapter = new MessageListenerAdapter(
				UnboundedJobProcessor.this);
		adapter.setDefaultListenerMethod("onResult");

		container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestinationName(JMSNamingSupport
				.getResultQueueName(profile.getJobId()));
		container.setMessageListener(adapter);
		container.afterPropertiesSet();

	}

	private void initializeTaskWritier() {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
	}

	public void onResult(final GridTaskResult taskResult) {
		if (taskResult.isComplete()) { // Result is Valid / Complete

			log.debug("[UnboundedJobProcessor] Received Result : Task "
					+ taskResult.getTaskId());

			// Post Process Result
			Serializable result = doPostProcessResult(taskResult.getResult());

			if (result != null) {
				// Fire callback if its not null
				profile.fireCallback(result);
			}

			// Task completed, remove it from TaskMap
			profile.removeTask(taskResult.getTaskId());

		} else { // Result Not Valid / Exception

			log.warn("[UnboundedJobProcessor] Result Failed ["
					+ taskResult.getTaskId() + "], ReEnqueueing - "
					+ taskResult.getException());

			// Request re-enqueue of Task
			jobService.getSplitterService()
					.reEnqueueTask(profile.getJobId(), taskResult.getTaskId(),
									profile.getTask(taskResult.getTaskId()));
		}
	}

	protected Serializable doPostProcessResult(final Serializable result) {
		return job.processResult(result);
	}

	protected void destroy() {
		if (container != null)
			container.shutdown();
	}

}
