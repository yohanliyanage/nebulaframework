package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;

public class SciMark2LUBenchmark extends AbstractScimark2Job {

	
	private static final long serialVersionUID = -8672987206565679456L;

	public SciMark2LUBenchmark(int numTasks) {
		super(BenchmarkType.LU, numTasks);
	}

	public SciMark2LUBenchmark(long cyclesPerTask,
			int numTasks) {
		super(BenchmarkType.LU, cyclesPerTask, numTasks);
	}

	public SciMark2LUBenchmark() {
		super(BenchmarkType.LU);
	}

	@Override
	public BenchmarkResult aggregate(List<? extends Serializable> results) {
		sw.stop();
		double time = sw.read();
		
		int tasks = results.size();
		
		// Calculate MFLOPS
		double mflops =	CalculationSupport.getLU(cyclesPerTask*tasks, time);
		
		BenchmarkResult result = new BenchmarkResult(type, mflops, time);
		result.setCyclesPerTask(cyclesPerTask);
		result.setTasks(tasks);
		
		return result;
	}

}
