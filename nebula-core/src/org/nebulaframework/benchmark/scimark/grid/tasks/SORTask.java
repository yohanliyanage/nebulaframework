package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

public class SORTask extends AbstractBenchmarkTask {

	private static final long serialVersionUID = 2187150481888763481L;
	
	private Random random;
	
	
	public SORTask(Random random) {
		super();
		this.random = random;
	}


	@Override
	public ExecutionResult doExecute() throws Exception {
		return CustomKernel.measureSOR(random);
	}

}
