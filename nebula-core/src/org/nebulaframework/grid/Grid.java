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
package org.nebulaframework.grid;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.swing.JOptionPane;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.configuration.ConfigurationKeys;
import org.nebulaframework.configuration.ConfigurationSupport;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.ResultCallback;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.exceptions.GridJobRejectionException;
import org.nebulaframework.core.job.future.GridJobFuture;
import org.nebulaframework.discovery.ClusterDiscoverySupport;
import org.nebulaframework.discovery.DiscoveryFailureException;
import org.nebulaframework.discovery.GridNodeDiscoverySupport;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.cluster.node.services.job.execution.TaskExecutor;
import org.nebulaframework.grid.cluster.node.services.job.submission.JobSubmissionService;
import org.nebulaframework.grid.service.event.ServiceEventsSupport;
import org.nebulaframework.grid.service.event.ServiceHookCallback;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;
import org.nebulaframework.ui.swing.UISupport;
import org.nebulaframework.util.spring.NebulaApplicationContext;
import org.springframework.util.StopWatch;

/**
 * The general access point to Nebula Grid. Allows to configure
 * and start {@link ClusterManager} and {@link GridNode} instances.
 * <p>
 * This class implements the JobSubmissionService to allow easy access 
 * to grid job submission operations. Note that the Job submission methods
 * delegate to the actual {@link JobSubmissionService} implementation. 
 * These methods are only available when the Grid has an active Grid Node.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class Grid implements JobSubmissionService {
	
	/**
	 * Nebula Version.
	 */
	public static final String VERSION = "1.0 RC 1";
	
	/** Cluster Configuration Properties File */
	public static final String CLUSTER_PROPERTY_CONFIGURATION = "conf/nebula-cluster.properties";
	
	/** Cluster XML Properties File */
	public static final String CLUSTER_XML_CONFIGURATION = "conf/nebula-cluster.xml";
	
	/** Grid Node Configuration Properties File */
	public static final String GRIDNODE_PROPERTY_CONFIGURATION = "conf/nebula-client.properties";
	
	/** Grid Node XML Properties File */
	public static final String GRIDNODE_XML_CONFIGURATION = "conf/nebula-client.xml";
	
	/** Cluster Spring Beans Configuration File */
	public static final String CLUSTER_SPRING_CONTEXT = "org/nebulaframework/grid/cluster/manager/cluster-manager.xml";
	
	/** Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-node.xml";
	
	/** Light Weight (Non-worker) Grid Node Spring Beans Configuration File */
	public static final String GRIDNODE_LIGHT_CONTEXT = "org/nebulaframework/grid/cluster/node/grid-nonworker-node.xml";
	
	
	private static Log log = LogFactory.getLog(Grid.class);
	
	private static NebulaApplicationContext applicationContext = null;
	private static boolean clusterManager = false;
	private static boolean node = false;
	private static boolean lightweight = false;
	
	/**
	 * No instantiation
	 */
	private Grid() {
		// No instantiation
	}

	/**
	 * Starts {@link ClusterManager} instance with default settings,
	 * read from default properties file.
	 * 
	 * @return ClusterManager
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static ClusterManager startClusterManager() throws IllegalStateException{

		if (isInitialized()) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		initializeDefaultExceptionHandler();
		
		StopWatch sw = new StopWatch();
		
		try {
			sw.start();
			log.info("ClusterManager Starting...");
			
			// Set Security Manager
			System.setSecurityManager(new SecurityManager());
			
			// Detect Configuration
			Properties config = ConfigurationSupport.detectClusterConfiguration();
			
			// Register with any Colombus Servers
			ClusterDiscoverySupport.registerColombus(config);
			
			clusterManager = true;
			
			log.debug("Starting up Spring Container...");
			
			applicationContext = new NebulaApplicationContext(CLUSTER_SPRING_CONTEXT, config);
			
			log.debug("Spring Container Started");
			
			
			
			return (ClusterManager) applicationContext.getBean("clusterManager");
		} finally {
			sw.stop();
			log.info("ClusterManager Started Up. " + sw.getLastTaskTimeMillis() + " ms");
		}
		 
	}

	private static void initializeDefaultExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.fatal("[Uncaught Thread Exception] on Thread " + t.getName() + " - " + e,e);
				e.printStackTrace();
			}
			
		});		
	}

	/**
	 * Starts a {@link GridNode} with default settings, read from
	 * default properties file. This version invokes the {@link #startGridNode(boolean)}
	 * with value true.
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static GridNode startGridNode() throws IllegalStateException {
		return startGridNode(true);
	}
	

	/**
	 * Starts a {@link GridNode} with default settings, read from
	 * default properties file.
	 * 
	 * @param useConfigDiscovery indicates whether to use information
	 * from configuration to discover
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static GridNode startGridNode(boolean useConfigDiscovery) throws IllegalStateException {
		
		if (isInitialized()) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		initializeDefaultExceptionHandler();
		
		StopWatch sw = new StopWatch();
		
		try {
			
			sw.start();
			
			// Set Security Manager
			System.setSecurityManager(new SecurityManager());
			
			// Detect Configuration
			Properties config = ConfigurationSupport.detectNodeConfiguration();
			
			log.info("GridNode Attempting Discovery...");
			
			// Discover Cluster If Needed
			GridNodeDiscoverySupport.discover(config, useConfigDiscovery);
			
			checkJMSBroker(config.getProperty(ConfigurationKeys.CLUSTER_SERVICE.value()));
			
			log.debug("Starting up Spring Container...");
			
			applicationContext = new NebulaApplicationContext(GRIDNODE_CONTEXT, config);
			
			log.debug("Spring Container Started");
			
			node = true;
			
			sw.stop();
			
			log.info("GridNode Started Up. " + sw.getLastTaskTimeMillis() + " ms");
			
			return (GridNode) applicationContext.getBean("localNode");
		} finally {
			if (sw.isRunning()) {
				sw.stop();
			}
		}
	}


	/**
	 * Starts a Light-weight {@link GridNode} (a GridNode without
	 * Job Execution Support, that is non-worker) with default
	 * settings, read from default properties file.
	 * 
	 * This version invokes the {@link #startLightGridNode(boolean)}
	 * with value true.
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static GridNode startLightGridNode() throws IllegalStateException {
		return startLightGridNode(true, false);
	}
	
	/**
	 * Starts a Light-weight {@link GridNode} (a GridNode without
	 * Job Execution Support, that is non-worker) with default
	 * settings, read from default properties file.
	 * 
	 * @param useConfigDiscovery indicates whether to use information
	 * from configuration to discover
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static GridNode startLightGridNode(boolean useConfigDiscovery) throws IllegalStateException {
		return startLightGridNode(useConfigDiscovery, false);
	}
	
	/**
	 * Starts a Light-weight {@link GridNode} (a GridNode without
	 * Job Execution Support, that is non-worker) with default
	 * settings, read from default properties file.
	 * 
	 * @param useConfigDiscovery indicates whether to use information
	 * from configuration to discover
	 * 
	 * @param isGui indicates that the application is a GUI based
	 * application and any disconnection notifications should be
	 * done through message boxes.
	 * 
	 * @return GridNode
	 * 
	 * @throws IllegalStateException if a Grid Member (Cluster / Node) has
	 * already started with in the current VM. Nebula supports only one Grid
	 * Member per VM.
	 */
	public synchronized static GridNode startLightGridNode(boolean useConfigDiscovery, final boolean isGui) throws IllegalStateException {
		
		if (isInitialized()) {
			// A Grid Member has already started in this VM
			throw new IllegalStateException("A Grid Memeber Already Started in VM");
		}
		
		initializeDefaultExceptionHandler();
		
		StopWatch sw = new StopWatch();
		
		
		try {
			sw.start();
		
			// Set Security Manager
			System.setSecurityManager(new SecurityManager());
			
			Properties config = ConfigurationSupport.detectNodeConfiguration();
			
			log.info("GridNode Attempting Discovery...");
			
			// Discover Cluster If Needed
			GridNodeDiscoverySupport.discover(config, useConfigDiscovery);
			
			checkJMSBroker(config.getProperty(ConfigurationKeys.CLUSTER_SERVICE.value()));
			
			// If we reach here, connection test succeeded
			
			log.debug("Starting up Spring Container...");
			
			applicationContext = new NebulaApplicationContext(GRIDNODE_LIGHT_CONTEXT, config);
			
			log.debug("Spring Container Started");
			
			node = true;
			lightweight = true;
			
			sw.stop();
			log.info("GridNode Started Up. " + sw.getLastTaskTimeMillis() + " ms");
			
			GridNode node = (GridNode) applicationContext.getBean("localNode");
			ServiceEventsSupport.addServiceHook(new ServiceHookCallback() {

				@Override
				public void onServiceEvent(ServiceMessage message) {
					
					log.warn("[GridNode] Disconnected from Cluster");
					log.warn("[GridNode] Shutting Down");
					
					if (isGui) {
						JOptionPane.showMessageDialog(UISupport.activeWindow(), "Disconnected from Cluster, terminating VM");
					}
					System.exit(0);
				}
				
			}, node.getClusterId().toString(), ServiceMessageType.NODE_DISCONNECTED);
			
			return node;
		
		} finally {
			if (sw.isRunning()) {
				sw.stop();
			}
		}
	}

	/**
	 * Checks the given Service URL to see if the
	 * Broker is running.
	 * 
	 * @param url connection url
	 */
	private static void checkJMSBroker(String url) {
		try {
			
			log.debug("Attempting to establish connection with Cluster " + url);
			
			ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(url);
			Connection testConnection = cf.createConnection();
			testConnection.start();
			testConnection.close();
			
			log.debug("Connection established");
			
		} catch (Exception e) {
			log.warn("Unable to establish cluster connection");
			throw new DiscoveryFailureException("Unable to connect to Cluster",e);
		}
	}
	
	/**
	 * Returns the {@link NebulaApplicationContext} for this 
	 * Grid Member, if available.
	 * 
	 * @return NebulaApplicationContext
	 * 
	 * @throws IllegalStateException if not initialized (started)
	 */
	public static NebulaApplicationContext getApplicationContext() throws IllegalStateException {
		if (applicationContext==null) throw new IllegalStateException ("Not Initialized");
		return applicationContext;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@code ClusterManager}.
	 * 
	 * @return true if the current node is 
	 * a {@code ClusterManager}.
	 */
	public static boolean isClusterManager() {
		return clusterManager;
	}
	
	/**
	 * Returns true if the current node is 
	 * a {@code GridNode}.
	 * 
	 * @return true if the current node is 
	 * a {@code GridNode}.
	 */
	public static boolean isNode() {
		return node;
	}
	
	
	/**
	 * Returns true if the current node is
	 * a lightweight (no {@link TaskExecutor}} {@code GridNode}.
	 * 
	 * @return true if the current node is lightweight
	 */
	public static boolean isLightweight() {
		return lightweight;
	}

	/**
	 * Returns true if this Grid has been initialized before.
	 * 
	 * @return true if this Grid has been initialized.
	 */
	protected static boolean isInitialized() {
		return applicationContext !=null;
	}

	/**
	 * Invoked by GridNodes to notify that the GridNode has
	 * disconnected from Cluster.
	 */
	public static void nodeDisconnected() {
		applicationContext = null;
		node = false;
		lightweight = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, GridJobFuture> submitArchive(GridArchive archive) throws IllegalStateException, GridJobRejectionException{
		return GridNode.getInstance().getJobSubmissionService().submitArchive(archive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, GridJobFuture> submitArchive(GridArchive archive,
			Map<String, ResultCallback> callbacks) throws IllegalStateException, GridJobRejectionException {
		return GridNode.getInstance().getJobSubmissionService().submitArchive(archive, callbacks);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridJobFuture submitJob(Serializable annotatedJob)
			throws GridJobRejectionException, IllegalStateException {
		return GridNode.getInstance().getJobSubmissionService().submitJob(annotatedJob);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridJobFuture submitJob(Serializable annotatedJob,
			ResultCallback callback) throws GridJobRejectionException, IllegalStateException {
		return GridNode.getInstance().getJobSubmissionService().submitJob(annotatedJob, callback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridJobFuture submitJob(GridJob<?, ?> job)
			throws GridJobRejectionException, IllegalStateException {
		return GridNode.getInstance().getJobSubmissionService().submitJob(job);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GridJobFuture submitJob(GridJob<?, ?> job, ResultCallback callback)
			throws GridJobRejectionException, IllegalStateException {
		return GridNode.getInstance().getJobSubmissionService().submitJob(job, callback);
	}

	
}
