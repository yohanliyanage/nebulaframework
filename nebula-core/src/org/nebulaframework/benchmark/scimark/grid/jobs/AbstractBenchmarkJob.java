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
package org.nebulaframework.benchmark.scimark.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.Constants;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;

/**
 * Abstract Benchmark GridJob for SciMark Benchmark Jobs.
 * <p>
 * Provides common functionality of SciMark jobs.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public abstract class AbstractBenchmarkJob implements SplitAggregateGridJob<ExecutionResult, Double> {

	private static final long serialVersionUID = 6956932641722913717L;
	
	// SciMark Randomizer
	protected Random random =  new Random(Constants.RANDOM_SEED);
	
	// Tasks per Job (should be higher than node count 
	// for optimal results)
	protected int taskCount = 100;
	

	public AbstractBenchmarkJob() {
		super();
	}

	public AbstractBenchmarkJob(int taskCount) {
		super();
		this.taskCount = taskCount;
	}

	@Override
	public abstract List<GridTask<ExecutionResult>> split();



	@Override
	public abstract Double aggregate(List<? extends Serializable> results);


}
