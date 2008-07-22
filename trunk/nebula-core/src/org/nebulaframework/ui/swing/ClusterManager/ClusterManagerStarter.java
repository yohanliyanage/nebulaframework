package org.nebulaframework.ui.swing.ClusterManager;

import javax.swing.UIManager;

import org.nebulaframework.grid.Grid;

public class ClusterManagerStarter {

	
	/**
	 * Starts execution of a ClusterManager instance on the
	 * system.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {

		try {
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Grid.startClusterManager();
		ClusterMainUI.create();
	}



}
