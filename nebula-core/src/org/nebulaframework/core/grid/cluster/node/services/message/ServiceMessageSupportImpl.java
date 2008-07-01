package org.nebulaframework.core.grid.cluster.node.services.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.grid.cluster.node.GridNode;
import org.nebulaframework.core.servicemessage.ServiceMessage;

public class ServiceMessageSupportImpl implements ServiceMessagesSupport {

	private static Log log = LogFactory.getLog(ServiceMessageSupportImpl.class);
	
	@SuppressWarnings("unused") // TODO Remove
	private GridNode node;
	
	private ServiceMessage message;

	public ServiceMessageSupportImpl(GridNode node) {
		super();
		this.node = node;
	}

	public ServiceMessage getLastMessage() {
		return message;
	}

	public void onServiceMessage(ServiceMessage message) {
		log.info("[Service] " + message);
		this.message = message;
		// TODO Write the rest of code to manage message and notify relevant
		// parties
	}

}
