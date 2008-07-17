package org.nebulaframework.discovery.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.util.net.NetUtils;

// TODO Fix Doc
public class MulticastDiscovery {

	private static Log log = LogFactory.getLog(MulticastDiscovery.class);
	
	public static final int SERVICE_PORT = 8787;
	public static final InetAddress SERVICE_REQUEST_IP;
	public static final InetAddress SERVICE_RESPONSE_IP;
	public static final String GREET_MSG = "ALOHA";
	public static final long TIMEOUT = 5000L;

	private InetAddress cluster = null;
	
	/**
	 * Static initilizer to resolve multicast
	 * service IP Address.
	 */
	static {
		try {
			SERVICE_REQUEST_IP = InetAddress.getByName("230.0.0.1");
			SERVICE_RESPONSE_IP = InetAddress.getByName("230.0.0.2");
		} catch (UnknownHostException e) {
			// Should not happen
			throw new AssertionError(e);
		}
	}
	
	private MulticastDiscovery() {
		// No External Instantiation
	}
	
	public static void startService() throws IOException {
		
		// Only allowed for ClusterManagers
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Multicast Discovery Service can be enabled only for ClusterManagers");
		}
		

		
		// Start Service
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					
					// Start Multicast Socket and listen for Requests
					final MulticastSocket mSock = new MulticastSocket(SERVICE_PORT);
					mSock.joinGroup(SERVICE_REQUEST_IP);
					
					while (true) {
						// Buffer (for Greeting Message)
						byte[] msg = new byte[GREET_MSG.getBytes("UTF-8").length];
						
						// Create Datagram Packet
						DatagramPacket packet = new DatagramPacket(msg, msg.length);
						
						// Receive Request
						mSock.receive(packet);
						log.debug("[MulticastDiscovery] Received Discovery Request");
						
						// Check if Greeting Message is valid
						try {
							String greet = new String(packet.getData());
							if (!greet.equals(GREET_MSG)) {
								throw new IllegalArgumentException("Invalid Greeting");
							}
						}
						catch (Exception e) {
							log.debug("Malformed Multicast Message Igonored");
							continue;
						}
						
						// Respond
						doRespond();
					}
					
				} catch (IOException e) {
					log.error("[MulticastDiscovery] Service Failed on Receive",e);
				}
				
			}
			
		});
		t.setDaemon(true);
		t.start();
		log.debug("[MulticastDiscovery] Service Started");
	}
	
	protected static void doRespond() {
		
		// Only allowed for ClusterManagers
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Multicast Discovery Service can be enabled only for ClusterManagers");
		}
		
		try {
			// Get Broker Service URL
			String serviceUrl = ClusterManager.getInstance().getBrokerUrl();
			
			byte[] hostIp = NetUtils.getHostAddressAsBytes(serviceUrl);
			
			// Create Response Packet
			DatagramPacket response = new DatagramPacket(hostIp, hostIp.length, SERVICE_RESPONSE_IP, SERVICE_PORT);
			
			MulticastSocket resSock = new MulticastSocket();
			
			// Send response
			resSock.send(response);
			log.debug("[MulticastDiscovery] Responded Discovery Request");
		} catch (Exception e) {
			log.error("[MulticastDiscovery] Service Failed to Reply",e);
		}
		
	}

	public static InetAddress discoverCluster() {
		log.info("[MulticastDiscovery] Attempting to discover cluster");
		final Object mutex = new Object();

		final MulticastDiscovery mDisc = new MulticastDiscovery();
		
		new Thread(new Runnable(){

			public void run() {
				try {
					// Attempt Discovery
					mDisc.doDiscover();
				} catch (IOException e) {
					log.warn("[MulticastDiscovery] Failed to Discover",e);
				}
				
				// Notify waiting Threads
				synchronized (mutex) {
					mutex.notifyAll();
				}
				
			}
			
		}).start();
		
		// Wait till response come or timeout happens
		synchronized (mutex) {
			
			try {
				mutex.wait(TIMEOUT);
			} catch (InterruptedException e) {
				// Should not happen
				throw new AssertionError(e);
			}
			
		}
		
		if (mDisc.cluster != null) {
			log.info("[MulticastDiscovery] Discovered Cluster at " + mDisc.cluster.getHostAddress());
		}
		return mDisc.cluster;
	}
	
	private void doDiscover() throws IOException {
		
		// Send Request
		byte[] greet = GREET_MSG.getBytes("UTF-8");
		DatagramPacket request = new DatagramPacket(greet, greet.length, SERVICE_REQUEST_IP, SERVICE_PORT);
		MulticastSocket reqSock = new MulticastSocket();
		reqSock.send(request);
		
		// Wait for Response
		MulticastSocket resSock = new MulticastSocket(SERVICE_PORT);
		resSock.joinGroup(SERVICE_RESPONSE_IP);
		
		byte[] hostIP = new byte[4]; // 4 = # of bytes for an IP Address
		DatagramPacket response = new DatagramPacket(hostIP, hostIP.length);
		
		// Receive
		resSock.receive(response);
		
		this.cluster = InetAddress.getByAddress(response.getData());
		
	}
}
