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
 * Thrown when a {@code GridJob} is rejected during the
 * time of submission.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridJobRejectionException extends RuntimeException {

	private static final long serialVersionUID = 175575667877619537L;

	/**
	 * Create a GridJobRejectionException without a message or cause.
	 */
	public GridJobRejectionException() {
		super();
	}

	/**
	 * Create a GridJobRejectionException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public GridJobRejectionException(String message) {
		super(message);
	}

	/**
	 * Create a GridJobRejectionException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public GridJobRejectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a GridJobRejectionException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public GridJobRejectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
