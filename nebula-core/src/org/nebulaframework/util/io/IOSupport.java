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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
	
	/**
	 * Serializes the given object and returns a byte[] representation of
	 * the serialized data.
	 * 
	 * @param obj Object to Serialize
	 * 
	 * @return byte[] of serialized data
	 * 
	 * @throws IOException if thrown during serialization
	 */
	public static <T extends Serializable> byte[] serializeToBytes(T obj) throws IOException{
		
		Assert.notNull(obj);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		
		return bos.toByteArray();
	}
	
	/**
	 * Deserializes an object instance from the given byte[] 
	 * of serial data.
	 * 
	 * @param bytes Byte[] containing serialized data
	 * 
	 * @return Deserialized object
	 * 
	 * @throws IOException if thrown during serialization
	 */
	@SuppressWarnings("unchecked") // Ignore type casting Warnings
	public static <T extends Serializable> T deserializeFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		
		Assert.notNull(bytes);
		
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
		return (T) ois.readObject();
	}
	
	/**
	 * Deserializes an object instance from the given byte[] 
	 * of serial data, and uses the specified ClassLoader 
	 * during process.
	 * 
	 * @param bytes Byte[] containing serialized data
	 * @param classLoader ClassLoader to be used to resolve classes
	 * 
	 * @return Deserialized object
	 * 
	 * @throws IOException if thrown during serialization
	 */
	@SuppressWarnings("unchecked") // Ignore type casting Warnings
	public static <T extends Serializable> T deserializeFromBytes(byte[] bytes, ClassLoader classLoader) throws IOException, ClassNotFoundException {
		
		Assert.notNull(bytes);
		
		// Use NebulaObjectInputStream, which allows using a specified ClassLoader
		// to resolve classes
		NebulaObjectInputStream ois = new NebulaObjectInputStream(new ByteArrayInputStream(bytes));
		ois.setClassLoader(classLoader);
		
		return (T) ois.readObject();
	}
}
