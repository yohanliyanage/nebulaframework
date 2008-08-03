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
package org.nebulaframework.ui.swing.cluster;

import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;

/**
 * Execution Point for Swing UI based ClusterManager.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
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
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JWindow splash = ClusterMainUI.showSplash();
	
		try {
			Grid.startClusterManager();
		} catch (Exception e) {
			splash.setVisible(false);
			log.error("Exception while starting",e);
			
			JOptionPane.showMessageDialog(null, "Unable to start ClusterManager due to Exception." +
					"\nSee StackTrace (log) for details", "Nebula Cluster", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		ClusterMainUI ui = ClusterMainUI.create();
		ui.setVisible(true);
		splash.setVisible(false);
		splash.dispose();
		log.info("[UI] Initialized");
	}



}
