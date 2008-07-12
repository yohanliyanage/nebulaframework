package org.nebulaframework.core.job.unbounded;

import java.io.Serializable;

import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.task.GridTask;

// TODO Fix Doc, Extend Common GridJob interface | ServerSide stuff here
public interface UnboundedGridJob<T extends Serializable, R extends Serializable> extends GridJob<T, R> {
	
	// Note : These methods will not be invoked on same thread, so use object.wait() based
	// mechanisms to communicate between task() and processResult()
	
	// TODO FixDoc
	public GridTask<T> task();
	
	// Invoked on each Result, may b used to update state to generate
	// future tasks using these results
	// TODO FixDoc
	public Serializable processResult(Serializable result);
	
}
