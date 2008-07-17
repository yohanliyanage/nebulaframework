package org.nebulaframework.util.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

// TODO FixDoc
public class NetUtils {

	public static String getHostName(String url) {

		String hostName = url.split("://")[1].split(":")[0];

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

	public static String getHostAddress(String url) {

		String hostAddress = url.split("://")[1].split(":")[0];

		try {
			if (hostAddress.equals("localhost")
					|| hostAddress.equals("127.0.0.1")) {
				
				// Local Address : Find Real Network IP
				hostAddress = InetAddress.getLocalHost().getHostAddress();

			} else {
				hostAddress = InetAddress.getByName(hostAddress)
						.getHostAddress();
			}
		} catch (UnknownHostException e) {
			// Should not happen
			throw new AssertionError(e);
		}
		return hostAddress;
	}
	
	public static byte[] getHostAddressAsBytes(String url) throws UnknownHostException {
		return InetAddress.getByName(getHostAddress(url)).getAddress();
	}

}
