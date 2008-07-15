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
package org.nebulaframework.grid.service.event;

/**
 * Represents a {@code Callback} which will be invoked by
 * {@code ServiceEventsSupport}, for a given {@code ServiceEvent}.
 * <p>
 * This may be utilized to specify code which is to be
 * executed at the time of a certain {@code ServiceEvent}.

 * @author Yohan Liyanage
 * @version 1.0
 */
public interface ServiceHookCallback {
	
	/**
	 * This method will be invoked by the {@code ServiceEventsSupport}
	 * when the {@code ServiceEvent} triggers. This should be overridden
	 * to provide code which is to be executed.
	 */
	public void onServiceEvent();
}
