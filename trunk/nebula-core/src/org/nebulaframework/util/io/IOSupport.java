package org.nebulaframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class IOSupport {

	public static byte[] readBytes(InputStream is) throws IOException {
	
		int byteRead = -1;
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		// Read all bytes
		while(( byteRead = is.read())!=-1) {
			list.add((byte) byteRead);
		}
		
		// Convert Byte[] to byte[] and return
		byte[] bytes = new byte[list.size()];
		for (int i=0; i<list.size(); i++) {
			bytes[i] = list.get(i);
		}
		
		return bytes;
	}
}
