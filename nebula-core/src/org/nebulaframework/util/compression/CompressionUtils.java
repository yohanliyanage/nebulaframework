package org.nebulaframework.util.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {

	public static int BEST_COMPRESSION = Deflater.BEST_COMPRESSION;
	public static int NORMAL_COMPRESSION = Deflater.DEFAULT_COMPRESSION;
	public static int BEST_SPEED = Deflater.BEST_SPEED;
	
	public static byte[] compress(Object obj) throws IOException {
		return compress(obj, 1024, Deflater.BEST_SPEED);
	}
	
	public static byte[] compress(Object obj, int bufferSize) throws IOException {
		return compress(obj, bufferSize, Deflater.BEST_SPEED);
	}
	
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
	
	@SuppressWarnings("unchecked") /* Ignore Unchecked Cast Warning */
	public static <T> T decompress(byte[] bytes, int bufferSize) throws IOException {
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);
			Inflater inflater = new Inflater();
			inflater.setInput(bytes);
			
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
