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
package org.nebulaframework.core.job.exceptions;

/**
 * Denotes a security violation occurred while executing
 * a GridJob. Note that security violations result in
 * complete job termination.
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
public class SecurityViolationException extends SecurityException {

	private static final long serialVersionUID = -7979918329053885097L;

	/**
	 * Create a SecurityViolationException without a message or cause.
	 */
	public SecurityViolationException() {
		super();
	}

	/**
	 * Create a SecurityViolationException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public SecurityViolationException(String message) {
		super(message);
	}

	/**
	 * Create a SecurityViolationException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public SecurityViolationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a SecurityViolationException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public SecurityViolationException(String message, Throwable cause) {
		super(message, cause);
	}

}
