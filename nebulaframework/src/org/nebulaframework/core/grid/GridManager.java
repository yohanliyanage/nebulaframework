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

import java.util.List;

/**
 * GridManager API Interface. A GridManager manages a 
 * local grid and supports node addition and removal, etc.
 * 
 * @author Yohan Liyanage
 *
 */
public interface GridManager {

	public void registerNode(GridNode node);
	public void unregisterNode(GridNode node);
	public List<GridNode> getNodes();
}
