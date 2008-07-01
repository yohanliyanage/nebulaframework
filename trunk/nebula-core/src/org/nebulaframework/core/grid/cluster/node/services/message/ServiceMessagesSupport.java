package org.nebulaframework.core.grid.cluster.node.services.message;

import org.nebulaframework.core.servicemessage.ServiceMessage;

public interface ServiceMessagesSupport {
	public void onServiceMessage(ServiceMessage message);
	public ServiceMessage getLastMessage();
}
