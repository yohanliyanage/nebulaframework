package org.nebulaframework.deployment.classloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.archive.GridArchive;
import org.nebulaframework.util.io.IOSupport;

public class GridArchiveClassLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(GridArchiveClassLoader.class);

	private ClassLoader parent;
	private File archiveFile;
	private List<File> libFileCache;
	
	public GridArchiveClassLoader(File archiveFile) {
		super();
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
		Class<?> cls = findLoadedClass(name);
		
		if (cls != null) return cls;
		
		try {
			
			// Delegate to Super Class
			try {
				cls = super.loadClass(name);
				if (cls!=null) return cls;
			} catch (ClassNotFoundException e) {
				//Ignore
			}

			// If still not found, attempt GridArchive loading
			
			cls = findClass(name);
			if (cls != null) {
				log.debug("GridArchiveClassLoader found class " + name);
				return cls;
			}
		} catch (ClassNotFoundException e) {
			// Ignore
		}

		// Still not found, try GridNode Remote Loading
		if (parent != null) {
			return parent.loadClass(name);
		}
		else {
			throw new ClassNotFoundException("Unable to find class " + name);
		}
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {

			String fileName = name.replaceAll("\\.", "/") + ".class";

			byte[] bytes = null;

			if (archiveFile.isFile()) {
				bytes = findInFile(fileName);
			} else if (archiveFile.isDirectory()) {
				bytes = findInFolder(fileName);
			}

			return defineClass(name, bytes, 0, bytes.length);

		} catch (Exception e) {
			throw new ClassNotFoundException("Unable to locate class", e);
		}
	}

	protected byte[] findInFolder(String fileName) throws IOException,
			ClassNotFoundException {
		File classFile = new File(archiveFile, fileName);

		if (!classFile.exists()) {

			// Attempt to look in libraries

			File libsDir = new File(archiveFile, GridArchive.LIBRARY_PATH);
			if (libsDir.exists() && libsDir.isDirectory()) {
				File[] files = libsDir.listFiles();

				// Process each Jar in Library Path
				for (File file : files) {

					if (file.isFile() && file.getName().endsWith(".jar")) {

						byte[] result = null;

						try {
							result = findInJar(new JarFile(file), fileName);
						} catch (Exception e) {
							// Ignore
						}

						if (result != null) {
							// If found
							log.debug("GridArchiveClassLoader Found Class + "
									+ fileName + " in Library : "
									+ file.getName());
							return result;
						}
					}
				}
			}

			throw new ClassNotFoundException("No such file as " + fileName);
		}

		log.debug("[GridArchiveClassLoader] found class at " + fileName);

		return IOSupport.readBytes(new FileInputStream(classFile));
	}

	protected byte[] findInJar(JarFile file, String fileName)
			throws IOException {

		JarEntry entry = file.getJarEntry(fileName);

		if (entry != null) {
			return IOSupport.readBytes(file.getInputStream(entry));
		}

		return null;
	}

	protected byte[] findInFile(String fileName) throws IOException,
			ClassNotFoundException {

		ZipFile archive = new ZipFile(archiveFile);

		ZipEntry entry = archive.getEntry(fileName);

		// Unable to find file in archive
		if (entry == null) {

			try {
				// Attempt to look in libraries
				
				if (libFileCache==null) {
					// Libraries not detected. Detect
					libFileCache = new ArrayList<File>();
					
					Enumeration<? extends ZipEntry> enumeration = archive.entries();
					while (enumeration.hasMoreElements()) {
						ZipEntry zipEntry = enumeration.nextElement();
						if (zipEntry.getName().contains(GridArchive.NEBULA_INF)
								&& zipEntry.getName().endsWith(".jar")) {
							
							log.debug("[GridArchiveClassLoader] Found Jar Lib <" + zipEntry.getName() + ">");
							
							File file = null;
							file = File.createTempFile("narlibjar", ".lib");
							file.deleteOnExit();
							FileOutputStream fout = new FileOutputStream(file);
							fout.write(IOSupport.readBytes(archive.getInputStream(zipEntry)));
							fout.flush();
							fout.close();
							
							// Add to cache
							libFileCache.add(file);
							
						}
					}					
				}
				
				for(File file : libFileCache) {
					byte[] bytes = findInJar(new JarFile(file), fileName);
					if (bytes!=null) {
						log.debug("[GridArchiveClassLoader] found class " + fileName + " in Library Cache");
						return bytes;
					}
				}
				
				
			} catch (Exception e) {
				//Ignore
			}

			throw new ClassNotFoundException("No such file as " + fileName);
		}
		log.debug("[GridArchiveClassLoader] found class at " + fileName);
		return IOSupport.readBytes(archive.getInputStream(entry));
	}

}
