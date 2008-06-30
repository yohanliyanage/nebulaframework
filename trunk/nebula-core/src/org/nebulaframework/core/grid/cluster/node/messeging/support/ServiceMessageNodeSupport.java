package org.nebulaframework.core.grid.cluster.node.messeging.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

public class ServiceMessageNodeSupport {
	
	private static Map<String, AbstractMessageListenerContainer> map = new HashMap<String, AbstractMessageListenerContainer>();
	
	public static void registerServiceTopic(ApplicationContext ctx, String serviceTopic) {
		AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) ctx.getBean("serviceTopicJMSContainer");
		container.setDestinationName(serviceTopic);
		container.start();
		if (map.containsKey(serviceTopic)) {
			throw new IllegalArgumentException("Already registered for Service Topic " + serviceTopic);
		}
		map.put(serviceTopic, container);
	}
	
	public static void unregisterServiceTopic(String serviceTopic) {
		AbstractMessageListenerContainer container = map.get(serviceTopic);
		if (container == null) {
			throw new IllegalArgumentException("Not registered for Service Topic " + serviceTopic);
		}
		container.stop();
	}
}
