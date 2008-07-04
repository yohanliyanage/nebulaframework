package org.nebulaframework.deployment.classloading.node.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GridNodeClassExporterImpl implements GridNodeClassExporter {

	private static Log log = LogFactory.getLog(GridNodeClassExporterImpl.class);
	
	public byte[] exportClass(String name) throws ClassNotFoundException {
		try {
			
			log.debug("Export request received for class " + name);
			
			log.debug("Class.forName(" + name + ") = " + Class.forName(name));
			
			String resName = "/" + name.replaceAll("\\.", "/") + ".class";
			InputStream is = Class.forName(name).getResourceAsStream(resName);
			
			if (is==null) log.warn("InputStream is NULL for " + resName);
			
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
			log.debug("Exporting " + name);
			return bytes;
		}
		catch (IOException ex) {
			log.warn("Unable to export class due to IOException",ex);
			throw new ClassNotFoundException("Unable to export class due to IOException", ex);
		}
		catch (NullPointerException ex) {
			log.warn("Unable to locate class with in Node",ex);
			throw new ClassNotFoundException("Unable to locate class with in Node", ex);
		}
	}

}
