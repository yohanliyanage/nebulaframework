package org.nebulaframework.benchmark.scimark2.grid.jobs;

import java.io.Serializable;
import java.util.List;

import org.nebulaframework.benchmark.scimark.grid.BenchmarkType;
import org.nebulaframework.benchmark.scimark2.grid.BenchmarkResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;

public class SciMark2SparseBenchmark extends AbstractScimark2Job {

	private static final long serialVersionUID = 3417392745206482472L;

	public SciMark2SparseBenchmark(int numTasks) {
		super(BenchmarkType.SPARSE, numTasks);
	}

	public SciMark2SparseBenchmark(long cyclesPerTask,
			int numTasks) {
		super(BenchmarkType.SPARSE, cyclesPerTask, numTasks);
	}

	public SciMark2SparseBenchmark() {
		super(BenchmarkType.SPARSE);
	}

	@Override
	public BenchmarkResult aggregate(List<? extends Serializable> results) {
		sw.stop();
		double time = sw.read();
		
		int tasks = results.size();
		
		// Calculate MFLOPS
		double mflops =	CalculationSupport.getSparse(cyclesPerTask*tasks, time);
		
		BenchmarkResult result = new BenchmarkResult(type, mflops, time);
		result.setCyclesPerTask(cyclesPerTask);
		result.setTasks(tasks);
		
		return result;
	}

}
