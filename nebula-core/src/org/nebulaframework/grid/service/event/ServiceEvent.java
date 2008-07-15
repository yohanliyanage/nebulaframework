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
package org.nebulaframework.grid.service.event;

import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * Represents an event which is related to a {@code ServiceMessage}.
 * When a {@code ServiceMessage} which matches with the fields of 
 * this {@code ServiceEvent} is received, all {@code ServiceHookCallback}s
 * associated with this is invoked by the {@code ServiceEventsSupport}.
 * <p>
 * Refer to {@link ServiceEventsSupport} for more information.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see ServiceEventsSupport
 * @see ServiceHookCallback
 * @see ServiceMessage
 */
public class ServiceEvent {

	private List<ServiceMessageType> types = new ArrayList<ServiceMessageType>();
	private String message;

	/**
	 * Returns the {@code ServiceMessageType}s associated with this
	 * {@code ServiceEvent}.
	 * 
	 * @return An array of {@code ServiceMessageType}
	 */
	public ServiceMessageType[] getTypes() {
		return types.toArray(new ServiceMessageType[]{});
	}

	/**
	 * Adds a {@code ServiceMessageType} to the associated
	 * {@code ServiceMessageType}s of this {@code ServiceEvent};
	 * @param type {@code ServiceMessageType} to add
	 */
	public void addType(ServiceMessageType type) {
		if (!types.contains(type)) {
			types.add(type);
		}
	}

	/**
	 * Returns the message associated with this
	 * {@code ServiceEvent}.
	 * 
	 * @return String message body
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message associated with this
	 * {@code ServiceEvent}.
	 * 
	 * @param message String message body
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Compares with the given {@code ServiceMessage} to check
	 * whether this {@link ServiceEvent} matches for the given
	 * {@code ServiceMessage}.
	 * 
	 * @param message {@code ServiceMessage} to match with
	 * @return if matched {@code true}, else {@code false}
	 */
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
