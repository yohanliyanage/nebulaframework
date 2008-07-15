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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;

/**
 * Provides support routines to clean unwanted JMS Resources.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class JMSResourceSupport {

	private static Log log = LogFactory.getLog(JMSResourceSupport.class);
	private static ExecutorService cleanupExecutor = Executors.newSingleThreadExecutor();
	
	/**
	 * Maximum attempts to remove destination before reporting exception
	 */
	private static final int MAX_ATTEMPTS = 5;
	
	/**
	 * Removes the given JMS Queue from Broker.
	 * 
	 * @param queueName JMS Queue Name
	 */
	public static void removeQueue(String queueName) {
		removeDestination(new ActiveMQQueue(queueName));
	}
	
	/**
	 * Removes the given JMS Topic from Broker.
	 * 
	 * @param topicName JMS Topic Name
	 */
	public static void removeTopic (String topicName) {
		removeDestination(new ActiveMQTopic(topicName));
	}
	
	/**
	 * Removes the given ActiveMQ Destination from Broker.
	 * 
	 * @param destination ActiveMQ Destination
	 */
	private static void removeDestination(final ActiveMQDestination destination) {
		
		// Not supported on GridNodes (only ClusterManagers)
		if (Grid.isNode()) throw new UnsupportedOperationException("GridNode cannot remove Queues");
		
		final BrokerService broker = ClusterManager.getInstance().getBrokerService();
		
		if (broker==null) { // If No BrokerService
			return;
		}
		
		// Add to thread pool executor
		cleanupExecutor.submit(new Runnable() {

			public void run() {

				boolean attempt = true;
				int count = 0;
				
				while (attempt) {
					try {
						// Try to remove destination
						broker.removeDestination(destination);
						log.debug("JMS Destination " +destination.getPhysicalName() + " removed");
						attempt = false;
					} catch (Exception e) {
						
						// If failed
						if (++count > MAX_ATTEMPTS) {
							// If more than max attempts, report exception at WARN level and return
							attempt = false;
							log.warn("Unable to remove Destination : " + destination.getPhysicalName(),e);
							return;
						}
						
						if (count > 2) {
							// If failed twice, log at DEBUG level
							log.debug("Unable to remove Destination : " + destination.getPhysicalName() + " - Attempt " + count);
						}
						
						// Retry
						attempt = true;
						
						try {
							// Wait sometime till resources are released
							Thread.sleep(5000);
						} catch (InterruptedException ex) {
							log.debug(ex);
						}
					}
				}				
			}
		});
	}
}
