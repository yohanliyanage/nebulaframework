package org.nebulaframework.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.util.system.SystemUtils;

/**
 * Provides support routines to detect and configure
 * Nebula Grid Members using various configuration
 * options including Property Files and XML Configuration
 * files.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ConfigurationSupport {

	private static Log log = LogFactory.getLog(ConfigurationSupport.class);
	
	/**
	 * Detects configuration of a GridNode, using a given
	 * Property File / XML Configuration File. Also
	 * detects the System Information related to the current
	 * system automatically.
	 * 
	 * @return Configuration Properties
	 */
	public static Properties detectNodeConfiguration() {
		
		Properties props = new Properties();
		
		// Attempt Reading Properties File
		if (readPropertyFile(props, Grid.GRIDNODE_PROPERTY_CONFIGURATION)) {
			// Property File Read
			log.info("[GridConfiguration] Loading from Property File");
		}
		else if (readXMLFile(props, Grid.GRIDNODE_XML_CONFIGURATION)) {
			// XML File Read
			log.info("[GridConfiguration] Loading from XML File");
		}
		else {
			// TODO Currently Config only contains cluster URL. So fallback = discovery enabled
			// No Configuration File [ Attempt Discovery]
			log.warn("[GridConfiguration] Configuration Details Not Found. Falling back to defaults");
		}
	
		// Get SystemInfo
		SystemUtils.detectSystemInfo(props);
		
		return props;
		
	}
	
	/**
	 * Detects the configuration of a {@link ClusterManager},
	 * using a given Property File or a XML Configuration File.
	 * 
	 * @return Configuration Properties
	 */
	public static Properties detectClusterConfiguration() {
		
		Properties props = new Properties();
		
		// Attempt Reading Properties File
		if (readPropertyFile(props, Grid.CLUSTER_PROPERTY_CONFIGURATION)) {
			// Property File Read
			log.info("[GridConfiguration] Loading from Property File");
			
			// TODO Process and Update Props as Needed
			
			// If No Transport Information, add dummy value
			if (!props.containsKey(ConfigurationKeys.TRANSPORT_URLS.value())) {
				props.put(ConfigurationKeys.TRANSPORT_URLS.value(), "");
			}
		}
		else if (readXMLFile(props, Grid.CLUSTER_XML_CONFIGURATION)) {
			// XML File Read
			log.info("[GridConfiguration] Loading from XML File");
		}
		else {
			// TODO Currently Config only contains cluster URL. So fallback = discovery enabled
			// No Configuration File [ Attempt Discovery]
			log.warn("[GridConfiguration] Configuration Details Not Found. Falling back to defaults");
		}
		return props;
	}
	
	/**
	 * Reads the given Property File and loads the information into
	 * the specified Properties object.
	 * 
	 * @param props Properties Object
	 * @param fileName File Name, as a String
	 * 
	 * @return boolean value indicating success / failure
	 */
	private static boolean readPropertyFile(Properties props, String fileName) {
		FileInputStream fis = null;
		try {
			// Load Properties
			log.debug("Config : " + fileName);
			fis = new FileInputStream(fileName);
			props.load(fis);
			return true;
		} catch (IOException e) {
	
			log.debug("[GridConfiguration] Failed to Read Properties : " + fileName, e);
			return false;
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				log.error("Unable to Close FileInputStream",e);
			}
		}
		
	}
	
	/**
	 * Parses the given XML Configuration File and loads the information
	 * into the specified Properties object.
	 * 
	 * @param props Properties Object
	 * @param fileName File Name, as a String
	 * 
	 * @return boolean value indicating success / failure 
	 */
	private static boolean readXMLFile(Properties props, String fileName) {
		// TODO Implement XML Configuration Support
		return false;
	}
}
