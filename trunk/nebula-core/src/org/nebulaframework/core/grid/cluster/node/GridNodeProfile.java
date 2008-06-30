package org.nebulaframework.core.grid.cluster.node;

import java.net.InetAddress;


/**
 * Holds information regarding a node of Grid.
 * @author Yohan Liyanage
 *
 */
public interface GridNodeProfile {

	/**
	 * Returns user-friendly name for Node
	 * @return String name
	 */
	public abstract String getName();

	/**
	 * Returns Processor Information
	 * @return String Processor Information
	 */
	public abstract String getProcessor();

	/**
	 * Returns ClockSpeed of Processor (FSB) in MHz
	 * @return double ClockSpeed in MHz
	 */
	public abstract double getClockSpeed();

	/**
	 * Returns total RAM memory available in MBs
	 * @return Memory Size in MB
	 */
	public abstract double getMemory();
	
	/**
	 * Returns the Operating System Name
	 * @return OS Name
	 */
	public abstract String getOS();

	/**
	 * Returns the IP Address of Node
	 * @return InetAddress IP Address
	 */
	public abstract InetAddress getIpAddress();
}