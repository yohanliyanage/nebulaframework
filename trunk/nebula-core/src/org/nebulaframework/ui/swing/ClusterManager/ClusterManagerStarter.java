package org.nebulaframework.ui.swing.ClusterManager;

import java.util.Properties;

import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;

public class ClusterManagerStarter {

	private static Log log = LogFactory.getLog(ClusterManagerStarter.class);
	
	/**
	 * Starts execution of a ClusterManager instance on the
	 * system.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {
//		Properties props = null; 
//		
//		// Try to detect and load any given CMD LINE arguments
//		try {
//			props = parseArguments(args);
//		} catch (IllegalArgumentException e) {
//			log.error("Invalid Command Line Arguments : " + e.getMessage());
//		}
//		
		try {
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Grid.startClusterManager();
		ClusterMainUI.create();
	}

	private static Properties parseArguments(String[] args) throws IllegalArgumentException {
		
		// TODO Implement
		return new Properties();
	}

}
