package org.nebulaframework.core.grid.cluster.manager.services.jobs.splitter;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSNamingSupport;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.exceptions.SplitException;
import org.nebulaframework.core.task.GridTask;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

/* Ignore Generics for clarity */
@SuppressWarnings("unchecked")
public class SpliterServiceImpl implements SplitterService {

	private static Log log = LogFactory.getLog(SpliterServiceImpl.class);
	private JmsTemplate jmsTemplate;

	public void startSplitter(final GridJobProfile profile) {

		// Start splitting on a separate thread

		new Thread(new Runnable() {
			public void run() {
				doStartSplitter(profile);
			}
		}).start();

	}

	protected void doStartSplitter(final GridJobProfile profile) {

		// Update status
		profile.getFuture().setState(GridJobState.INITIALIZING);

		try {
			GridJob job = profile.getJob();

			// Split to Tasks
			log.debug("Splitting Tasks");
			List taskList = job.split();

			log.debug("Enqueueing Tasks");
			for (int i = 0; i < taskList.size(); i++) {
				GridTask task = (GridTask) taskList.get(i);
				enqueueTask(profile.getJobId(), i, task); 	// Put to Task Queue
				profile.getTaskMap().put(i, task); 			// Put copy to TaskMap in Profile
			}
		} catch (Exception e) {
			log.warn("Exception while Splitting Job " + profile.getJobId(), e);
			profile.getFuture().setException(new SplitException(e));
			profile.getFuture().setState(GridJobState.FAILED);
			return;
		}

		profile.getFuture().setState(GridJobState.EXECUTING);
		
		log.debug("Enqueued. Waiting Execution");
	}

	private void enqueueTask(final String jobId, final int taskId, GridTask task) {

		jmsTemplate.convertAndSend(JMSNamingSupport.getTaskQueueName(jobId),
				task, new MessagePostProcessor() {

					public Message postProcessMessage(Message message)
							throws JMSException {
						message.setJMSCorrelationID(jobId); 		// Set Correlation ID to Job Id
						message.setIntProperty("taskId", taskId); 	// Put taskId as a property
						log.debug("Enqueued Task : " + taskId);
						return message;
					}

				});
	}

	public void reEnqueueTask(final String jobId, final int taskId, GridTask task) {
		enqueueTask(jobId, taskId, task);
	}
	
	@Required
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
		
		/* TODO Remove if not needed
		 * // Enable explicit client acknowledge
		this.jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);*/
	}

}
