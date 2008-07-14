package org.nebulaframework.grid.service.event;

import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

// TODO FixDoc
public class ServiceEvent {

	private List<ServiceMessageType> types = new ArrayList<ServiceMessageType>();
	private String message;

	
	public ServiceEvent() {
		super();
	}

	public ServiceMessageType[] getTypes() {
		return types.toArray(new ServiceMessageType[]{});
	}

	public void addType(ServiceMessageType type) {
		if (!types.contains(type)) {
			types.add(type);
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isEvent(ServiceMessage message) {
		for (ServiceMessageType type : types) {
			if (message.getType()==type) {
				if (message.getMessage().equals(this.message)||this.message==null) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof ServiceEvent)) {
			return false;
		}
		ServiceEvent elm = (ServiceEvent) obj;
		
		return (this.types==elm.types && this.message.equals(elm.getMessage()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + this.types.hashCode() * this.message.hashCode() / 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Service Event (" + this.types + " : " + this.message + ")";
	}
	
	
}
