package org.nebulaframework.core.grid;

import java.util.List;

public interface GridManager {

	public void registerNode(GridNode node);
	public void unregisterNode(GridNode node);
	public List<GridNode> getNodes();
}
