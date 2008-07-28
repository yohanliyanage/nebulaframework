package org.nebulaframework.grid.cluster.manager.services.jobs;

// TODO FixDoc
public interface JobCancellationCallback {
	
	/**
	 * Cancels this {@code JobExecutionManager}, as the
	 * {@code GridJob} is canceled.
	 * 
	 * @return if canceled, {@code true}, if failed, {@code false}.
	 */
	public boolean cancel();
}
