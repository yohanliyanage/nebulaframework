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
package org.nebulaframework.discovery.ws.colombus;

import javax.xml.ws.Endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.discovery.ws.ColombusDiscovery;
import org.nebulaframework.discovery.ws.ColombusDiscoveryImpl;
import org.nebulaframework.discovery.ws.ColombusManagerImpl;

/**
 * ColombusServer, which creates an embedded Jetty Server and
 * exposes  the {@link ColombusDiscovery} and
 * {@link ColombusManagerImpl} web services.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ColombusServer {

	private static Log log = LogFactory.getLog(ColombusServer.class);
	
	private static int port = 9000;
	
	/**
	 * Execution Point of Colombus Server.
	 * 
	 * @param args Command Line Arguments
	 */
	public static void main(String[] args) {
		
		// If User has given Port
		if (args.length>0) {
			try {
				port = Integer.parseInt(args[0]);
				if (port <=0 || port >= 65535) {
					throw new NumberFormatException("Out of Range");
				}
			} catch (NumberFormatException e) {
				showHelp();
				System.exit(1);
			}
		}
		
		long t1 = System.currentTimeMillis();
		log.info("[Colombus Server] Starting Up");
		
		// Start Web Services
		startDiscoveryService();
		startManagerService();
		
		log.info("[Colombus Server] Started in " + (System.currentTimeMillis()-t1) + "ms");
	}

	/**
	 * Creates the Service EndPoint for Discovery Service.
	 */
	private static void startDiscoveryService() {
		
		ColombusDiscoveryImpl discoveryImpl = new ColombusDiscoveryImpl();
		String address = "http://localhost:"+ port +"/Colombus/Discovery";
		Endpoint.publish(address, discoveryImpl);
		
		log.debug("[Colombus Server] Published EndPoint : " + address);
	}
	
	/**
	 * Creates Service EndPoint for Management Service
	 */
	private static void startManagerService() {
		
		ColombusManagerImpl managerImpl = new ColombusManagerImpl();
		String address = "http://localhost:"+ port +"/Colombus/Manager";
		Endpoint.publish(address, managerImpl);
		
		log.debug("[Colombus Server] Published EndPoint : " + address);
	}
	
	/**
	 * Displays a Help Message regarding starting
	 * a Colombus Server.
	 */
	private static void showHelp() {
		System.out.println("Colombus Server");
		System.out.println("---------------");
		System.out.println("");
		System.out.println("Colombus is the WebService based Discovery Component of Nebula Grid.");
		System.out.println("This application starts up a Colombus Web Service.");
		System.out.println("");
		System.out.println("Usage :");
		System.out.println("\tjava -jar Colombus.jar [port]");
		System.out.println("");
		System.out.println("\tport\tport number; 0 < port < 65535 (optional, default = 9000)");
	}
}
