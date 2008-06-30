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
package org.nebulaframework.core.task;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridTaskResult;
import org.nebulaframework.core.GridTaskResultImpl;
import org.nebulaframework.core.grid.GridNode;
import org.nebulaframework.core.job.distribute.RemoteGridTask;
import org.nebulaframework.core.job.distribute.ResultQueue;
import org.nebulaframework.core.job.distribute.TaskQueue;

/**
 * TaskExecutor manages Task Execution with in a node.
 * @author Yohan Liyanage
 *
 */
public class TaskExecutor {

	private int taskCount = 0;
	private static Log log = LogFactory.getLog(TaskExecutor.class);
	private GridNode node;
	private TaskQueue taskQueue;
	private ResultQueue<? extends Serializable> resultQueue;
	private boolean stopped = true;
	private Object mutex = new Object();


	public TaskExecutor(GridNode node) {
		super();
		this.node = node;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public <T extends Serializable> void setResultQueue(
			ResultQueue<T> resultQueue) {
		this.resultQueue = resultQueue;
	}

	public void start() {

		
		log.info("Starting TaskExecutor of Node [" + this.node.getName() + "]");
		if (this.taskQueue == null || this.resultQueue == null) {
			throw new IllegalStateException("TaskQueue or ResultQueue not Set");
		}

		synchronized (mutex) {
			this.stopped = false;
		}

		taskCount = 0;	// Reset Count
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				while (true) {
					synchronized(mutex) {
						//If Executor is Stopped (at end of job), then stop Thread
						if (stopped) {
							break;
						}
					}
					
					//Get Task from Queue
					RemoteGridTask task = taskQueue.getTask(node.getName());
					
					//If no task in queue
					if (task == null) {
						try {
							//Sleep and re-check
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// No-operation
						}
						continue;
					}
					
					executeTask(task);
					
				}
			}

		}).start();

	}
	
	private void executeTask(RemoteGridTask task) {
		
		log.debug("TaskExecutor of Node " + node.getName() + " is executing Task : " + task.getJobId() + "|" + task.getTaskId());
		GridTaskResultImpl taskResult = new GridTaskResultImpl(task.getJobId(), task.getTaskId(), this.node.getName());
		
		try {
			Serializable result = task.getTask().execute();
			taskResult.setResult(result);
			taskResult.setComplete(true);
		} catch (Exception e) {
			log.info("Exception at Task Execution - ",e);
			taskResult.setComplete(false);
			taskResult.setException(e);
		}
		finally {
			log.debug("TaskExecutor of Node " + node.getName() + " is Submitting Result for Task: " + task.getJobId() + "|" + task.getTaskId());
			resultQueue.submit((GridTaskResult)taskResult);
			taskCount++;
		}
	}

	public void stop() {
		synchronized (mutex) {
			this.stopped = true;
		}
		log.debug("TaskExecutor of Node " + node.getName() + " is stopping");
	}

	public void clean() {
		synchronized (mutex) {
			if (this.stopped) {
				this.taskQueue = null;
				this.resultQueue = null;
				log.debug("TaskExecutor of Node " + node.getName() + " executed " + this.taskCount + " tasks");
				this.taskCount = 0;
				log.debug("TaskExecutor of Node " + node.getName() + " was cleaned");
			}
		}
		
	}

}
