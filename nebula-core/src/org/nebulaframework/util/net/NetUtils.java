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
package org.nebulaframework.util.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Network related utility operations for Nebula Framework.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class NetUtils {

	
	/**
	 * Returns the Host Name as a String, parsed from
	 * the given URL.
	 * <p>
	 * For example, for URL {@code tcp://host:port/xyz},
	 * host name is '{@code host}'.
	 * 
	 * @param url URL as a String
	 * @return Host Name from URL.
	 */
	public static String getHostName(String url) {

		// Split from :// and : and get Host Name Part
		String hostName = url.split("://")[1].split(":")[0];

		// For localhost loopback, detect real network name
		if (hostName.equals("localhost") || hostName.equals("127.0.0.1")) {
			try {
				// Local Address : Find Real Network IP
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				// Should not happen
				throw new AssertionError(e);
			}
		}

		return hostName;
	}

	/**
	 * Returns the Host Address (IP Address) of the given 
	 * URL. This method uses {@link InetAddress#getByName(String)}
	 * to detect the IP Address of the given host.
	 * 
	 * @param url URL as a String
	 * @return Host Address as String
	 */
	public static String getHostAddress(String url) throws UnknownHostException {

		// Split from :// and : and get Host Name Part
		String hostAddress = url.split("://")[1].split(":")[0];

		// For localhost loopback, detect real network name
		if (hostAddress.equals("localhost")
				|| hostAddress.equals("127.0.0.1")) {
			
			// Local Address : Find Real Network IP
			hostAddress = InetAddress.getLocalHost().getHostAddress();

		} else {
			// Find IP Address
			hostAddress = InetAddress.getByName(hostAddress)
					.getHostAddress();
		}
		
		return hostAddress;
	}
	
	/**
	 * Returns the Host Address of a given URL as byte[].
	 * 
	 * @param url URL as a String
	 * 
	 * @return Host IP Address, as byte[]
	 * 
	 * @throws UnknownHostException if Host Name cannot be mapped to IP Address
	 * 
	 * @see {@link #getHostAddress(String)}
	 */
	public static byte[] getHostAddressAsBytes(String url) throws UnknownHostException {
		return InetAddress.getByName(getHostAddress(url)).getAddress();
	}

	/**
	 * Returns the port number extracted from the given URL, if available.
	 * 
	 * @param url URL to Parse
	 * 
	 * @return Port as integer
	 * 
	 * @throws IllegalArgumentException if URL is invalid or does not contain port information
	 */
	public static int getHostPort(String url) throws IllegalArgumentException {
		
		
		try {
			String port = url.split("://")[1].split(":")[1];
			int portNum = Integer.parseInt(port);
			if (portNum >= 0 && portNum <= 65535 ) {
				return portNum;
			}
			else {
				throw new IllegalArgumentException("Port Number Out of Range");
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("No Port Information in URL : " + url);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid URL String : " + url);
		}
	}
	
	/**
	 * Returns the port number extracted from the given URL, if available.
	 * 
	 * @param url URL to Parse
	 * 
	 * @return port as byte[] of size 5. Extra bytes will be padded with 0 from left.
	 * 
	 * @throws IllegalArgumentException if URL is invalid or does not contain port information
	 */
	public static byte[] getHostPortAsBytes(String url) throws IllegalArgumentException {
		
		String strHostPort = String.format("%05d", NetUtils.getHostPort(url));
		
		try {
			byte[] hostPort = new byte[5];
			for (int i=0; i<5; i++) {
				byte b = 0;
				b = Byte.parseByte(strHostPort.substring(i, i+1));
				hostPort[i] = b;
			}
			
			return hostPort;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid URL " + url);
		}
		
	}
	
	/**
	 * Returns the Host Information (IP Address, Port) as byte[].
	 * 
	 * @param url Service URL to Parse
	 * 
	 * @return Host Information as byte[]
	 * 
	 * @throws IllegalArgumentException If Service URL is invalid
	 * @throws UnknownHostException If Host cannot be parsed
	 */
	public static byte[] getHostInfoAsBytes(String url) throws IllegalArgumentException, UnknownHostException {
		
		byte[] hostIp = NetUtils.getHostAddressAsBytes(url);
		byte[] hostPort = NetUtils.getHostPortAsBytes(url);
		
		// 4 + 5
		byte[] hostInfo = new byte[hostIp.length + hostPort.length];
		int i = 0;
		
		for (byte b : hostIp) {
			hostInfo[i++] = b;
		}
		
		for (byte b : hostPort) {
			hostInfo[i++] = b;
		}
		
		return hostInfo;
	}
	
	/**
	 * Returns the real IP Address of the NIC for the localhost.
	 * 
	 * @return String representation of IP Address
	 * 
	 * @throws RuntimeException If unable to resolve address
	 */
	public static String getLocalHostAddress() throws RuntimeException {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the Protocol of given URL.
	 * 
	 * @param url URL to Parse
	 * @return Protocol Name
	 * @throws IllegalArgumentException If Invalid URL
	 */
	public static String getProtocol(String url) throws IllegalArgumentException {
		
		
		try {
			return url.split("://")[0].toUpperCase();
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid URL : " + url);
		}
	}
}
