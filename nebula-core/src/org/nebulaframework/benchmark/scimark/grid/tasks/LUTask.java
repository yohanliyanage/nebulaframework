package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

public class LUTask extends AbstractBenchmarkTask {

	private static final long serialVersionUID = 3439560473545351470L;
	
	private Random random;
	
	
	public LUTask(Random random) {
		super();
		this.random = random;
	}


	@Override
	public ExecutionResult doExecute() throws Exception {
		return CustomKernel.measureLU(random);
	}

}
