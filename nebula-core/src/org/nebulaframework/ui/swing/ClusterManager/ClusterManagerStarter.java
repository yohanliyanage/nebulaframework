package org.nebulaframework.ui.swing.ClusterManager;

import javax.swing.JOptionPane;
import javax.swing.JWindow;
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

		try {
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JWindow splash = ClusterMainUI.showSplash();
	
		try {
			Grid.startClusterManager();
		} catch (Exception e) {
			splash.setVisible(false);
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unable to start ClusterManager due to Exception." +
					"\nSee StackTrace for details", "Nebula Cluster", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		ClusterMainUI ui = ClusterMainUI.create();
		ui.setVisible(true);
		splash.setVisible(false);
		splash.dispose();
		log.info("[UI] Initialized");
	}



}
