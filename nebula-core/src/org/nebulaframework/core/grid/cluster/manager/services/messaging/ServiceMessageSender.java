package org.nebulaframework.core.grid.cluster.manager.services.messaging;

import org.nebulaframework.core.servicemessage.ServiceMessage;

public interface ServiceMessageSender {
	public void sendServiceMessage(ServiceMessage message);
}
