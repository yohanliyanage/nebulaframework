package org.nebulaframework.ui.swing.ClusterManager;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClusterManagerStarter {

	private static Log log = LogFactory.getLog(ClusterManagerStarter.class);
	
	/**
	 * Starts execution of a ClusterManager instance on the
	 * system.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {
		Properties props = null; 
		
		// Try to detect and load any given CMD LINE arguments
		try {
			props = parseArguments(args);
		} catch (IllegalArgumentException e) {
			log.error("Invalid Command Line Arguments : " + e.getMessage());
		}
		
		
	}

	private static Properties parseArguments(String[] args) throws IllegalArgumentException {
		
		// TODO Implement
		return new Properties();
	}

}
