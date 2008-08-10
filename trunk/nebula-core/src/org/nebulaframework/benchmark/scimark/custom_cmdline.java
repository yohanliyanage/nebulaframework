/*
 * The copyright of these materials are subject to the
 * original authors.
 * 
 * This code is distributed freely.
 */
package org.nebulaframework.benchmark.scimark;

import org.nebulaframework.benchmark.scimark.grid.ExecutionResult;
import org.nebulaframework.benchmark.scimark2.grid.CalculationSupport;


/**
	SciMark2: A Java numerical benchmark measuring performance
	of computational kernels for FFTs, Monte Carlo simulation,
	sparse matrix computations, Jacobi SOR, and dense LU matrix
	factorizations.  


*/
public class custom_cmdline
{

  /* Benchmark 5 kernels with individual Mflops.
	 "results[0]" has the average Mflop rate.

  */


	public static void main(String args[]) throws Exception
	{

		// run the benchmark
        ExecutionResult r = null;
		double res[] = new double[6];
		Random R = new Random(Constants.RANDOM_SEED);

		Stopwatch sw = new Stopwatch();
		sw.start();
		r = CustomKernel.measureFFT(R);
		res[1] = CalculationSupport.getFFT(r.getCycles(), r.getCalculationTime());
		sw.stop();
		
		System.out.println("FFT : " + sw.read());
		sw.reset();
		
		sw.start();
		r = CustomKernel.measureSOR(R);
		res[2] = CalculationSupport.getSOR(r.getCycles(), r.getCalculationTime());
		sw.stop();

		System.out.println("SOR : " + sw.read());
		sw.reset();

		sw.start();
		r = CustomKernel.measureMonteCarlo();
		res[3] = CalculationSupport.getMonteCarlo(r.getCycles(), r.getCalculationTime());
		sw.stop();
		
		System.out.println("Monte Carlo : " + sw.read());
		sw.reset();

		sw.start();
		r = CustomKernel.measureSparseMatmult(R);
		res[4] = CalculationSupport.getSparse(r.getCycles(), r.getCalculationTime());
		sw.stop();

		System.out.println("Sparse : " + sw.read());
		sw.reset();

		sw.start();
		r = CustomKernel.measureLU(R);
		res[5] = CalculationSupport.getLU(r.getCycles(), r.getCalculationTime());
		sw.stop();
		
		System.out.println("LU : " + sw.read());
		sw.reset();


		res[0] = (res[1] + res[2] + res[3] + res[4] + res[5]) / 5;


	    // print out results

		System.out.println();
		System.out.println("SciMark 2.0a");
		System.out.println();
		System.out.println("Composite Score: " + res[0]);
		System.out.print("FFT ");
		if (res[1]==0.0)
			System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
		else
			System.out.println(res[1]);

		System.out.println("SOR "+ "  " + res[2]);
		System.out.println("Monte Carlo : " + res[3]);
		System.out.println("Sparse matmult : " + res[4]);
		System.out.print("LU : ");
		if (res[5]==0.0)
			System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
		else
			System.out.println(res[5]);


	}
  
}
