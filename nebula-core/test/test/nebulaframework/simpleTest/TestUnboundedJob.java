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

package test.nebulaframework.simpleTest;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.unbounded.UnboundedGridJob;
import org.nebulaframework.core.task.GridTask;

public class TestUnboundedJob implements UnboundedGridJob<Integer> {

	private static final long serialVersionUID = 7973628989182769533L;
	private static Log log = LogFactory.getLog(TestUnboundedJob.class);
	
	public Serializable processResult(Serializable result) {
		log.info("FROM PROCESS RESULT : " + result);
		return result;
	}

	public GridTask<Integer> task() {
		return new TestTask();
	}

}
