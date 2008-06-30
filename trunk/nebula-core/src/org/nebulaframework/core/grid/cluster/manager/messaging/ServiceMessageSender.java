package org.nebulaframework.core.grid.cluster.manager.messaging;

import org.nebulaframework.core.grid.cluster.ServiceMessage;

public interface ServiceMessageSender {
	public void sendServiceMessage(ServiceMessage message);
}
