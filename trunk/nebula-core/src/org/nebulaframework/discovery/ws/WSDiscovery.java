package org.nebulaframework.discovery.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.service.event.ServiceEvent;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.util.net.NetUtils;
import org.springframework.util.Assert;

public class WSDiscovery {
	
	public static final String WS_DISCOVERY_PATH = "Colombus/Discovery";
	public static final String WS_MANAGER_PATH = "Colombus/Manager";
	public static final String WS_NAMESPACE = "http://ws.discovery.nebulaframework.org";
	
	private static Log log = LogFactory.getLog(WSDiscovery.class);
	private static URL[] urls = null;
	
	private static ExecutorService exService = Executors.newSingleThreadExecutor();
	
	public static void setUrls(String[] urls) {
		
		List<URL> lst = new ArrayList<URL> ();
		for (String strUrl : urls) {
			try {
				// Process and build full WS URL
				StringBuilder wsURL = new StringBuilder(strUrl.trim());
				if (wsURL.toString().endsWith("/")) wsURL.append("/");
				wsURL.append(WSDiscovery.WS_MANAGER_PATH);
				
				// Create and enlist URL object
				URL url = new URL(wsURL.toString());
				lst.add(url);
			} catch (MalformedURLException e) {
				log.warn("Malformed URL in Colombus Server URLs : " + strUrl);
			}
		}
		
		WSDiscovery.urls = lst.toArray(new URL[] {});
	}

	public static void registerCluster() {
		
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Allowed only to ClusterManagers");
		}
		
		if (urls==null) {
			log.debug("[WSDiscovery] No Colombus Servers to Register");
			return;
		}
		
		// Register on each Colombus Server
		for (URL url : urls) {
			registerCluster(url);
		}
		
		// Shutdown ThreadExecutor when finished
		exService.shutdown();
	}
	
	public static void registerCluster(final URL url)  throws IllegalArgumentException {
		
		Assert.notNull(url);
		
		if (! Grid.isClusterManager()) {
			throw new UnsupportedOperationException("Allowed only to ClusterManagers");
		}
		
		exService.submit(new Runnable() {
			public void run() {
				doRegisterCluster(url);
			}
		});
	}

	protected static void doRegisterCluster(URL url) throws IllegalArgumentException {
		
			Assert.notNull(url);
			
			try {
				log.debug("[WSDiscovery] Connecting Colombus on " + url);
				
				// Create CXF JAX-WS Proxy
				JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
				factory.setServiceClass(ColombusManager.class);
				factory.setAddress(url.toString());
				final ColombusManager mgr = (ColombusManager) factory.create();

				// Get Broker Service Host IP
				String serviceUrl = ClusterManager.getInstance().getClusterInfo().getServiceUrl();
				final String serviceIP = NetUtils.getHostAddress(serviceUrl);
				
				// Register in Colombus Service
				mgr.registerCluster(serviceIP);

				// Register for Cluster Shutdown Event to Unregister Cluster
				ServiceEvent event = new ServiceEvent();
				event.addType(ServiceMessageType.CLUSTER_SHUTDOWN);
				event.setMessage(ClusterManager.getInstance().getClusterId().toString());
				
				ServiceHookCallback callback = new ServiceHookCallback() {

					public void onServiceEvent() {
						mgr.unregisterCluster(serviceIP);
					}
					
				};
				
				ServiceEventsSupport.getInstance().addServiceHook(event, callback);
				log.info("[WSDiscovery] Registered on Colombus Server " + url.getHost());
				
			} catch (UnknownHostException e) {
				log.warn("[WSDiscovery] Registration Failed : " + url.getHost());
			}
	}

	public static String discoverCluster(String url) throws Exception {
		
		// Create CXF JAX-WS Proxy
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ColombusDiscovery.class);
		factory.setAddress(url);
		final ColombusDiscovery discovery = (ColombusDiscovery) factory.create();
		
		// Attempt Discovery
		String cluster = discovery.discover();
		
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


	// FIXME Implement Peer Cluster Discovery
	
	
}
