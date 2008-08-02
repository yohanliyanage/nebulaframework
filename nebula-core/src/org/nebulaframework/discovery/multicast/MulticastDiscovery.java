package org.nebulaframework.discovery.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.util.net.NetUtils;

/**
 * Implementation of Multicast Discovery. This class provides necessary
 * mechanisms to discover clusters using multicast messages. All 
 * ClusterManager's will start a multicast discovery service which listens
 * to multicast requests for a specific IP Address. When such a message
 * is received, it will respond on a designated response channel (another
 * multicast IP Address).
 *  
 * @author Yohan Liyanage
 * @version 1.0
 */
// TODO Fix Doc and re check as algo changed
public class MulticastDiscovery {

	private static Log log = LogFactory.getLog(MulticastDiscovery.class);
	
	/** Multicast Service Port */
	public static final int SERVICE_PORT = 8787;
	
	/** Request Channel IP Address */
	public static final InetAddress SERVICE_REQUEST_IP;
	
	/** Response Channel IP Address*/
	public static final InetAddress SERVICE_RESPONSE_IP;
	
	/** Default Greeting Message */
	public static final String GREET_MSG = "ALOHA";
	
	/** Multicast Discovery Timout */
	public static final long TIMEOUT = 10000L;

	private String cluster = null;
	
	/**
	 * Static initilization to resolve Multicast
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
	
	
	/** 
	 * Private Constructor. No External Instantiation.
	 */
	private MulticastDiscovery() {
		// No External Instantiation
	}
	
	/**
	 * Starts Multicast Discovery Service. This can only
	 * be invoked by a Nebula ClusterManager.
	 * 
	 * @throws IOException if occurred during operation
	 * @throws UnsupportedOperationException if invoked by non-ClusterManager nodes.
	 */
	public static void startService() throws IOException, UnsupportedOperationException {
		
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

					// Infinite Loop
					while (true) {
						// Buffer (for Greeting Message)
						byte[] msg = new byte[GREET_MSG.getBytes("UTF-8").length];
						
						// Create Datagram Packet
						DatagramPacket packet = new DatagramPacket(msg, msg.length);
						
						// Wait and Receive Request
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
		t.setDaemon(true);	// Run as Daemon thread
		t.start();			// Start Service
		log.debug("[MulticastDiscovery] Service Started");
	}
	
	/**
	 * Responds  to a Multicast Discovery Request by publishing
	 * the IP Address of Service into response channel. 
	 */
	protected static void doRespond() {
		
		// Only allowed for ClusterManagers
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Multicast Discovery Service can be enabled only for ClusterManagers");
		}
		
		try {
			// Get Broker Service URL
			String serviceUrl = ClusterManager.getInstance().getClusterInfo().getServiceUrl();
			
			byte[] hostInfo = NetUtils.getHostInfoAsBytes(serviceUrl);
			
			
			// Create Response Packet
			DatagramPacket response = new DatagramPacket(hostInfo, hostInfo.length, SERVICE_RESPONSE_IP, SERVICE_PORT);
			
			// Create Multicast Socket
			MulticastSocket resSock = new MulticastSocket();
			
			// Send response
			resSock.send(response);
			
			log.debug("[MulticastDiscovery] Responded Discovery Request");
		} catch (Exception e) {
			log.error("[MulticastDiscovery] Service Failed to Reply",e);
		}
		
	}

	/**
	 * Invoked by GridNodes to detect Clusters with in multicast
	 * reachable network range. The discovery process returns 
	 * without results if no cluster was discovered with in 
	 * a fixed duration, set by {@link #TIMEOUT}.
	 * 
	 * @return IP Address and Port of detected Cluster (String)
	 */
	public static String discoverCluster() {
		
		log.debug("[MulticastDiscovery] Attempting to discover cluster");
		
		// Synchronization Mutex
		final Object mutex = new Object();

		// Create Instance of MulticastDiscovery
		final MulticastDiscovery mDisc = new MulticastDiscovery();
		
		// Execute on a seperate Thread
		new Thread(new Runnable(){

			public void run() {
				try {
					// Attempt Discovery
					mDisc.doDiscover();
				} catch (IOException e) {
					log.debug("[MulticastDiscovery] Failed to Discover",e);
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
			log.info("[MulticastDiscovery] Discovered Cluster at " + mDisc.cluster);
		}
		return mDisc.cluster;
	}
	
	/**
	 * Discovery Process to identify Clusters with in 
	 * network.
	 * 
	 * @throws IOException if occurred during operation
	 */
	private void doDiscover() throws IOException {
		
		// Send Request
		byte[] greet = GREET_MSG.getBytes("UTF-8");
		DatagramPacket request = new DatagramPacket(greet, greet.length, SERVICE_REQUEST_IP, SERVICE_PORT);
		MulticastSocket reqSock = new MulticastSocket();
		reqSock.send(request);
		
		// Wait for Response
		MulticastSocket resSock = new MulticastSocket(SERVICE_PORT);
		resSock.joinGroup(SERVICE_RESPONSE_IP);
		
		// 9 = # of bytes for an IP Address + 5 byte port
		DatagramPacket response = new DatagramPacket(new byte[9],9);
		
		// Receive
		resSock.setSoTimeout((int)TIMEOUT);
		try {
			resSock.receive(response);
		} catch (SocketTimeoutException e) {
			log.debug("[MulticastDiscovery] Receive Timeout");
			return;
		}
		
		byte[] data = response.getData();
		
		byte[] ipBytes = Arrays.copyOfRange(data, 0,4);
		byte[] portBytes = Arrays.copyOfRange(data, 4, 9);
		
		InetAddress ip = InetAddress.getByAddress(ipBytes);
		StringBuilder sb = new StringBuilder(ip.getHostAddress());
		sb.append(":");
		for(byte b:portBytes) {
			sb.append(b);
		}
		
		this.cluster = sb.toString();
	}
	
	//TODO FixDOc
	public static void discoverPeerClusters() throws IOException {
		
		// Only allowed for ClusterManagers
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Multicast Discovery Service can be enabled only for ClusterManagers");
		}
		
		log.info("[MulticastDiscovery] Discovering Peer Clusters...");
		
		// Send Request
		byte[] greet = GREET_MSG.getBytes("UTF-8");
		DatagramPacket request = new DatagramPacket(greet, greet.length, SERVICE_REQUEST_IP, SERVICE_PORT);
		MulticastSocket reqSock = new MulticastSocket();
		reqSock.send(request);
		
		// Wait for Response
		MulticastSocket resSock = new MulticastSocket(SERVICE_PORT);
		resSock.joinGroup(SERVICE_RESPONSE_IP);
		
		// 9 = # of bytes for an IP Address + 5 byte port
		DatagramPacket response = new DatagramPacket(new byte[9],9);
		
		// Set Socket Timeout
		resSock.setSoTimeout((int)TIMEOUT);
		
		try {
			
			// Loop until Socket Timeout Occurs
			while(true) {
				
				// Receive
				resSock.receive(response);
				
				byte[] data = response.getData();
				
				byte[] ipBytes = Arrays.copyOfRange(data, 0,4);
				byte[] portBytes = Arrays.copyOfRange(data, 4, 9);
				
				InetAddress ip = InetAddress.getByAddress(ipBytes);
				StringBuilder sb = new StringBuilder(ip.getHostAddress());
				sb.append(":");
				for(byte b:portBytes) {
					sb.append(b);
				}
				
				// Add Peer Cluster
				ClusterManager.getInstance().getPeerService().addCluster(sb.toString());
			}
			
		} catch (SocketTimeoutException e) {
			log.debug("[MulticastDiscovery] Receive Timeout");
			return;
		}
		finally {
			log.info("[MulticastDiscovery] Peer Cluster Discovery Complete");
		}

	}
}
