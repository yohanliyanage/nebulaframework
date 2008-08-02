package org.nebulaframework.ui.swing.GridNode;

import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.discovery.DiscoveryFailureException;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.ui.swing.ClusterManager.ClusterMainUI;

public class GridNodeStarter {

	private static final Log log = LogFactory.getLog(GridNodeStarter.class);
	private static boolean silentMode = false;

	/**
	 * Starts execution of a ClusterManager instance on the
	 * system.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {

		processArgs(args);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JWindow splash = null;
		if (!silentMode) {
			splash = ClusterMainUI.showSplash();
		}
		
		try {
			Grid.startGridNode();
		} catch (DiscoveryFailureException ex) {
			log.warn("Discovery Failed");
		} catch (Exception e) {
			
			log.error("Exception while starting",e);
			e.printStackTrace();

			if (!silentMode) {
				splash.setVisible(false);
				JOptionPane.showMessageDialog(null, "Unable to start GridNode due to Exception." +
						"\nSee StackTrace (log) for details", "Nebula Grid", JOptionPane.ERROR_MESSAGE);
			}
			
			System.exit(1);
		}
		
		NodeMainUI ui = NodeMainUI.create();
		

		
		// Show if not Silent Mode
		if (!silentMode) {
			splash.setVisible(false);
			splash.dispose();
			ui.setVisible(true);
		}
		
		log.info("[UI] Initialized");
		
		if (!Grid.isNode()) {
			log.warn("[GridNode] Not connected to Cluster");
		}
	}

	private static void processArgs(String[] args) {
		
		// If no args
		if (args.length==0) return;
		
		if (args[0].equals("-silent")) {
			silentMode = true;
		}
	}
}
