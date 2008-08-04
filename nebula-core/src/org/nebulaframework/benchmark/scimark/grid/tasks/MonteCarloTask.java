package org.nebulaframework.benchmark.scimark.grid.tasks;

import org.nebulaframework.benchmark.scimark.CustomKernel;
import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;

public class MonteCarloTask extends AbstractBenchmarkTask {

	private static final long serialVersionUID = 2894090550980995684L;

	@Override
	public ExecutionResult doExecute() throws Exception {
		return CustomKernel.measureMonteCarlo();
	}

}
