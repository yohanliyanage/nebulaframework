package org.nebulaframework.core.job;


public interface JobQueue {
	
	public void add(RemoteGridJob job);

	public boolean remove(RemoteGridJob job);

	public RemoteGridJob nextJob();
}
