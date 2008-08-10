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

package test.annotated.splitaggregate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.springframework.remoting.RemoteInvocationFailureException;

public class AnnotationTest {
	
	private static Log log = LogFactory.getLog(AnnotationTest.class);
	
	public static void main(String[] args) {

		// Test Job
		AnnotatedSATestJob annoatedJob = new AnnotatedSATestJob();
		
		try {

			
			GridNode node =  Grid.startLightGridNode();
			
			log.info("GridNode ID : " + node.getId());
			
			
			// Submit Job
			log.debug("Submitting Job");
			
			GridJobFuture future = node.getJobSubmissionService().submitJob(annoatedJob);
			try {
				log.info("Job Result : " + future.getResult());
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			node.getNodeRegistrationService().unregister();
			
			log.info("Unregistered, Terminating...");
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
}
