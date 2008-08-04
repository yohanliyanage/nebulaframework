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

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

/**
 * LinPack Benchmark Test Task.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class LinPackTask implements GridTask<LinPackResult>{

	private static final long serialVersionUID = -5670003743368069061L;
	
	private int cycles = 1;
	
	
	public LinPackTask() {
		super();
	}


	public LinPackTask(int cycles) {
		super();
		this.cycles = cycles;
	}


	/**
	 * Executes the LinPack benchmark for given number of
	 * cycles on the GridNode.
	 * 
	 * @return results of benchmarks
	 */
	@Override
	public LinPackResult execute() throws GridExecutionException {
		return new Linpack().runBenchmark(cycles);
	}

}
