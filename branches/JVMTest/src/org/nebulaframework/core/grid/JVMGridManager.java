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
package org.nebulaframework.core.grid;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.JobExecutor;

/**
 * Manages a Single JVM Grid (Testing Purposes Only)
 * @author Yohan Liyanage
 *
 */
public class JVMGridManager implements GridManager {

	private static Log log = LogFactory.getLog(JVMGridManager.class);
	private static JVMGridManager instance = new JVMGridManager();
	private List<GridNode> nodes = new ArrayList<GridNode>();

	private JVMGridManager() {
		super();
	}
	
	public static JVMGridManager getInstance() {
		return instance;
	}

	@Override
	public synchronized void registerNode(GridNode node) {
		log.debug("Registering node " + node.getName());
		this.nodes.add(node);
		JobExecutor.addJobListener(node);
	}

	@Override
	public synchronized void unregisterNode(GridNode node) {
		this.nodes.remove(node);
	}

	@Override
	public List<GridNode> getNodes() {
		return this.nodes;
	}
	
	
}
