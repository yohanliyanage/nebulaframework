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
package org.nebulaframework.util.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

/**
 * Provides support routines to create and consume
 * remote services using Spring JMS Remoting API.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class JMSRemotingSupport {

	/**
	 * Creates and returns a proxy object which can be
	 * used to consume a remote service.
	 * 
	 * @param <T> Type of Service Interface
	 * @param cf JMS Connection Factory
	 * @param queueName Name of JMS Queue used for communication
	 * @param serviceClass Service Interface Class
	 * @return Proxy for the remote service
	 */
	@SuppressWarnings("unchecked")
	/* Ignore Unchecked Cast */
	public static <T> T createProxy(ConnectionFactory cf, String queueName,
			Class<T> serviceClass) {
		
		// Create Proxy Factory Instance
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(cf);
		proxyFactory.setQueueName(queueName);
		proxyFactory.setServiceInterface(serviceClass);
		proxyFactory.afterPropertiesSet();

		// Return Proxy
		return (T) proxyFactory.getObject();
	}

	/**
	 * Remote enables a given object as a remote JMS Service
	 * through a given Service Interface.
	 * 
	 * @param <T> Type of Service Interface
	 * @param cf JMS Connection Factory
	 * @param queueName Name of JMS Queue used for communication
	 * @param service Service object to be remote enabled
	 * @param serviceClass Service Interface Class
	 * @return Message Listener Container
	 */
	public static <T> DefaultMessageListenerContainer createService(
			ConnectionFactory cf, String queueName, T service, Class<T> serviceClass) {

		ActiveMQQueue queue = new ActiveMQQueue(queueName);

		// Export Service
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setServiceInterface(serviceClass);
		exporter.setService(service);
		exporter.afterPropertiesSet();

		// Set up MessageListenerContainer
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(cf);
		container.setDestination(queue);
		container.setMessageListener(exporter);
		container.afterPropertiesSet();

		return container;
	}
}
