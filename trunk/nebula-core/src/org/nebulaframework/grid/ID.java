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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.nebulaframework.grid.cluster.node.GridNode;

/**
 * Provides Identifier Generation support for Nebula Grid Members
 * ({@link ClusterManager}s and {@link GridNode}s.
 * <p>
 * The generated UUID is an identifier which is dependent on
 * the MAC Address of the Network Interface and current time.
 * If MAC address information is not available, a random UUID
 * is returned.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ID {

	private static Log log = LogFactory.getLog(ID.class);

	/**
	 * Attempts to read the MAC Address of the local Network
	 * Interface.
	 * 
	 * @return MAC Address as a HEX-String
	 * 
	 * @throws IOException if occurred during process
	 */
	private static String getMACAddress() throws IOException {
		StringBuilder mac = new StringBuilder();

		// Obtain NetworkInterface Reference
		NetworkInterface networkInterface = NetworkInterface
				.getByInetAddress(InetAddress.getLocalHost());

		// Read MAC Address as Bytes
		byte[] macBytes = networkInterface.getHardwareAddress();

		// Check if MAC info is not available
		if (macBytes == null)
			return null;

		// Convert MAC Address to String
		for (int i = 0; i < macBytes.length; i++) {
			mac.append(String.format("%02X%s", macBytes[i],
					(i < macBytes.length - 1) ? "-" : ""));
		}

		return mac.deleteCharAt(mac.length() - 1).toString();
	}

	/**
	 * Returns the Identifier for the invoking Grid Member,
	 * which is dependent on the MAC Address and the current 
	 * time or random UUID if MAC Address is not 
	 * available.
	 * 
	 * @return UUID Identifier
	 */
	public static UUID getId() {

		String mac = null;
		try {
			mac = getMACAddress();
		} catch (Exception e) {
			log.warn("Unalbe to obtain MAC Address", e);
		}

		if (mac != null) {
			// MAC Info available, use it to generate UUID
			return UUID.nameUUIDFromBytes(new String(mac + (new Date()).getTime()).getBytes());
		} else {
			// MAC Info not available, fallback to Random UUID
			return UUID.randomUUID();
		}
	}
}
