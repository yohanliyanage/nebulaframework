package org.nebulaframework.benchmark.scimark.grid;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.benchmark.scimark.grid.jobs.BenchmarkJob;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

//TODO Remove
public class BenchmarkSingleExecutor {
	
	private static Log log = LogFactory.getLog(BenchmarkSingleExecutor.class);
	
	public static void main(String[] args) {

		
		try {

			log.info("Benchmark GridNode Starting...");
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
			log.debug("Submitting Benchmark Job");
			future = node.getJobSubmissionService().submitJob(new BenchmarkJob());
			
			double result = 0;
			try {
				result = (Double) future.getResult();
				log.debug("Grid MLFOPS : " + result);
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			
			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis() + " ms");
			
			log.debug("Press any key to unregister GridNode and terminate");
			System.in.read();
			node.getNodeRegistrationService().unregister();
			
			log.info("Unregistered, Terminating...");
			System.exit(0);
			
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
