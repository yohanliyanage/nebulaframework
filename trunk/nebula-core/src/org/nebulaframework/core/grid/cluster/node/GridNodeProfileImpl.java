package org.nebulaframework.core.grid.cluster.node;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GridNodeInfo Implementation Class
 * @author Yohan Liyanage
 *
 */
public class GridNodeProfileImpl implements Serializable, GridNodeProfile {

	private static final long serialVersionUID = -7303863519997272578L;

	private static Log log = LogFactory.getLog(GridNodeProfileImpl.class);
	
	private InetAddress ipAddress;
	private String name;
	private String processor;
	private double clockSpeed;
	private double memory;
	private String OS;
	
	
	public GridNodeProfileImpl() {
		super();
		 try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.warn("Unable to resolve local IP Address");
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
	 * @param name Name
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
	 * @param processor Processor Info
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
	 * @param clockSpeed Processor FSB in MHz
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
	 * @param memory Memory Size in MB
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
	 * @param os OS Name
	 */
	public void setOS(String os) {
		OS = os;
	}

	@Override
	public String toString() {
		return ipAddress + " => " + name + " [" + processor + " @ " + clockSpeed + "MHz " + memory + "MB - "+ OS + "]";
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}
	
}
