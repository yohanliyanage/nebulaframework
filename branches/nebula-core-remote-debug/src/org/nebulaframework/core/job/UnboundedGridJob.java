package org.nebulaframework.core.job;

import java.io.Serializable;

import org.nebulaframework.core.task.GridTask;

// TODO Fix Doc, Extend Common GridJob interface
public interface UnboundedGridJob<T extends GridTask<R>, R extends Serializable> extends Serializable{
	
	// Invoked repetitively for each task, don't use instance/class variables
	// to keep state
	// TODO FixDoc
	public T task();
	
	// Invoked on each Result
	// TODO FixDoc
	public void onResult(R result);
	
}
