package org.nebulaframework.core.grid.cluster.manager.services.jobs.aggregator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.GridJobProfile;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.support.JMSNamingSupport;
import org.nebulaframework.core.job.GridJobState;
import org.nebulaframework.core.job.exceptions.AggregateException;
import org.nebulaframework.core.task.GridTaskResult;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

public class AggregatorServiceImpl implements AggregatorService {

	private static Log log = LogFactory.getLog(AggregatorServiceImpl.class);
	private ConnectionFactory connectionFactory;
	private ClusterJobServiceImpl jobService;

	public AggregatorServiceImpl(ClusterJobServiceImpl jobService) {
		super();
		this.jobService = jobService;
	}

	public void startAggregator(final GridJobProfile profile) {
		new Thread(new Runnable() {

			public void run() {
				doStartAggregator(profile);
			}

		}).start();
	}

	protected void doStartAggregator(GridJobProfile profile) {

		ResultCollector collector = new ResultCollector(profile, jobService);
		// Create Message Listener Adapter and Result Collector for Job
		MessageListenerAdapter adapter = new MessageListenerAdapter(collector);
		adapter.setDefaultListenerMethod("onResult");

		// Create JMS Message Listener Container
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setDestinationName(JMSNamingSupport
				.getResultQueueName(profile.getJobId()));
		container.setMessageListener(adapter);
		container.afterPropertiesSet();
		collector.setContainer(container);

	}

	public void aggregateResults(final GridJobProfile profile) {

		new Thread(new Runnable() {

			public void run() {
				doAggregateResults(profile);
			}

		}).start();

	}

	protected void doAggregateResults(GridJobProfile profile) {
		
		Collection<GridTaskResult> taskResults = profile.getResultMap().values();
		List<Serializable> results = new ArrayList<Serializable>();

		for (GridTaskResult result : taskResults) {
			results.add(result.getResult());
		}

		Serializable jobResult = null;

		try {
			jobResult = profile.getJob().aggregate(results);
		} catch (RuntimeException e) {
			log.warn("Exception while aggregating final result of "
					+ profile.getJobId(), e);
			profile.getFuture().setException(new AggregateException(e));
			profile.getFuture().setState(GridJobState.FAILED);
			return;
		}

		/*  -- Aggregation Complete : Job Complete -- */
		
		// Notify Workers
		jobService.notifyJobEnd(profile.getJobId());
		
		// Update Future and Return Result
		profile.getFuture().setResult(jobResult);
		profile.getFuture().setState(GridJobState.COMPLETE);
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

}
