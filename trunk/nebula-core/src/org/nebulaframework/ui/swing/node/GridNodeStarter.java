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
package org.nebulaframework.ui.swing.node;

import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.discovery.DiscoveryFailureException;
import org.nebulaframework.grid.Grid;

/**
 * Execution Point for Swing UI based GridNode.
 * Note that it is possible to start a GridNode
 * with optional {@code -silent} argument, which is 
 * suitable for setting up GridNodes to start up
 * along with the O/S.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class GridNodeStarter {

	private static final Log log = LogFactory.getLog(GridNodeStarter.class);
	private static boolean silentMode = false;

	/**
	 * Starts execution of a ClusterManager instance on the
	 * system.
	 * <p>
	 * Note that it is possible to start a GridNode
	 * with optional {@code -silent} argument, which is 
	 * suitable for setting up GridNodes to start up
	 * along with the O/S.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {

		//Log4JConfiguration.configure();
		
		// Process Arugments
		processArgs(args);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JWindow splash = null;
		if (!silentMode) {
			splash = NodeMainUI.showSplash();
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

	/**
	 * Processes the Command Line Arguments Supplied
	 * 
	 * @param args agruments
	 */
	private static void processArgs(String[] args) {
		
		// If no args
		if (args.length==0) return;
		
		// If silent mode
		if (args[0].equals("-silent")) {
			silentMode = true;
		}
	}
}
