package org.nebulaframework.util.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SHA1Generator {

	private static Log log = LogFactory.getLog(SHA1Generator.class);
	
	
	public static byte[] generate(byte[] source) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(source);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			log.fatal("Cannot load hashing algorithm", e);
			throw new RuntimeException(e);
		}
	}
	
	public static String generate(String source) {
		return bytesToString(generate(source.getBytes()));
	}
	
	public static String bytesToString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(String.format("%02X%s", bytes[i],
					(i < bytes.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}	
}
