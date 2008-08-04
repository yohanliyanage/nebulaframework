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
package org.nebulaframework.benchmark.scimark.grid;

import java.io.Serializable;

import org.nebulaframework.core.task.ExecutionTimeAware;

/**
 * SciMark Benchmark Job Result Wrapper.
 * <p>
 * Implements ExecutionTimeAware interface to 
 * indicate that this result should be injected
 * with the time taken for execution
 * at a GridNode for this task, by the framework.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ExecutionResult implements Serializable, ExecutionTimeAware {

	private static final long serialVersionUID = 8766052176725712967L;
	
	private BenchmarkType type;		// Type of benchmark
	private double calculationTime;	// Time for calculation of result
	private double executionTime;	// Time taken for complete task
	private long cycles;			//  Number of cycles executed

	public BenchmarkType getType() {
		return type;
	}

	public void setType(BenchmarkType type) {
		this.type = type;
	}

	public double getCalculationTime() {
		return calculationTime;
	}

	public void setCalculationTime(double calculationTime) {
		this.calculationTime = calculationTime;
	}

	public long getCycles() {
		return cycles;
	}

	public void setCycles(long cycles) {
		this.cycles = cycles;
	}

	public double getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = ((double)executionTime) / 1000;
	}

}
