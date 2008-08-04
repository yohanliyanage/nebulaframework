package org.nebulaframework.benchmark.linpack;

import org.nebulaframework.core.task.GridTask;
import org.nebulaframework.grid.GridExecutionException;

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


	@Override
	public LinPackResult execute() throws GridExecutionException {
		return new Linpack().runBenchmark(cycles);
	}

}
