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
package org.nebulaframework.core.grid;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.JobListener;
import org.nebulaframework.core.job.JobPriority;
import org.nebulaframework.core.job.distribute.ResultQueue;
import org.nebulaframework.core.job.distribute.TaskQueue;
import org.nebulaframework.core.task.TaskExecutor;

/**
 * Mock GridNode Implementation through Threads. 
 * @author Yohan Liyanage
 *
 */
public class GridNode implements JobListener{
	private static Log log = LogFactory.getLog(GridNode.class);
	
	private TaskExecutor executor;
	private String name;

	public GridNode(String name) {
		super();
		this.name = name;
		executor = new TaskExecutor(this);
	}

	public String getName() {
		return name;
	}

	
	@Override
	public <T extends Serializable> void startJob(UUID jobId, JobPriority priority, TaskQueue taskQueue,
			ResultQueue<T> resultQueue) {
		// TODO Re implement considering Priorities etc
		
		log.debug("Starting Job " + jobId + " at Node " + this.name);
		executor.setTaskQueue(taskQueue);
		executor.setResultQueue(resultQueue);
		executor.start();
	}

	@Override
	public void endJob(UUID jobId) {
		log.debug("Ending Job " + jobId + " at Node " + this.name);
		executor.stop();
		executor.clean();
	}

	
}
