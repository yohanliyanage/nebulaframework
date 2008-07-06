package org.nebulaframework.deployment.classloading.node.exporter;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.util.io.IOSupport;


public class GridNodeClassExporterImpl implements GridNodeClassExporter {

	private static Log log = LogFactory.getLog(GridNodeClassExporterImpl.class);
	
	public byte[] exportClass(String name) throws ClassNotFoundException {
		try {
			
			String resName = "/" + name.replaceAll("\\.", "/") + ".class";
			InputStream is = Class.forName(name).getResourceAsStream(resName);
			
			if (is==null) log.warn("InputStream is NULL for " + resName);

			log.debug("[GridNodeClassExporter] Exporting " + name);
			return IOSupport.readBytes(is);
		}
		catch (IOException ex) {
			log.warn("Unable to export class due to IOException", ex);
			throw new ClassNotFoundException("Unable to export class due to IOException", ex);
		}
		catch (NullPointerException ex) {
			log.warn("Unable to locate class with in Node", ex);
			throw new ClassNotFoundException("Unable to locate class with in Node", ex);
		}
	}

}
