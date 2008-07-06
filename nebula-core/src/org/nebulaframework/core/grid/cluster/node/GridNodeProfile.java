/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

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