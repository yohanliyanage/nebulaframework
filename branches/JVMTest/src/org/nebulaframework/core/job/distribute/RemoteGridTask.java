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
package org.nebulaframework.core.job.distribute;

import java.io.Serializable;
import java.util.UUID;

import org.nebulaframework.core.GridTask;

/**
 * A Grid Task wrapper which is to be dispatched for remote execution.
 * @author Yohan Liyanage
 *
 */
public class RemoteGridTask implements Serializable{
	
	
	private static final long serialVersionUID = -7652685199169554306L;
	
	private GridTask<? extends Serializable> task;
	private UUID taskId;
	private UUID jobId;
	
	
	public RemoteGridTask(UUID jobId, GridTask<? extends Serializable> task,
			UUID taskId) {
		super();
		this.jobId = jobId;
		this.task = task;
		this.taskId = taskId;
	}

	public GridTask<? extends Serializable> getTask() {
		return task;
	}
	
	public UUID getTaskId() {
		return taskId;
	}
	
	public UUID getJobId() {
		return jobId;
	}

	@Override
	public String toString() {
		return "Job [" + this.jobId + "] Task [" + this.taskId + "] : " + this.task.toString();
	}
	
	
	
	
}
