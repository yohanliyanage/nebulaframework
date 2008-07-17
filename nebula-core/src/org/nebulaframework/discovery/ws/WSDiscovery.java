package org.nebulaframework.discovery.ws;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.service.event.ServiceEvent;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.net.NetUtils;

public class WSDiscovery {
	
	public static final String WS_DISCOVERY_PATH = "Colombus/Discovery";
	public static final String WS_MANAGER_PATH = "Colombus/Manager";
	
	private static Log log = LogFactory.getLog(WSDiscovery.class);
	private static String[] urls = null;
	
	private static ExecutorService exService = Executors.newSingleThreadExecutor();
	
	public static void setUrls(String[] urls) {
		WSDiscovery.urls = urls;
	}

	public static void registerCluster() {
		if (urls==null) {
			log.debug("[WSDiscovery] No Colombus Servers to Register");
			return;
		}
		
		String host = null;
		
		// Register on each Colombus Server
		for (String url : urls) {
			try {
				
				host = null;
				host = new URL(url.trim()).getHost();
				
				// Process and build full WS URL
				StringBuilder wsURL = new StringBuilder(url.trim());
				if (!url.trim().endsWith("/")) wsURL.append("/");
				wsURL.append(WSDiscovery.WS_MANAGER_PATH);
				
				// Register
				registerCluster(wsURL.toString());
				
			} catch (Exception e) {
				log.warn("[WSDiscovery] Failed to Register : " + host,e);
			}
		}
		
		// Shutdown ThreadExecutor when finished
		exService.shutdown();
	}
	
	public static void registerCluster(final String url)  {
		
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Allowed only to ClusterManagers");
		}
		
		exService.submit(new Runnable() {
			public void run() {
				doRegisterCluster(url);
			}
		});
	}

	protected static void doRegisterCluster(String url) {
		
		URL urlObj=null;
		
		try {
			
			urlObj = new URL(url);
			 
			// Create XFire WS Client
			Service service = new ObjectServiceFactory().create(ColombusManager.class);
			XFireProxyFactory factory = new XFireProxyFactory(XFireFactory.newInstance().getXFire());
			final ColombusManager mgr = (ColombusManager) factory.create(service, url);
			
			// Get Broker Service Host IP
			String serviceUrl = ClusterManager.getInstance().getBrokerUrl();
			final String serviceIP = NetUtils.getHostAddress(serviceUrl);

			// Register in Colombus Service
			mgr.registerCluster(serviceIP);
			
			// Add Service Hook to  Unregister
			ServiceEvent event = new ServiceEvent();
			event.addType(ServiceMessageType.CLUSTER_SHUTDOWN);
			event.setMessage(ClusterManager.getInstance().getClusterId().toString());
			
			ServiceHookCallback callback = new ServiceHookCallback() {

				public void onServiceEvent() {
					mgr.unregisterCluster(serviceIP);
				}
				
			};
			
			ServiceEventsSupport.getInstance().addServiceHook(event, callback);
			log.info("[WSDiscovery] Registered on Colombus Server " + new URL(url).getHost());
		} catch (Exception e) {
			log.warn("[WSDiscovery] Registration Failed : " + ((urlObj!=null) ? urlObj.getHost() : "?"));
		}		
	}

	public static String discoverCluster(String url) throws Exception {
		
		// Create XFire WS Client
		Service service = new ObjectServiceFactory().create(ColombusDiscovery.class);
		XFireProxyFactory factory = new XFireProxyFactory(XFireFactory.newInstance().getXFire());
		ColombusDiscovery mgr = (ColombusDiscovery) factory.create(service, url);

		// Attempt Discovery
		String cluster = mgr.discover();
		
		if (cluster==null) {
			// Discovery Failed
			log.debug("[WSDiscovery] Discovery Failed");
		}
		else {
			// Discovered
			log.debug("[WSDiscovery] Discovered Cluster " + cluster);
		}
		
		return cluster;
	}


	
	
	
}
