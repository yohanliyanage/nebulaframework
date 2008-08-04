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
package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

/**
 * FFT Benchmark Task.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class FFTTask extends AbstractBenchmarkTask {

	private static final long serialVersionUID = 9095871398482560613L;
	
	private Random random;
	
	public FFTTask(Random random) {
		super();
		this.random = random;
	}


	@Override
	public ExecutionResult doExecute() throws Exception {
		return CustomKernel.measureFFT(random);
	}

}
