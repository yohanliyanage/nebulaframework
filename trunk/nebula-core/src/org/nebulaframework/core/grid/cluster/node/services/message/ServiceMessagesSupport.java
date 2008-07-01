package org.nebulaframework.core.grid.cluster.node.services.message;

import org.nebulaframework.core.grid.cluster.ServiceMessage;

public interface ServiceMessagesSupport {
	public void onServiceMessage(ServiceMessage message);
	public ServiceMessage getLastMessage();
}
