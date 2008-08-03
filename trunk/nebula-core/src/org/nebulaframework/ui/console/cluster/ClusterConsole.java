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
package org.nebulaframework.ui.console.cluster;

import org.nebulaframework.grid.Grid;

/**
 * Console Execution Point for Nebula ClusterManager.
 * <p>
 * Note that this class provides only minimal
 * functionality. Advanced users are advised to
 * use the API methods to create their own classes,
 * specially for embedded execution situations.
 * <p>
 * If the requirement is to run the framework
 * as stand-alone application, consider using 
 * Swing UIs of the framework.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ClusterConsole {

	/**
	 * Console Execution Point
	 * @param args arguments
	 */
	public static void main(String[] args) {
		Grid.startClusterManager();
	}
}
