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

package org.nebulaframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Provides support for utility IO operations.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class IOSupport {

	/**
	 * Reads from the given {@codeInputStream} until end of stream
	 * and returns the data read as a {@code byte[]}
	 * 
	 * @param is InputStream
	 * 
	 * @return bytes read as {@code byte[]} 
	 * 
	 * @throws IOException If IO error occurs during operation
	 * @throws IllegalArgumentException if {@code InputStream} is {@code null}
	 */
	public static byte[] readBytes(InputStream is) 
		throws IOException, IllegalArgumentException {

		Assert.notNull(is);
		
		int byteRead = -1;
		List<Byte> list = new ArrayList<Byte>();

		// Read all bytes
		while ((byteRead = is.read()) != -1) {
			list.add((byte) byteRead);
		}

		// Convert to byte[] and return
		return tobyteArray(list);
	}

	/**
	 * Overloaded version reads from the given {@codeInputStream} until end of stream
	 * occurs or until a specified number of bytes is read (whichever occurs first). 
	 * Returns the data read as a {@code byte[]}
	 * 
	 * @param is InputStream
	 * 
	 * @return bytes read as {@code byte[]} 
	 * 
	 * @throws IOException If IO error occurs during operation
	 * @throws IllegalArgumentException if {@code InputStream} is {@code null}
	 */
	public static byte[] readBytes(InputStream is, long len) 
			throws IOException, IllegalArgumentException  {

		Assert.notNull(is);
		Assert.isTrue(len>0);
		
		int byteRead = -1;
		List<Byte> list = new ArrayList<Byte>();

		// Read all bytes until specified 'len' bytes are read
		while (((byteRead = is.read()) != -1) || (len > 0)) {
			list.add((byte) byteRead);
			len--;
		}

		// Convert to byte[] and return
		return tobyteArray(list);
	}

	/**
	 * Support method which converts a given List of Bytes to
	 * an array of primitive bytes.
	 * 
	 * @param list {@code List<Byte>}
	 * @return A {@code byte[]} representation of given list
	 * @throws IllegalArgumentException if {@code list} is {@code null}
	 */
	private static byte[] tobyteArray(List<Byte> list) 
			throws IllegalArgumentException {

		Assert.notNull(list);
		
		// Convert List<Byte> to byte[] and return
		byte[] bytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			bytes[i] = list.get(i);
		}

		return bytes;
	}
}
