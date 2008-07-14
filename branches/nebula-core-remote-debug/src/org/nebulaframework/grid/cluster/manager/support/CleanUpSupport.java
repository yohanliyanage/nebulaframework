package org.nebulaframework.grid.cluster.manager.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.service.event.ServiceEvent;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;


// TODO FixDoc
public class CleanUpSupport {

	private static Log log = LogFactory.getLog(CleanUpSupport.class);
	
	public static void removeQueueWhenFinished(String jobId, String queueName) {
		removeQueueWhenFinished(jobId, queueName, null);
	}
	
	public static void removeQueueWhenFinished(String jobId, String queueName, DefaultMessageListenerContainer container) {

		// Create Event & Callback
		ServiceEvent event = createJobEndEvent(jobId);
		ServiceHookCallback callback = createRemoveQueueCallback(queueName , container);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}
	

	public static void removeTopicWhenFinished(String jobId, String topicName) {
		// Create Event & Callback
		ServiceEvent event = createJobEndEvent(jobId);
		ServiceHookCallback callback = createRemoveTopicCallback(topicName);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}

	public static void shutdownContainerWhenFinished(String jobId, DefaultMessageListenerContainer container) {
		// Create Event & Callback
		ServiceEvent event = createJobEndEvent(jobId);
		ServiceHookCallback callback = createShutdownContainerCallback(container);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}
	


	public static void removeQueueWhenNodeLeft(String nodeId, String queueName) {
		removeQueueWhenNodeLeft(nodeId, queueName, null);
	}
	
	public static void removeQueueWhenNodeLeft(String nodeId, String queueName, DefaultMessageListenerContainer container) {
		// Create Event & Callback
		ServiceEvent event = createNodeLeftEvent(nodeId);
		ServiceHookCallback callback = createRemoveQueueCallback(queueName, container);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}
	
	public static void removeTopicWhenNodeLeft(String nodeId, String topicName) {
		// Create Event & Callback
		ServiceEvent event = createNodeLeftEvent(nodeId);
		ServiceHookCallback callback = createRemoveTopicCallback(topicName);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}
	
	public static void shutdownContainerWhenNodeLeft(String nodeId, DefaultMessageListenerContainer container) {
		// Create Event & Callback
		ServiceEvent event = createNodeLeftEvent(nodeId);
		ServiceHookCallback callback = createShutdownContainerCallback(container);
		
		// Add Hook
		ServiceEventsSupport.getInstance().addServiceHook(event, callback);
	}
	
	private static ServiceEvent createJobEndEvent(String jobId) {
		ServiceEvent event = new ServiceEvent();
		event.setMessage(jobId);
		event.addType(ServiceMessageType.JOB_CANCEL);
		event.addType(ServiceMessageType.JOB_END);
		return event;
	}
	
	private static ServiceEvent createNodeLeftEvent(String nodeId) {
		ServiceEvent event = new ServiceEvent();
		event.setMessage(nodeId);
		event.addType(ServiceMessageType.NODE_UNREGISTERED);
		return event;
	}
	
	private static ServiceHookCallback createRemoveQueueCallback(final String queueName, final DefaultMessageListenerContainer container) {
		ServiceHookCallback callback = new ServiceHookCallback() {

			public void onServiceEvent() {
				if (container != null) container.shutdown();
				JMSResourceSupport.removeQueue(queueName);
			}
			
		};
		return callback;
	}
	
	private static ServiceHookCallback createRemoveTopicCallback(final String topicName) {
		ServiceHookCallback callback = new ServiceHookCallback() {

			public void onServiceEvent() {
				JMSResourceSupport.removeTopic(topicName);
			}
			
		};
		return callback;
	}
	
	private static ServiceHookCallback createShutdownContainerCallback(
			final DefaultMessageListenerContainer container) {
		ServiceHookCallback callback = new ServiceHookCallback() {

			public void onServiceEvent() {
				try {
					if (container!=null) container.shutdown();
				} catch (JmsException e) {
					log.debug("Unable to shutdown container",e);
				}
			}
			
		};
		return callback;
	}
}
