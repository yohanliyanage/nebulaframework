package org.nebulaframework.core.grid.cluster.node.delegate;

import java.util.UUID;

import org.nebulaframework.deployment.classloading.node.exporter.GridNodeClassExporter;

public class GridNodeDelegate {

	private UUID nodeId;
	private GridNodeClassExporter classExporter;

	
	public GridNodeDelegate(UUID nodeId) {
		super();
		this.nodeId = nodeId;
	}

	public GridNodeClassExporter getClassExporter() {
		return classExporter;
	}

	public void setClassExporter(GridNodeClassExporter classExporter) {
		this.classExporter = classExporter;
	}

	public UUID getNodeId() {
		return nodeId;
	}

}
