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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.util.StopWatch;

public class TestNodeWorker {
	
	private static Log log = LogFactory.getLog(TestNodeWorker.class);
	
	public static void main(String[] args) {

		try {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			
			// Start Grid Node
			GridNode node = Grid.startGridNode();
			
			sw.stop();
			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
			
			log.info("GridNode ID : " + node.getId());
			
			try {
				node.getNodeRegistrationService().register();
				log.info("Registered in Cluster : " + node.getNodeRegistrationService().getRegistration().getClusterId());
			} catch (RuntimeException e) {
				System.err.println("Exception");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			log.debug("Press any key to unregister GridNode and terminate");
			System.in.read();
			node.getNodeRegistrationService().unregister();
			
			log.info("Unregistered, Terminating...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
