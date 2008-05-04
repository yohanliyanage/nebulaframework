package test.nebulaframework.simpleTest;

import java.util.Random;

import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridTask;

public class TestTask implements GridTask<Integer>{

	private static final long serialVersionUID = -4826864297461445244L;

	@Override
	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer execute() throws GridExecutionException {
		Integer val = new Random().nextInt(100);
		System.out.println("Got Random Value : " + val);
		TestRunner.sum += val;
		return val;
	}

}
