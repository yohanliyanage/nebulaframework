package org.nebulaframework.core.job.unbounded;

import java.io.Serializable;

import org.nebulaframework.core.task.GridTask;

// TODO Fix Doc, Extend Common GridJob interface | ServerSide stuff here
public interface UnboundedGridJob<T extends GridTask<R>, R extends Serializable> extends Serializable{
	
	// TODO FixDoc
	public T task();
	
	// Invoked on each Result
	// TODO FixDoc
	public void onResult(R result);
	
}
