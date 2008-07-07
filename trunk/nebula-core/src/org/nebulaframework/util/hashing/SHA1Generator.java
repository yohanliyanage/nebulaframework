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

package org.nebulaframework.util.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * A utility class which generates SHA1 hash for a given
 * {@code byte[]} or a {@code String}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class SHA1Generator {

	private static Log log = LogFactory.getLog(SHA1Generator.class);
	
	
	/**
	 * Generates SHA1 Hash for the given {@code byte[]} and returns
	 * the hash code as a {@code byte[]}.
	 * 
	 * @param source source value
	 * @return SHA-1 hash for value
	 * 
	 * @see #generate(String)
	 * @see #generateAsString(byte[])
	 * 
	 * @throws IllegalArgumentException if {@code source} is {@code null}
	 */
	public static byte[] generate(byte[] source) throws IllegalArgumentException {
		
		// Check for nulls
		Assert.notNull(source);
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(source);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			log.fatal("Cannot load hashing algorithm", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Generates SHA1 Hash for the given {@code byte[]} and returns
	 * the hash code as a {@code String}.
	 * 
	 * @param source source value
	 * @return SHA-1 hash for value
	 * 
	 * @see #generate(byte[])
	 * @see #generate(String)
	 * 
	 * @throws IllegalArgumentException if {@code source} is {@code null}
	 */
	public static String generateAsString(byte[] source) throws IllegalArgumentException {
		return bytesToString(generate(source));
	}
	
	/**
	 * Overloaded version generates SHA1 Hash for the given {@code String} 
	 * and returns the hash code as a {@code String}.
	 * 
	 * @param source source value
	 * @return SHA-1 hash for value
	 * 
	 * @see #generate(byte[])
	 * @see #generateAsString(byte[])
	 * 
	 * @throws IllegalArgumentException if {@code source} is {@code null}
	 */
	public static String generate(String source) throws IllegalArgumentException {
		return bytesToString(generate(source.getBytes()));
	}
	
	/**
	 * Support method which converts a given {@code byte[]}
	 * to a HEX String.
	 * 
	 * @param bytes {@code byte[]} to convert
	 * @return {@code String} representation (hex)
	 * 
	 * @throws IllegalArgumentException if {@code bytes} is {@code null}
	 */
	private static String bytesToString(byte[] bytes) throws IllegalArgumentException {
		
		// Check for nulls
		Assert.notNull(bytes);
		
		StringBuilder sb = new StringBuilder();
		
		// Convert each byte to HEX String format
		for (int i = 0; i < bytes.length; i++) {
			sb.append(String.format("%02X%s", bytes[i],
					(i < bytes.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}	
}
