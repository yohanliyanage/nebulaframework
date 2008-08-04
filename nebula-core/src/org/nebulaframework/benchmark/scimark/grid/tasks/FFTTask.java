package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

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
