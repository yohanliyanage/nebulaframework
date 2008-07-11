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
 * Thrown when a {@code GridNode}'s request to participate
 * in a {@code GridJob} is denied.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridJobPermissionDeniedException extends Exception {

	private static final long serialVersionUID = -3031404025046256470L;


	/**
	 * Create a GridJobPermissionDeniedException without a message or cause.
	 */
	public GridJobPermissionDeniedException() {
		super();
	}

	/**
	 * Create a GridJobPermissionDeniedException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public GridJobPermissionDeniedException(String message) {
		super(message);
	}

	/**
	 * Create a GridJobPermissionDeniedException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public GridJobPermissionDeniedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a GridJobPermissionDeniedException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public GridJobPermissionDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

}
