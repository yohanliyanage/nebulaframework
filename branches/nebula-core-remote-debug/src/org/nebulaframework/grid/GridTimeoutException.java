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
package org.nebulaframework.grid;

/**
 * Denotes a TimeOut event occurred with in Grid.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridTimeoutException extends Exception {

	private static final long serialVersionUID = 7656800161725963750L;

	/**
	 * Create a GridExecutionException without a message or cause.
	 */
	public GridTimeoutException() {
		super();
	}

	/**
	 * Create a GridExecutionException with the given message
	 * @param message Message
	 */
	public GridTimeoutException(String message) {
		super(message);
	}

	/**
	 * Create a GridExecutionException with the given cause
	 * @param cause Cause
	 */
	public GridTimeoutException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a GridExecutionException with the given message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public GridTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}
