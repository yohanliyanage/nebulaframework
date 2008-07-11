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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GridNodeInfo Implementation. Holds information regarding a node of Grid.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridNodeProfileImpl implements Serializable, GridNodeProfile {

	private static final long serialVersionUID = -7303863519997272578L;

	private static Log log = LogFactory.getLog(GridNodeProfileImpl.class);

	private InetAddress ipAddress;	// IP Address
	private String name;			// System Name
	private String processor;		// Processor
	private double clockSpeed;		// Processor ClockSpeed (MHz)
	private double memory;			// RAM (MB)
	private String OS;				// Operating System

	/**
	 * Constructor. Detects IPAddress through {@code InetAddress}.
	 * 
	 */
	public GridNodeProfileImpl() {
		super();
		detectNodeInfo();
	}

	private void detectNodeInfo() {
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.warn("[GridNodeProfile] Unable to resolve local IP Address");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets user-friendly name for Node
	 * 
	 * @param name
	 *            Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProcessor() {
		return processor;
	}

	/**
	 * Sets Processor Information String
	 * 
	 * @param processor
	 *            Processor Info
	 */
	public void setProcessor(String processor) {
		this.processor = processor;
	}

	/**
	 * {@inheritDoc}
	 */
	public double getClockSpeed() {
		return clockSpeed;
	}

	/**
	 * Sets ClockSpeed of Processor in MHz
	 * 
	 * @param clockSpeed
	 *            Processor FSB in MHz
	 */
	public void setClockSpeed(double clockSpeed) {
		this.clockSpeed = clockSpeed;
	}

	/**
	 * {@inheritDoc}
	 */
	public double getMemory() {
		return memory;
	}

	/**
	 * Sets total RAM memory available in MBs
	 * 
	 * @param memory
	 *            Memory Size in MB
	 */
	public void setMemory(double memory) {
		this.memory = memory;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOS() {
		return OS;
	}

	/**
	 * Sets the Operating System Name
	 * 
	 * @param os
	 *            OS Name
	 */
	public void setOS(String os) {
		OS = os;
	}

	/**
	 * {@inheritDoc}.
	 * <p>
	 * {@code String} representation format is as follows:
	 * <code>
	 * <i>Name</i>|<i>IPAddress</i>|<i>Processor</i>@<i>ClockSpeed</i>MHz|<i>Memory</i>MB|<i>OS</i>
	 * </code> 
	 */
	@Override
	public String toString() {
		return name + "|" + ipAddress + "|" + processor + "@"
				+ clockSpeed + "MHz|" + memory + "MB|" + OS;
	}

	/**
	 * Returns the IP Address of this {@code GridNode}.
	 * 
	 * @return {@code InetAddress} IP Address
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}

}
