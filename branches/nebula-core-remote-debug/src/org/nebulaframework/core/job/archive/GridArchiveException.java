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

package org.nebulaframework.core.job.archive;

/**
 * Thrown when an exceptional condition arises during
 * processing of a {@link GridArchive}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * @see GridArchive
 */
public class GridArchiveException extends Exception {

	private static final long serialVersionUID = -9017644319912170356L;

	/**
	 * Create a GridArchiveException without a message or cause.
	 */
	public GridArchiveException() {
		super();
	}
	
	/**
	 * Create a GridArchiveException with the given message and cause.
	 * 
	 * @param message
	 *            Message
	 * @param cause
	 *            Cause
	 */
	public GridArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a GridArchiveException with the given message.
	 * 
	 * @param message
	 *            Message
	 */
	public GridArchiveException(String message) {
		super(message);
	}

	/**
	 * Create a GridArchiveException with the given cause.
	 * 
	 * @param cause
	 *            Cause
	 */
	public GridArchiveException(Throwable cause) {
		super(cause);
	}

}