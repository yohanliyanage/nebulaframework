package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;

public class SciMark2MCBenchmark extends AbstractScimark2Job{

	private static final long serialVersionUID = 7116169032836337746L;

	public SciMark2MCBenchmark(int numTasks) {
		super(BenchmarkType.MONTE_CARLO, numTasks);
	}

	public SciMark2MCBenchmark(long cyclesPerTask,
			int numTasks) {
		super(BenchmarkType.MONTE_CARLO, cyclesPerTask, numTasks);
	}

	public SciMark2MCBenchmark() {
		super(BenchmarkType.MONTE_CARLO);
	}

	@Override
	public BenchmarkResult aggregate(List<? extends Serializable> results) {
		sw.stop();
		double time = sw.read();
		
		int tasks = results.size();
		
		// Calculate MFLOPS
		double mflops =	CalculationSupport.getMonteCarlo(cyclesPerTask*tasks, time);
		
		BenchmarkResult result = new BenchmarkResult(type, mflops, time);
		result.setCyclesPerTask(cyclesPerTask);
		result.setTasks(tasks);
		
		return result;
	}

}
