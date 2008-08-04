package org.nebulaframework.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkFFT;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkLU;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkMonteCarlo;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkSOR;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkSparse;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

/**
 * SciMark Benchmark Executor. This class submits a series of
 * benchmark jobs, using the SciMark Benchmark Suite. The tests executed
 * includes,
 * <ul>
 * 	<li>Fast Fourier Transformation</li>
 * 	<li>Jacobi Successive Over Relaxation</li>
 * 	<li>Monte Carlo Method to calculate PI</li>
 * 	<li>LU Matrix Factorization</li>
 * 	<li>Sparse Matrix Multiplication</li>
 * </ul>
 * <p>
 * Note that this is a stand-alone console application, and it creates a 
 * light-weight job submission GridNode.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class SciMarkMultiJobBenchmarkNode {
	
	private static Log log = LogFactory.getLog(SciMarkMultiJobBenchmarkNode.class);
	
	// Number of Tasks Deployed per Test Job
	// (Make this number higher than expected node count for best results)
	private static final int TASKCOUNT = 5;	
	
	public static void main(String[] args) {
		
		double mflop_fft = 0;		// FFT Result
		double mflop_sor = 0;		// SOR Result
		double mflop_monte = 0;		// Monte Carlo Result
		double mflop_lu = 0;		// LU Result
		double mflop_sparse = 0;	// Sparse Result
		
		try {

			log.info("SciMark Multi-Job Benchmark GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			
			GridNode node =  Grid.startLightGridNode();
			
			log.info("GridNode ID : " + node.getId());
			
			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
			
			// Submit Job
			
			sw.start();
			
			GridJobFuture future = null;
			
			/* ------------------------------- FFT Benchmark -------------------------------- */
			log.debug("Submitting FFT Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkFFT(TASKCOUNT));
			
			try {
				mflop_fft = (Double) future.getResult();
				log.debug("FFT Results : " + mflop_fft);
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			/* ------------------------------- SOR Benchmark -------------------------------- */
			log.debug("Submitting SOR Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkSOR(TASKCOUNT));
			
			try {
				mflop_sor = (Double) future.getResult();
				log.debug("SOR Results : " + mflop_sor);
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			/* ------------------------------- Monte Carlo Benchmark -------------------------------- */
			log.debug("Submitting Monte Carlo Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkMonteCarlo(TASKCOUNT));
			
			try {
				mflop_monte = (Double) future.getResult();
				log.debug("MonteCarlo Results : " + mflop_monte);
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			/* ------------------------------- LU Benchmark -------------------------------- */
			log.debug("Submitting LU Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkLU(TASKCOUNT));
			
			try {
				mflop_lu = (Double) future.getResult();
				log.debug("LU Results : " + mflop_lu);
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			/* ------------------------------- Sparse Benchmark -------------------------------- */
			log.debug("Submitting Sparse Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkSparse(TASKCOUNT));
			
			try {
				mflop_sparse = (Double) future.getResult();
				log.debug("Sparse Results : " + mflop_sparse);
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			
			/* ------------------------------- FINAL RESULT -------------------------------- */
			double result = (mflop_fft + mflop_lu + mflop_monte + mflop_sor + mflop_sparse) /5;
			log.debug("Benchmark Result : " + result);
			
			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis() + " ms");
			
			node.getNodeRegistrationService().unregister();
			
			
			System.err.println("*******************************");
			System.err.println("   SciMark Benchmark Results");
			System.err.println("*******************************");
			System.err.println("(Tasks per Benchmark : " + TASKCOUNT + ")\n");
			System.err.printf("Fast Fourier Transform : %.2f MFLOPS\n" , mflop_fft);
			System.err.printf("Jacobi Successive Over Relaxation : %.2f MFLOPS\n" , mflop_sor);
			System.err.printf("Monte Carlo PI Calculation : %.2f MFLOPS\n" , mflop_monte);
			System.err.printf("Sparse Matrix Multiplication : %.2f MFLOPS\n" , mflop_sparse);
			System.err.printf("LU Matrix Factorization : %.2f MFLOPS\n" , mflop_lu );
			System.err.printf("\nComposite Result : %.2f MFLOPS\n" , result);
			System.exit(0);
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
