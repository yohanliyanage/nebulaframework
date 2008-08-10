package test.annotated.unbounded;

import java.io.Serializable;

import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;

public class AnnotationUnboundedTest {

	public static void main(String[] args) {
		
		Grid.startLightGridNode();
		
		AnnotatedUnboundedJob job = new AnnotatedUnboundedJob();
		
		GridNode.getInstance().getJobSubmissionService().submitJob(job, new ResultCallback() {

			@Override
			public void onResult(Serializable result) {
				System.err.println(result);
			}
			
		});
		
	}
}
