package org.nebulaframework.core.job.archive.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.core.job.archive.GridArchiveException;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.util.io.IOSupport;
import org.springframework.util.Assert;

@SuppressWarnings("unchecked") /* Ignore Generics for clarity */
public class GridArchiveSupport {

	private static Log log = LogFactory.getLog(GridArchiveSupport.class);
	
	public static GridArchive createGridArchive(File file) throws GridArchiveException {
		
		try {
			// Assertions
			Assert.notNull(file);
			
			// Verify file integrity
			if (! verify(file)) {
				throw new SecurityException("Grid Archive Verification failed of " + file);
			}
			
			String[] jobClassNames = findJobClassNames(file);
			byte[] bytes = IOSupport.readBytes(new FileInputStream(file));
			
			return new GridArchive(bytes, jobClassNames);
			
		} catch (Exception e) {
			throw new GridArchiveException("Cannot create Grid Archive", e);
		}
	}
	
	private static boolean verify(File file) {
		// TODO Implement to verify the Jar
		return true;
	}
	
	private static String[] findJobClassNames(File file) throws IOException {
		
		GridArchiveClassLoader classLoader = new GridArchiveClassLoader(file);
		
		String[] allClassNames = getClassNames(file);
		
		List<Class> classes = new ArrayList<Class>();
		
		for (String className : allClassNames) {
			try {
				classes.add(classLoader.loadClass(className));
			} catch (ClassNotFoundException e) {
				log.debug("[GridArchive] Unable to load class " + className);
			}
		}
		
		return getGridJobClassNames(classes.toArray(new Class[] {}));
	}
	
	private static String[] getGridJobClassNames(Class[] classes) {
		List<String> gridJobClasses = new ArrayList<String>();
		
		
		// Use Reflection to determine if a class 
		// implements GridJob interface
		
		for (Class cls : classes) {
			for(Class iface : cls.getInterfaces()) {
				// If class implements GridJob interface
				if (iface.getName().equals(GridJob.class.getName())) {
					gridJobClasses.add(cls.getName());
					log.debug("[GridArchive] Found GridJob Class " + cls.getName());
				}
			}
		}
		
		return gridJobClasses.toArray(new String[] {});
	}

	private static String[] getClassNames(File file) throws IOException {
		
		List<String> names = new ArrayList<String>();
		
		ZipFile archive = new ZipFile(file);
		
		Enumeration<? extends ZipEntry> entries = archive.entries();
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			
			// Ignore Directories
			if (entry.isDirectory()) continue;
			
			// Process each file which is a valid class file name
			if (isClass(entry.getName())) {
				names.add(toClassName(entry.getName()));
			}
		}
		
		return names.toArray(new String[] {});
	}

	private static String toClassName(String fileName) {
		String name = fileName.substring(0, fileName.length()- ".class".length());
		return name.replaceAll("\\/|\\\\", "."); // Replace all path separators (Win/Linux)
	}

	private static boolean isClass(String fileName) {
		return fileName.endsWith(".class");
	}
	
	

}
