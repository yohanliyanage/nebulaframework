package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;

public class SciMark2SORBenchmark extends AbstractScimark2Job {

	
	private static final long serialVersionUID = -3333062388176954288L;

	public SciMark2SORBenchmark(int numTasks) {
		super(BenchmarkType.SOR, numTasks);
	}

	public SciMark2SORBenchmark(long cyclesPerTask,
			int numTasks) {
		super(BenchmarkType.SOR, cyclesPerTask, numTasks);
	}

	public SciMark2SORBenchmark() {
		super(BenchmarkType.SOR);
	}

	@Override
	public BenchmarkResult aggregate(List<? extends Serializable> results) {
		sw.stop();
		double time = sw.read();
		
		int tasks = results.size();
		
		// Calculate MFLOPS
		double mflops =	CalculationSupport.getSOR(cyclesPerTask*tasks, time);
		
		BenchmarkResult result = new BenchmarkResult(type, mflops, time);
		result.setCyclesPerTask(cyclesPerTask);
		result.setTasks(tasks);
		
		return result;
	}

}
