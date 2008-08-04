package org.nebulaframework.benchmark;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.benchmark.linpack.LinPackBenchmark;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

/**
 * LinPack Benchmark Executor. This class can be executed to submit a 
 * LinPack benchmark job on the Grid and to obtain the results.
 * <p>
 * Note that this is a stand-alone console application, and it creates a 
 * light-weight job submission GridNode.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class LinPackBechmarkNode {
	
	private static Log log = LogFactory.getLog(LinPackBechmarkNode.class);

	public static void main(String[] args) {

		try {

			log.info("LinPack Benchmark GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();

			GridNode node = Grid.startLightGridNode();

			log.info("GridNode ID : " + node.getId());

			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis()
					+ " ms]");

			sw.start();

			GridJobFuture future = null;

			log.debug("Submitting LinPack Benchmark Job");

			// Submit Job
			future = node.getJobSubmissionService()
					.submitJob(new LinPackBenchmark(200,5));

			double result = 0;
			try {
				result = (Double) future.getResult();
				System.err.println("*********************************************");
				System.err.println("LinPack Benchmark Grid MLFOPS : " + result);
				System.err.println("*********************************************");
				
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}

			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis()
					+ " ms");

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
