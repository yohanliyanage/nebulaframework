package test.nebulaframework.simpleTest;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;

public class TestUnboundedJob implements UnboundedGridJob<Integer, Serializable> {

	private static final long serialVersionUID = 7973628989182769533L;
	private static Log log = LogFactory.getLog(TestUnboundedJob.class);
	
	public Serializable processResult(Serializable result) {
		log.info("FROM PROCESS RESULT : " + result);
		return result;
	}

	public GridTask<Integer> task() {
		return new TestTask();
	}

}
