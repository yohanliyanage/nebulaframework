package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.Random;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

public class SparseTask extends AbstractBenchmarkTask {

	private static final long serialVersionUID = 4634409310904872389L;

	private Random random;
	
	public SparseTask(Random random) {
		super();
		this.random = random;
	}


	@Override
	public ExecutionResult doExecute() throws Exception {
		return CustomKernel.measureSparseMatmult(random);
	}

}
