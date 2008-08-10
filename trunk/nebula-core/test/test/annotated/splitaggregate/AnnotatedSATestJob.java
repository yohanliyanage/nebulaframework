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
package test.annotated.splitaggregate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.nebulaframework.core.job.annotations.Task;
import org.nebulaframework.core.job.annotations.splitaggregate.Aggregate;
import org.nebulaframework.core.job.annotations.splitaggregate.Split;
import org.nebulaframework.core.job.annotations.splitaggregate.SplitAggregateJob;

/**
 * GridJob Implementation for Testing
 * @author Yohan Liyanage
 *
 */
@SplitAggregateJob
public class AnnotatedSATestJob implements Serializable {

	private static final long serialVersionUID = -4504183475114576465L;

	
	@Split
	public List<Serializable> makeNumbers() {
		List<Serializable> tasks = new ArrayList<Serializable>();
		
		for(int i=0; i < 50; i++) {
			tasks.add(this);
		}
		
		return tasks;
	}

	@Aggregate
	public Integer sumResults(List <Serializable> results) {
		int sum = 0;
		for (Serializable result : results) {
			sum += ((Integer) result).intValue();
		}
		return sum;
	}

	@Task
	public Integer makeRandomNumber() {
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return new Random().nextInt(100);
	}
}