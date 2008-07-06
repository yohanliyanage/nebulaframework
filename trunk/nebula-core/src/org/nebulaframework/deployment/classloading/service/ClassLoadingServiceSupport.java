package org.nebulaframework.deployment.classloading.service;

import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.nebulaframework.core.grid.cluster.manager.ClusterManager;
import org.nebulaframework.core.grid.cluster.manager.services.jobs.ClusterJobServiceImpl;
import org.nebulaframework.core.grid.cluster.manager.services.registration.ClusterRegistrationServiceImpl;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;

public class ClassLoadingServiceSupport {

	public static void startClassLoadingService(ClusterManager manager, ConnectionFactory connectionFactory) {
		ClassLoadingServiceImpl service = new ClassLoadingServiceImpl(manager);
		
		// FIXME This type casting may cause troubles in future versions, if real implementation differs
		service.setJobServiceImpl((ClusterJobServiceImpl)manager.getJobService());
		service.setRegServiceImpl((ClusterRegistrationServiceImpl) manager.getClusterRegistrationService());
		
		JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
		exporter.setService(service);
		exporter.setServiceInterface(ClassLoadingService.class);
		exporter.afterPropertiesSet();
		
		ActiveMQQueue queue = new ActiveMQQueue(getQueueName(manager.getClusterId()));
		
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setDestination(queue);
		container.setMessageListener(exporter);
		container.setConnectionFactory(connectionFactory);
		container.afterPropertiesSet();
	}

	private static String getQueueName(UUID clusterId) {
		return "nebula.cluster." + clusterId + ".classloading.queue";
	}
	
	public static ClassLoadingService createProxy(UUID clusterId, ConnectionFactory connectionFactory) {
		JmsInvokerProxyFactoryBean proxyFactory = new JmsInvokerProxyFactoryBean();
		proxyFactory.setConnectionFactory(connectionFactory);
		proxyFactory.setServiceInterface(ClassLoadingService.class);
		proxyFactory.setQueueName(getQueueName(clusterId));
		proxyFactory.afterPropertiesSet();
		return (ClassLoadingService) proxyFactory.getObject();
	}
}
