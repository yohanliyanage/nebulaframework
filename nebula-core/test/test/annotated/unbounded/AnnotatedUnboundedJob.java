package test.annotated.unbounded;

import java.io.Serializable;
import java.util.Random;

import org.nebulaframework.core.job.annotations.Task;
import org.nebulaframework.core.job.annotations.unbounded.TaskGenerator;
import org.nebulaframework.core.job.annotations.unbounded.UnboundedJob;

@UnboundedJob(postProcess=false)
public class AnnotatedUnboundedJob implements Serializable{

	private static final long serialVersionUID = 1L;

	@TaskGenerator
	public AnnotatedUnboundedJob createTask() {
		return this;
	}

	@Task
	public Integer guessNumber() {
		return new Random().nextInt(100);
	}
	
	
	
}
