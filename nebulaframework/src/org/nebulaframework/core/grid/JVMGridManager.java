package org.nebulaframework.core.grid;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.JobExecutor;

/**
 * Manages a Single JVM Grid (Testing Purposes Only)
 * @author Yohan
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
	public void registerNode(GridNode node) {
		log.debug("Registering node " + node.getName());
		this.nodes.add(node);
		JobExecutor.addJobListener(node);
	}

	@Override
	public void unregisterNode(GridNode node) {
		this.nodes.remove(node);
	}

	@Override
	public List<GridNode> getNodes() {
		return this.nodes;
	}
	
	
}
