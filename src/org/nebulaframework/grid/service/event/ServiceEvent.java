package org.nebulaframework.grid.service.event;

import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

public class ServiceEvent {

	private ServiceMessageType type;
	private String message;

	
	public ServiceEvent() {
		super();
	}

	public ServiceMessageType getType() {
		return type;
	}

	public void setType(ServiceMessageType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isEvent(ServiceMessage message) {
		if (message.getType()==this.type) {
			if (message.getMessage().equals(this.message)) {
				return true;
			}
		}
		return false;
	}
}
