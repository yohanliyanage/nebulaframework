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

package test.manager;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.network.NetworkConnector;
import org.nebulaframework.grid.Grid;


public class TestManagerRunner {
	
	public static void main(String[] args) throws Exception {
		

		
		Grid.startClusterManager();
		
		
		
		BrokerService broker = (BrokerService) Grid.getApplicationContext().getBean("broker");
		
		System.out.println("Press any key to Add new Network Broker");
		System.in.read();
		
		NetworkConnector con = broker.addNetworkConnector("static://(tcp://excalibur:61616)");
		
		if (con!=null) {
			// con.addExcludedDestination(new ActiveMQQueue(">"));
			con.addStaticallyIncludedDestination(new ActiveMQTopic("nebula.cluster.service.topic"));
			con.setConduitSubscriptions(false);
			con.setDynamicOnly(true);
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.registration.queue"));
			con.addExcludedDestination(new ActiveMQQueue("nebula.cluster.services.facade.queue"));
			con.setNetworkTTL(128);
			con.start();
		}

//		System.out.println("Press any key to Shutdown");
//		System.in.read();
//		
//		mgr.shutdown();
	}
	
	
}