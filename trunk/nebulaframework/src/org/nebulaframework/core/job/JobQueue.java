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
package org.nebulaframework.core.job;

/**
 * Interface for JobQueue. The JobQueue will be implemented 
 * priority based blocking queue.
 * 
 * @author Yohan Liyanage
 *
 */
public interface JobQueue {

	/**
	 * Adds the given RemoteGridJob to the JobQueue
	 * @param job Job to add
	 */
	public void add(RemoteGridJob job);

	/**
	 * Removes the given RemoteGridJob from Queue, if possible.
	 * @param job Job to remove
	 * @return <tt>true</tt> if successfully removed
	 */
	public boolean remove(RemoteGridJob job);

	/**
	 * Returns the next available Job from the JobQueue.
	 * This method will block until a Job becomes available.
	 * @return RemoteGridJob nextJob in Queue
	 */
	public RemoteGridJob nextJob();
}
