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

package org.nebulaframework.util.jms;

/**
 * A support class which returns JMS Resource Names for
 * given parameters.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class JMSNamingSupport {
	
	/**
	 * Returns the {@code TaskQueue} Name for a given JobId.
	 * 
	 * @param jobId JobId
	 * @return String TaskQueue Name
	 */
	public static String getTaskQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".task.queue";
	}

	/**
	 * Returns the {@code ResultQueue} Name for a given JobId.
	 * 
	 * @param jobId JobId
	 * @return String ResultQueue Name
	 */
	public static String getResultQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".result.queue";
	}

	/**
	 * Returns the {@code FutureQueue} Name for a given JobId.
	 * 
	 * @param jobId JobId
	 * @return String FutureQueue Name
	 */
	public static String getFutureQueueName(String jobId) {
		return "nebula.jobs." + jobId + ".future.queue";
	}

	public static String getRemoteJobServiceQueueName() {
		return "nebula.grid.jobservice.queue";
	}
	
	public static String getHeartBeatServiceQueueName() {
		return "nebula.grid.heartbeat.queue";
	}
	
}
