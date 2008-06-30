package org.nebulaframework.core.support;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ID {

	private static Log log = LogFactory.getLog(ID.class);

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

	public static UUID getId() {

		String mac = null;
		try {
			mac = getMACAddress();
		} catch (IOException e) {
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
