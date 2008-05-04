package test.nebulaframework.simpleTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridJobFuture;
import org.nebulaframework.core.grid.GridNode;
import org.nebulaframework.core.grid.JVMGridManager;
import org.nebulaframework.core.job.JobManager;

public class TestRunner {
	
	private static Log log = LogFactory.getLog(TestRunner.class);
	public static int sum = 0;
	
	public static void main(String[] args) {
		
		//Create Some Fake Nodes
		JVMGridManager.getInstance().registerNode(new GridNode("A"));
		JVMGridManager.getInstance().registerNode(new GridNode("B"));
		JVMGridManager.getInstance().registerNode(new GridNode("C"));
		//JVMGridManager.getInstance().registerNode(new GridNode("D"));
		
		//Submit Task
		GridJobFuture future = JobManager.getInstance().start(new TestJob());
		
		try {
			log.info("FINAL RESULT : " + future.getResult());
			log.info("EXPECTED RESULT : " + TestRunner.sum);
		} catch (GridExecutionException e) {
			e.printStackTrace();
		}
	}
}
