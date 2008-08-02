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
package test.remote.testjob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

/**
 * GridJob Implementation for Testing
 * @author Yohan Liyanage
 *
 */
public class TestJob implements SplitAggregateGridJob<Integer, Integer>{

	private static final long serialVersionUID = -4504183475114576465L;

	
	public List<? extends GridTask<Integer>> split() {
		//System.out.println("Splitting...");
		List<TestTask> tasks = new ArrayList<TestTask>();
		
		for(int i=0; i < 50; i++) {
			tasks.add(new TestTask());
		}
		
		//System.out.println("Splitting...Done");
		return tasks;
	}

	public Integer aggregate(List<? extends Serializable> results) {
		//System.out.println("Aggregating...");
		int sum = 0;
		for (Serializable result : results) {
			sum += ((Integer) result).intValue();
		}
		//System.out.println("Aggregating...Done");
		return sum;
	}

}
