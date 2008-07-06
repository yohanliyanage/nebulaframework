package org.nebulaframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IOSupport {

	public static byte[] readBytes(InputStream is) throws IOException {

		int byteRead = -1;
		List<Byte> list = new ArrayList<Byte>();

		// Read all bytes
		while ((byteRead = is.read()) != -1) {
			list.add((byte) byteRead);
		}

		// Convert to byte[] and return
		return tobyteArray(list);
	}

	public static byte[] readBytes(InputStream is, long len) throws IOException {

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

	private static byte[] tobyteArray(List<Byte> list) {

		// Convert List<Byte> to byte[] and return
		byte[] bytes = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			bytes[i] = list.get(i);
		}

		return bytes;
	}
}
