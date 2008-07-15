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
package org.nebulaframework.util.system;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides utility routines which enables
 * to obtain information about the
 * system environment.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class SystemUtils {
	
	private static Log log = LogFactory.getLog(SystemUtils.class);
	
	/**
	 * Attempts to detect information about the system
	 * environment and inserts the information to the
	 * property file using predefined keys.
	 * 
	 * @param props Properties File
	 */
	public static void detectSystemInfo(Properties props) {
		Map<String, String> map = detectSystemInfo();
		
		for (String key : map.keySet()) {
			props.put(key, map.get(key));
		}
	}
	
	/**
	 * Attempts to detect information about the system
	 * environment and returns the information as a Map.
	 * 
	 * @param props Properties File
	 * @return Map of information
	 */
	public static Map<String, String> detectSystemInfo() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.warn("[SystemUtils] Unable to resolve local IP Address");
		}
		
		map.put("os.name", System.getProperty("os.name"));
		map.put("os.arch", System.getProperty("os.arch"));
		map.put("java.version", System.getProperty("java.version"));
		map.put("java.vendor", System.getProperty("java.vendor"));
		if (ipAddress!=null) {
			map.put("net.ip", ipAddress.getHostAddress());
			map.put("net.name", ipAddress.getHostAddress());
		}
		else {
			map.put("net.ip", "");
			map.put("net.name", "");
		}
		
		return map;
	}
}
