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
package org.nebulaframework.grid.cluster.manager.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.service.event.ServiceEvent;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;


/**
 * Provides Resource Clean Up {@code ServiceHooks} to release unwanted
 * resources after usage.
 * 
 * For more information about {@code ServiceEvent}s mechanism,
 * refer to {@link ServiceEventsSupport}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class CleanUpSupport {

	private static Log log = LogFactory.getLog(CleanUpSupport.class);
	
	/**
	 * Removes specified Queue when the given job is finished
	 * 
	 * @param jobId GridJob Id
	 * @param queueName Queue to be removed
	 */
	public static void removeQueueWhenFinished(String jobId, String queueName) {
		removeQueueWhenFinished(jobId, queueName, null);
	}
	
	/**
	 * Shuts down the given container, and removes specified queue 
	 * when the given job is finished
	 * 
	 * @param jobId GridJob Id
	 * @param queueName Queue to be removed
	 * @param container Message Listener Container
	 */
	public static void removeQueueWhenFinished(String jobId, String queueName, DefaultMessageListenerContainer container) {
		ServiceEventsSupport.addServiceHook(createJobEndEvent(jobId), createRemoveQueueCallback(queueName , container));
	}
	
	/**
	 * Removes specified Topic when the given job is finished
	 * 
	 * @param jobId GridJob Id
	 * @param topicName Topic to be removed
	 */
	public static void removeTopicWhenFinished(String jobId, String topicName) {
		ServiceEventsSupport.addServiceHook(createJobEndEvent(jobId), createRemoveTopicCallback(topicName));
	}

	/**
	 * Shutdowns the given container when the specified job is finished.
	 * 
	 * @param jobId Job Id
	 * @param container Message Listener Container to be shutdown
	 */
	public static void shutdownContainerWhenFinished(String jobId, DefaultMessageListenerContainer container) {
		ServiceEventsSupport.addServiceHook(createJobEndEvent(jobId), createShutdownContainerCallback(container));
	}
	
	/**
	 * Removes the specified Queue when the given node has left the Grid.
	 * 
	 * @param nodeId GridNode Id
	 * @param queueName Queue to be removed
	 */
	public static void removeQueueWhenNodeLeft(String nodeId, String queueName) {
		removeQueueWhenNodeLeft(nodeId, queueName, null);
	}
	
	/**
	 * Shutdowns the specified container and removes the specified Queue when the 
	 * given node has left the Grid.
	 * 
	 * @param nodeId GridNode Id
	 * @param queueName Queue to be removed
	 * @param container Message Listener Container to be shutdown
	 */
	public static void removeQueueWhenNodeLeft(String nodeId, String queueName, DefaultMessageListenerContainer container) {
		ServiceEventsSupport.addServiceHook(createNodeLeftEvent(nodeId), createRemoveQueueCallback(queueName, container));
	}
	
	/**
	 * Removes the specified Topic when the given node has left the Grid.
	 * 
	 * @param nodeId GridNode Id
	 * @param topicName Topic to be removed
	 */
	public static void removeTopicWhenNodeLeft(String nodeId, String topicName) {
		ServiceEventsSupport.addServiceHook(createNodeLeftEvent(nodeId), createRemoveTopicCallback(topicName));
	}
	
	/**
	 * Shutdowns the specified container when the given node has 
	 * left the Grid.
	 * 
	 * @param nodeId GridNode Id
	 * @param container Message Listener Container to be shutdown
	 */
	public static void shutdownContainerWhenNodeLeft(String nodeId, DefaultMessageListenerContainer container) {
		ServiceEventsSupport.addServiceHook(createNodeLeftEvent(nodeId),createShutdownContainerCallback(container));
	}
	
	/**
	 * Creates and returns a JOB_END {@code ServiceEvent} for
	 * given JobId
	 * 
	 * @param jobId GridJobId
	 * 
	 * @return ServiceEvent
	 */
	private static ServiceEvent createJobEndEvent(String jobId) {
		ServiceEvent event = new ServiceEvent();
		event.setMessage(jobId);
		event.addType(ServiceMessageType.JOB_CANCEL);
		event.addType(ServiceMessageType.JOB_END);
		return event;
	}
	
	/**
	 * Creates and returns a NODE_UNREGISTERED {@code ServiceEvent} for
	 * given Node Id
	 * 
	 * @param nodeId GridNode Id
	 * 
	 * @return ServiceEvent
	 */
	private static ServiceEvent createNodeLeftEvent(String nodeId) {
		ServiceEvent event = new ServiceEvent();
		event.setMessage(nodeId);
		event.addType(ServiceMessageType.NODE_UNREGISTERED);
		return event;
	}
	
	/**
	 * Creates and returns a {@link ServiceHookCallback} which shutdowns the given
	 * container and removes the given queue from broker.
	 * 
	 * @param queueName Queue to be removed
	 * @param container Container to be shutdown (can be {@code null})
	 * 
	 * @return ServiceHookCallback
	 */
	private static ServiceHookCallback createRemoveQueueCallback(final String queueName, final DefaultMessageListenerContainer container) {
		ServiceHookCallback callback = new ServiceHookCallback() {
			public void onServiceEvent(ServiceMessage message) {
				if (container != null) container.shutdown();
				JMSResourceSupport.removeQueue(queueName);
			}
			
		};
		return callback;
	}
	
	/**
	 * Creates and returns a {@link ServiceHookCallback} which removes 
	 * the given topic from broker.
	 * 
	 * @param topicName Topic to be removed
	 * 
	 * @return ServiceHookCallback
	 */
	private static ServiceHookCallback createRemoveTopicCallback(final String topicName) {
		ServiceHookCallback callback = new ServiceHookCallback() {

			public void onServiceEvent(ServiceMessage message) {
				JMSResourceSupport.removeTopic(topicName);
			}
			
		};
		return callback;
	}
	
	/**
	 * Creates and returns a {@link ServiceHookCallback} which shutdowns the given
	 * container.
	 * 
	 * @param container Container to be shutdown
	 * 
	 * @return ServiceHookCallback
	 */
	private static ServiceHookCallback createShutdownContainerCallback(
			final DefaultMessageListenerContainer container) {
		ServiceHookCallback callback = new ServiceHookCallback() {

			public void onServiceEvent(ServiceMessage message) {
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
