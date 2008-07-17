package org.nebulaframework.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.Grid;
import org.nebulaframework.util.system.SystemUtils;

// TODO Fix DOc
public class ConfigurationSupport {

	private static Log log = LogFactory.getLog(ConfigurationSupport.class);
	
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
	
	public static Properties detectClusterConfiguration() {
		
		Properties props = new Properties();
		
		// Attempt Reading Properties File
		if (readPropertyFile(props, Grid.CLUSTER_PROPERTY_CONFIGURATION)) {
			// Property File Read
			log.info("[GridConfiguration] Loading from Property File");
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
	
	private static boolean readPropertyFile(Properties props, String fileName) {
		try {
			props.load(new FileInputStream(fileName));
			return true;
		} catch (IOException e) {
			log.debug("[GridConfiguration] Failed to Read Properties : " + Grid.GRIDNODE_PROPERTY_CONFIGURATION, e);
			return false;
		}
	}
	
	private static boolean readXMLFile(Properties props, String fileName) {
		// TODO Implement
		return false;
	}
}
