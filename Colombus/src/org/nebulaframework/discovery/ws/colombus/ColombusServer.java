package org.nebulaframework.discovery.ws.colombus;

import javax.xml.ws.Endpoint;

import org.nebulaframework.discovery.ws.ColombusDiscoveryImpl;
import org.nebulaframework.discovery.ws.ColombusManagerImpl;

public class ColombusServer {

	private static int port = 9000;
	
	public static void main(String[] args) {
		
		if (args.length>0) {
			try {
				port = Integer.parseInt(args[0]);
				if (port <=0 || port >= 65535) {
					throw new IllegalArgumentException();
				}
			} catch (Exception e) {
				showHelp();
				System.exit(1);
			}
		}
		
		startDiscoveryService();
		startManagerService();
	}
	
	private static void startDiscoveryService() {
		ColombusDiscoveryImpl discoveryImpl = new ColombusDiscoveryImpl();
		String address = "http://localhost:"+ port +"/Colombus/Discovery";
		Endpoint.publish(address, discoveryImpl);
	}
	
	private static void startManagerService() {
		ColombusManagerImpl managerImpl = new ColombusManagerImpl();
		String address = "http://localhost:"+ port +"/Colombus/Manager";
		Endpoint.publish(address, managerImpl);
	}
	
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
