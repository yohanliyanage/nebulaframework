package org.nebulaframework.benchmark.scimark2.grid;

import org.nebulaframework.benchmark.scimark2.grid.jobs.SciMark2FFTBenchmark;
import org.nebulaframework.benchmark.scimark2.grid.jobs.SciMark2LUBenchmark;
import org.nebulaframework.benchmark.scimark2.grid.jobs.SciMark2MCBenchmark;
import org.nebulaframework.benchmark.scimark2.grid.jobs.SciMark2SORBenchmark;
import org.nebulaframework.benchmark.scimark2.grid.jobs.SciMark2SparseBenchmark;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;

public class SciMark2App {
	
	public static void main(String[] args) throws Exception {
		
		GridNode node = Grid.startLightGridNode();
		
		final int TASKS = 10;
		
		GridJobFuture future;
		BenchmarkResult result;
		
		double mflops_sum = 0;
		
		// FFT
		future = node.getJobSubmissionService().submitJob(new SciMark2FFTBenchmark(TASKS));
		result = (BenchmarkResult) future.getResult();
		System.err.println("FFT : " + result);
		mflops_sum += result.getMflops();
		
		// SOR
		future = node.getJobSubmissionService().submitJob(new SciMark2SORBenchmark(TASKS));
		result = (BenchmarkResult) future.getResult();
		System.err.println("SOR : " + result);
		mflops_sum += result.getMflops();
		
		// Monte Carlo
		future = node.getJobSubmissionService().submitJob(new SciMark2MCBenchmark(TASKS));
		result = (BenchmarkResult) future.getResult();
		System.err.println("Monte Carlo : " + result);
		mflops_sum += result.getMflops();
		
		// Sparse
		future = node.getJobSubmissionService().submitJob(new SciMark2SparseBenchmark(TASKS));
		result = (BenchmarkResult) future.getResult();
		System.err.println("Sparse : " + result);
		mflops_sum += result.getMflops();
		
		// LU
		future = node.getJobSubmissionService().submitJob(new SciMark2LUBenchmark(TASKS));
		result = (BenchmarkResult) future.getResult();
		System.err.println("LU : " + result);
		mflops_sum += result.getMflops();
		
		
		System.err.println("Composite Result : " + mflops_sum / 5);
		
		node.shutdown();
		System.exit(0);
	}
}
