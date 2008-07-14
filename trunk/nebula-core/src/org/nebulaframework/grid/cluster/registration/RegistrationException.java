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

package org.nebulaframework.grid.cluster.registration;

/**
 * {@code RegistrationException} is thrown when a {@codeGridNode} registration
 * at a Cluster ({@code ClusterManager}) fails.
 * <p>
 * This is a checked exception.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class RegistrationException extends Exception {

	private static final long serialVersionUID = -6447294708307085793L;

	/**
	 * Create a RegistrationException without a message or cause.
	 */
	public RegistrationException() {
		super();
	}
	
	/**
	 * Create a RegistrationException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public RegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a RegistrationException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public RegistrationException(String message) {
		super(message);
	}

	/**
	 * Create a RegistrationException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public RegistrationException(Throwable cause) {
		super(cause);
	}
	
}
