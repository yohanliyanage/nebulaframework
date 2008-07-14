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

package org.nebulaframework.grid.service.message;

import java.io.Externalizable;
import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * A {@code ServiceMessage} is a message sent by {@code ClusterManager} 
 * to {@code GridNode}s registered with it, using the {@code ServiceTopic}.
 * Service messages may be sent to notify various types of information 
 * to the {@code GridNode}, specified by {@link ServiceMessageType}.
 * <p>
 * The main categories of ServiceMessages as of current implementation is
 * <ul>
 * 	<li>Job Messages (Start, End, Cancel)</li>
 * 	<li>Cluster Messages (Shutdown of {@code ClusterManager})</li>
 * </ul>
 * <p> 
 * This class implements {@link Externalizable} interface, instead of {@link Serializable}
 * to improve performance in communications, by reducing the data transfer amount and
 * serialization time [Grosso, W. 2001. "Java RMI", Section 10.7.1].

 * @author Yohan Liyanage
 * @version 1.0
 */
public class ServiceMessage implements Serializable {

	private static final long serialVersionUID = 1971655973546408806L;
	
	private ServiceMessageType type;
	private String message;

	/**
	 * Constructs a ServiceMessage with given message body.
	 * 
	 * @param message Message
	 * @param type Type of message
	 * @throws IllegalArgumentException if {@code message} or {@code type} is {@code null}.
	 */
	public ServiceMessage(String message, ServiceMessageType type) throws IllegalArgumentException{
		super();
		
		// Check if arguments are null
		Assert.notNull(message);
		Assert.notNull(type);
		
		this.message = message;
		this.type = type;
	}

	/**
	 * Returns the message body of this {@code ServiceMessage}
	 * instance.
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the type of this {@code ServiceMessage}
	 * instance.
	 * 
	 * @return type of message
	 */
	public ServiceMessageType getType() {
		return type;
	}

	/**
	 * Returns true if this message is a Job related message.
	 * That is, message types {@code JOB_START}, {@code JOB_END} 
	 * and {@code JOB_CANCEL}.
	 * 
	 * @return if job related, {@code true}, otherwise {@code false}
	 */
	public boolean isJobMessage() {
		return (type == ServiceMessageType.JOB_START)
				|| (type == ServiceMessageType.JOB_END)
				|| (type == ServiceMessageType.JOB_CANCEL);
	}

	/**
	 * Returns true if this message is a Cluster related message.
	 * That is, message types {@code CLUSTER_SHUTDOWN}.
	 * 
	 * @return if cluster related, {@code true}, otherwise {@code false}
	 */
	public boolean isClusterMessage() {
		return (type == ServiceMessageType.CLUSTER_SHUTDOWN);
	}

	/**
	 * Returns a String representation of this {@code ServiceMessage}.
	 * It is of format
	 * <ul><li><code><i>type</i> : <i>message</i></code></li></ul>
	 * 
	 * @return String representation of {@code ServiceMessage}.
	 */
	@Override
	public String toString() {
		return type + " : " + message;
	}

}
