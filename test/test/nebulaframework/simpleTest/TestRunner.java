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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.GridExecutionException;
import org.nebulaframework.core.GridJobFuture;
import org.nebulaframework.core.grid.GridNode;
import org.nebulaframework.core.grid.JVMGridManager;
import org.nebulaframework.core.job.JobManager;

/**
 * Test GridJob Execution Class
 * @author Yohan Liyanage
 *
 */
public class TestRunner {
	
	private static Log log = LogFactory.getLog(TestRunner.class);
	public static int sum = 0;
	
	public static void main(String[] args) {
		
		//Create Some Fake Nodes
		JVMGridManager.getInstance().registerNode(new GridNode("A"));
		JVMGridManager.getInstance().registerNode(new GridNode("B"));
		JVMGridManager.getInstance().registerNode(new GridNode("C"));
		JVMGridManager.getInstance().registerNode(new GridNode("D"));
		JVMGridManager.getInstance().registerNode(new GridNode("E"));
		JVMGridManager.getInstance().registerNode(new GridNode("F"));
		JVMGridManager.getInstance().registerNode(new GridNode("G"));
		JVMGridManager.getInstance().registerNode(new GridNode("H"));
		JVMGridManager.getInstance().registerNode(new GridNode("I"));
		JVMGridManager.getInstance().registerNode(new GridNode("J"));
//		
		//Submit Task
		Date start = new Date();
		Date end = null;
		GridJobFuture future = JobManager.getInstance().start(new TestJob());
		
		try {
			log.info("FINAL RESULT : " + future.getResult());
			end = new Date();
			log.info("EXPECTED RESULT : " + TestRunner.sum);
		} catch (GridExecutionException e) {
			e.printStackTrace();
		}
		finally {
			log.info("Execution Time : " + (end.getTime() - start.getTime()) + " milliseconds");
		}
	}
}
