package org.nebulaframework.deployment.classloading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.util.io.IOSupport;
import org.nebulaframework.util.profiling.annotations.ProfileExecution;

public class GridArchiveClassLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(GridArchiveClassLoader.class);

	private ClassLoader parent;
	private File archiveFile;

	public GridArchiveClassLoader(File archiveFile) {
		super();
		
		if (!archiveFile.isFile()) {
			throw new IllegalArgumentException("Invalid Archive, not a file");
		}
		
		this.archiveFile = archiveFile;
	}

	public GridArchiveClassLoader(GridArchive archive) {
		super();
		try {
			this.archiveFile = createTempArchiveFile(archive);
		} catch (IOException e) {
			throw new RuntimeException(
					"Cannot create class loader for Archive", e);
		}
	}

	public GridArchiveClassLoader(File archiveFile, ClassLoader parent) {
		this(archiveFile);
		this.parent = parent;
	}

	public GridArchiveClassLoader(GridArchive archive, ClassLoader parent) {
		this(archive);
		this.parent = parent;

	}

	protected File createTempArchiveFile(GridArchive archive)
			throws IOException {

		File archiveFile = File.createTempFile("archivetemp", "nar");
		archiveFile.deleteOnExit();

		FileOutputStream fout = new FileOutputStream(archiveFile);
		fout.write(archive.getBytes());
		fout.flush();
		fout.close();

		return archiveFile;
	}

	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		
		// Try to find already loaded class
		Class<?> cls = findLoadedClass(name);
		if (cls != null) return cls;

		// If not loaded yet,
		try {

			// Delegate to Super Class
			try {
				cls = super.loadClass(name);
				if (cls != null)
					return cls;
			} catch (ClassNotFoundException e) {
				// Ignore
			}

			// If not found by super class, attempt GridArchive loading
			cls = findClass(name);
			
			if (cls != null) {
				log.debug("GridArchiveClassLoader found class " + name);
				return cls;
			}
		} catch (ClassNotFoundException e) {
			// Ignore
		}

		// If not found by super class or by GridArchive loading, try GridNode Remote Loading
		if (parent != null) {
			return parent.loadClass(name);
		} else {
			throw new ClassNotFoundException("Unable to find class " + name);
		}
	}

	@ProfileExecution
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			
			// Convert class name to file name
			String fileName = name.replaceAll("\\.", "/") + ".class";

			byte[] bytes = findInArchive(fileName);
			return defineClass(name, bytes, 0, bytes.length);

		} catch (Exception e) {
			throw new ClassNotFoundException("Unable to locate class", e);
		}
	}

	protected byte[] findInJarStream(InputStream inStream, String fileName)
			throws IOException {

		ZipInputStream zipInStream = new ZipInputStream(inStream);
		ZipEntry entry = null;

		while ((entry = zipInStream.getNextEntry()) != null) {
			// If match found
			//log.debug("findInJarStream Comparing : " + entry.getName() + " | " + fileName);
			if (entry.getName().equals(fileName)) {
				log.debug("Match Found");
				return IOSupport.readBytes(zipInStream, entry.getSize());
			}
		}
		return null;
	}

	protected byte[] findInArchive(String fileName) throws IOException,
			ClassNotFoundException {

		ZipFile archive = new ZipFile(archiveFile);

		ZipEntry entry = archive.getEntry(fileName);

		if (entry == null) { // Unable to find file in archive
			try {
				// Attempt to look in libraries
				Enumeration<? extends ZipEntry> enumeration = archive.entries();
				while (enumeration.hasMoreElements()) {
					ZipEntry zipEntry = enumeration.nextElement();
					if (zipEntry.getName().contains(GridArchive.NEBULA_INF)
							&& zipEntry.getName().endsWith(".jar")) {

						log.debug("[GridArchiveClassLoader] Looking in Jar Lib <"+ zipEntry.getName() + ">");

						byte[] bytes = findInJarStream(archive.getInputStream(zipEntry), fileName);
						if (bytes != null) {
							log.debug("[GridArchiveClassLoader] found class in JAR Library " + fileName);
							return bytes;
						}
					}
				}
			} catch (Exception e) {
				// Ignore
			}

			throw new ClassNotFoundException("No such file as " + fileName);
		}
		else { // Entry not null, Found Class
			log.debug("[GridArchiveClassLoader] found class at " + fileName);
			return IOSupport.readBytes(archive.getInputStream(entry));
		}
	}

}
