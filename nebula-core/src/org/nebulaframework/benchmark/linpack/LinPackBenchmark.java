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
package org.nebulaframework.benchmark.linpack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.core.job.splitaggregate.SplitAggregateGridJob;
import org.nebulaframework.core.task.GridTask;
import org.springframework.util.StopWatch;

/**
 * LinPack Benchmark GridJob.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class LinPackBenchmark implements SplitAggregateGridJob<LinPackResult, Double> {

	private static final long serialVersionUID = 6153976750227917794L;

	private int tasks = 5;		// # of tasks deployed
	private int cycles = 1;		// # of cycles each task executed
	
	private transient StopWatch sw = null;
	
	/**
	 * No-args constructor
	 */
	public LinPackBenchmark() {
		super();
	}


	/**
	 * Specify the number of tasks to be deployed.
	 * @param tasks task count
	 */
	public LinPackBenchmark(int tasks) {
		this.tasks = tasks;
	}


	/**
	 * Specify the number of tasks to be deployed
	 * and the number of cycles each task should
	 * execute the benchmark.
	 * 
	 * @param tasks task count
	 * @param cycles cycles per task
	 */
	public LinPackBenchmark(int tasks, int cycles) {
		super();
		this.tasks = tasks;
		this.cycles = cycles;
	}


	/**
	 * Splits the benchmark tasks.
	 * 
	 * @return benchmark tasks
	 */
	@Override
	public List<GridTask<LinPackResult>> split() {
		List<GridTask<LinPackResult>> list = new ArrayList<GridTask<LinPackResult>>();
		for (int i=0; i < tasks; i++) {
			list.add(new LinPackTask(cycles));
		}
		
		sw = new StopWatch();
		
		sw.start();
		
		return list;
	}
	
	/**
	 * Calculates final MFLOPS value from results.
	 * 
	 * @param results task results
	 * @return final MFLOPS value for Grid
	 */
	@Override
	public Double aggregate(List<? extends Serializable> results) {
		
		sw.stop();
		
		// Total execution time
		double timeSecs = sw.getTotalTimeSeconds();
		
		int cycleCount = 0;
		
		for (Serializable r : results) {
			if (r instanceof LinPackResult) {
				
				// We do not check for any value from result
				// as the number of floating point operations
				// are constant for each task
				
				// A better solution is to check the normRes
				// for extreme conditions and ignore
				// such results
				
				cycleCount+=cycles;
			}
		}
		
		// Calculate final MFLOPs for Grid
		return Linpack.calculateMFLOPS(cycleCount, timeSecs);
	}



}
