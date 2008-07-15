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
package org.nebulaframework.util.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility class which provides support for compression
 * of Objects into byte[]s and decompression of compressed
 * byte[]s back to Objects.
 * <p>
 * Internally, this class uses {@link Deflater} of Java API
 * for compression and {@link Inflater} for decompression.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class CompressionUtils {

	/**
	 * Best Compression Level (Slow)
	 */
	public static int BEST_COMPRESSION = Deflater.BEST_COMPRESSION;
	
	/**
	 * Normal Compression Level (Medium Speed, Medium Compression)
	 */
	public static int NORMAL_COMPRESSION = Deflater.DEFAULT_COMPRESSION;
	
	/**
	 * Best Speed (Low Compression). This is the default.
	 */
	public static int BEST_SPEED = Deflater.BEST_SPEED;
	
	/**
	 * Compresses the given Object into a byte[], using default
	 * settings. The default settings are BEST_SPEED and a buffer
	 * of 1024 bytes.
	 * 
	 * @param obj Object to be compressed
	 * 
	 * @return compressed byte[]
	 * 
	 * @throws IOException if thrown during process
	 */
	public static byte[] compress(Object obj) throws IOException {
		return compress(obj, 1024, Deflater.BEST_SPEED);
	}
	
	/**
	 * Compresses the given Object into a byte[], using default
	 * level, BEST_SPEED and a buffer of given size (in bytes).
	 * 
	 * @param obj Object to be compressed
	 * @param bufferSize Size of buffer in bytes
	 * 
	 * @return compressed byte[]
	 * 
	 * @throws IOException if thrown during process
	 */
	public static byte[] compress(Object obj, int bufferSize) throws IOException {
		return compress(obj, bufferSize, Deflater.BEST_SPEED);
	}
	
	/**
	 * Compresses the given Object into a byte[], using given
	 * level and a buffer of given size (in bytes).
	 * 
	 * @param obj Object to be compressed
	 * @param bufferSize Size of buffer in bytes
	 * @param level Compression level ({@link #BEST_COMPRESSION}, 
	 * {@link #NORMAL_COMPRESSION}, {@link #BEST_SPEED})
	 * 
	 * @return compressed byte[]
	 * 
	 * @throws IOException if thrown during process
	 */
	public static byte[] compress(Object obj, int bufferSize, int level) throws IOException {

		// Convert Object to byte[]
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		
		byte[] bytes = bos.toByteArray();
		
		// Compress
		Deflater deflater = new Deflater(level);
		deflater.setInput(bytes);
		deflater.finish();
		
		bos = new ByteArrayOutputStream(bytes.length);
		
	    byte[] buf = new byte[bytes.length];
	    while (!deflater.finished()) {
	        int count = deflater.deflate(buf);
	        bos.write(buf, 0, count);
	    }
	    
	    bos.close();
	    
	    return bos.toByteArray();
	}
	
	/**
	 * Decompresses the given byte[] and returns the Object.
	 * 
	 * @param <T> Type of expected Object
	 * @param bytes compressed byte[]
	 * @param bufferSize size of buffer to be used (in bytes)
	 * 
	 * @return decompressed Object
	 * 
	 * @throws IOException if failed to decompress
	 * @throws ClassCastException if cannot cast to specified type
	 */
	@SuppressWarnings("unchecked") /* Ignore Unchecked Cast Warning */
	public static <T> T decompress(byte[] bytes, int bufferSize) throws IOException, ClassCastException {
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);
			Inflater inflater = new Inflater();
			inflater.setInput(bytes);
			
			// Decompress
			byte[] buf = new byte[bufferSize];
			while (!inflater.finished()) {
			    int count = inflater.inflate(buf);
			    bos.write(buf, 0, count);
			}
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
			
			return (T) ois.readObject();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
