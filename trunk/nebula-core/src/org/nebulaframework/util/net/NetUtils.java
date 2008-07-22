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
	 * If the URL does not contain port information, an illegal argument
	 * @param url
	 * @return
	 * @throws IllegalArgumentException
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

}
