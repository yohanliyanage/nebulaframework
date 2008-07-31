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

package test.node;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.archive.GridArchiveException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.GridExecutionException;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.cluster.registration.RegistrationException;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.util.StopWatch;

public class TestNodeNARRunner {
	
	private static Log log = LogFactory.getLog(TestNodeRunner.class);
	

	public static void main(String[] args) {
		try {

			log.info("GridNode Starting...");
			StopWatch sw = new StopWatch();
			sw.start();
			
			GridNode node = Grid.startGridNode();
			
			log.info("GridNode ID : " + node.getId());
			
			node.getNodeRegistrationService().register();
			log.info("Registered in Cluster : " + node.getNodeRegistrationService().getRegistration().getClusterId());
			
			sw.stop();

			log.info("GridNode Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
			
			// Submit Job
			log.debug("Reading NAR");
			
			sw.start();
			
			GridArchive archive;
			archive = GridArchive.fromFile(new File("simpletestjob.nar"));
			
			log.debug("Submitting NAR");
			
			GridJobFuture future = (GridJobFuture) node.getJobSubmissionService().submitArchive(archive).values().toArray()[0];
			
			try {
				log.info("RESULT : " + future.getResult());
			} catch (RemoteInvocationFailureException e) {
				e.getCause().printStackTrace();
			}
			
			sw.stop();
			log.info("GridJob Finished. Duration " + sw.getLastTaskTimeMillis() + " ms");
			
			log.debug("Press any key to unregister GridNode and terminate");
			System.in.read();
			node.getNodeRegistrationService().unregister();
			
			log.info("Unregistered, Terminating...");
			System.exit(0);
			
			
		} 
		catch (GridArchiveException e) {
			log.fatal("GridArchiveException",e);
		}
		catch (GridExecutionException e) {
			log.fatal("Execution Failed",e);
		}
		catch (RegistrationException e) {
			log.error("Registration Failed",e);
		}
		catch (IOException e) {
			log.error("IOException",e);
		}

	}
}

