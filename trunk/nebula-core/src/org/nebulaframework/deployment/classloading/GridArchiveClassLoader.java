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
import org.springframework.util.Assert;

/**
 * {@code GridArchiveClassLoader} is responsible of loading classes from
 * {@code GridArchives}. This custom class loader is capable of loading classes
 * from the {@code byte[]} of {@code .nar} file, which is in the
 * {@code GridArchive}.
 * <p>
 * If this class was instantiated with a GridArchive (not physical file of
 * {@code .nar} file), it first writes the {@code byte[]} to a temporary  file,
 * for performance reasons (a physical file can be searched directly against
 * a given file path, where as a stream should be compared against each entry). 
 * Once the temporary file is created, it will be used as a physical 
 * {@code .nar} file.
 * <p>
 * {@code GridArchiveClassLoader} supports a parent class loader, which will be
 * used as a fall back option, if the {@code GridArchiveClassLoader} is not
 * capable of loading a class. Usually, a {@link GridNodeClassLoader} is
 * specified as the parent class loader, enabling remote node based class
 * loading if a class is not loadable from {@code GridArchive}.
 * <p>
 * The class loading strategy of {@code GridArchiveClassLoader} is as follows.
 * At loadClass request, this class checks if the class has already been loaded.
 * If so, it will be directly returned. If not, it will then attempt to load the
 * class through its super class, that is {@link java.lang.ClassLoader}. If
 * that also fails, then it will attempt to load the class from the
 * {@code GridArchive}.
 * <p>
 * When searching for the class in the {@code .nar} file, it first attempts to
 * convert the given class name to relevant filename and to locate the file
 * directly in the {@code .nar} file. If this fails, it then looks in the
 * {@code .jar} libraries available in the {@code .nar} file, and attempts to
 * locate the class inside each of the {@code .jar} file.
 * <p>
 * If all fails, finally it would fall back to the parent class loader, if one
 * was specified.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridArchive
 * @see GridNodeClassLoader
 */
public class GridArchiveClassLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(GridArchiveClassLoader.class);

	private ClassLoader parent; // Parent Class Loader
	private File archiveFile; // Physical File (User Specified / Temp)
	
	/**
	 * Constructs a {@code GridArchiveClassLoader} for the {@code .nar} file
	 * specified by the {@code archiveFile} argument.
	 * 
	 * @param archiveFile
	 *            {@code .nar} file
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code archiveFile} is {@code null} or is not a valid
	 *             {@code .nar} file.
	 * @throws SecurityException if ClassLoader creation is prohibited
	 * by the current class loader.
	 * 
	 */
	public GridArchiveClassLoader(File archiveFile)
			throws IllegalArgumentException {
		
		super();

		// Look for nulls
		Assert.notNull(archiveFile);

		if (!archiveFile.isFile()) { // If archiveFile is not a file
			throw new IllegalArgumentException("Invalid Archive, not a file");
		}

		this.archiveFile = archiveFile;
	}

	/**
	 * Constructs a {@code GridArchiveClassLoader} for the given
	 * {@code GridArchive}. The {@code byte[]} of the archive will be written
	 * to a temporary file, and it will be used as the source for class loading
	 * functionalities.
	 * 
	 * @param archive
	 *            {@code GridArchive}
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code archive} is {@code null}
	 * @throws RuntimeException
	 *             if IO errors occur during temporary file creation
	 */
	public GridArchiveClassLoader(GridArchive archive)
			throws IllegalArgumentException, RuntimeException {
		super();

		// Look for nulls
		Assert.notNull(archive);

		try {
			// Create Temporary File
			this.archiveFile = createTempArchiveFile(archive);
		} catch (IOException e) {
			throw new RuntimeException(
					"Cannot create class loader for Archive", e);
		}
	}

	/**
	 * 
	 * Constructs a {@code GridArchiveClassLoader} for the {@code .nar} file
	 * specified by the {@code archiveFile} argument, and the given
	 * {@code ClassLoader} as the parent class loader.
	 * 
	 * @param archiveFile
	 *            {@code .nar} file
	 * @param parent
	 *            parent {@code ClassLoader}. If this is {@code null}, it
	 *            would be same as using the
	 *            {@link #GridArchiveClassLoader(File)} constructor.
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code archiveFile} is {@code null} or is not a valid
	 *             {@code .nar} file.
	 */
	public GridArchiveClassLoader(File archiveFile, ClassLoader parent)
			throws IllegalArgumentException {
		this(archiveFile);
	
		this.parent = parent;
	}

	/**
	 * Constructs a {@code GridArchiveClassLoader} for the given
	 * {@code GridArchive} and the given {@code ClassLoader} as the parent class
	 * loader.
	 * 
	 * @param archive
	 *            {@code GridArchive}
	 * @param parent
	 *            parent {@code ClassLoader}, if this is {@code null}, it
	 *            would be same as using the
	 *            {@link #GridArchiveClassLoader(GridArchive)} constructor.
	 * 
	 * @throws IllegalArgumentException
	 *             if {@code archive} is {@code null}
	 * @throws RuntimeException
	 *             if IO errors occur during temporary file creation
	 */
	public GridArchiveClassLoader(GridArchive archive, ClassLoader parent)
			throws IllegalArgumentException, RuntimeException {
		this(archive);
		this.parent = parent;

	}

	/**
	 * Creates a temporary file which consists of the {@code byte[]} of a given
	 * {@code GridArchive}.
	 * 
	 * @param archive
	 *            {@code GridArchive}
	 * @return A {@code File} reference for new temporary file
	 * 
	 * @throws IOException
	 *             if IOException occurs during {@code File} handling
	 */
	protected File createTempArchiveFile(GridArchive archive)
			throws IOException {

		// Create Temp File
		File archiveFile = File.createTempFile("archivetemp", "nar");
		archiveFile.deleteOnExit(); // Mark to delete

		// Write the byte[]
		FileOutputStream fout = new FileOutputStream(archiveFile);
		fout.write(archive.getBytes());
		fout.flush();
		fout.close();

		return archiveFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException, SecurityException {
		
		try {
			// Delegate to Super Class
			// (which will call findClass)
			return super.loadClass(name, resolve);
			
		} catch (ClassNotFoundException e) {
			// Ignore : Super class cannot find
			if (log.isDebugEnabled()) {
				log.debug("GridArchiveClassLoader unable to load class " + name);
			}
		}

		// If not found by super class or by GridArchive loading
		// Note that we do not rely on java.lang.ClassLoader to invoke 
		// parent class loader.
		
		if (parent != null) {
			// Delegate to parent ClassLoader
			return parent.loadClass(name);
			
		} else {
			throw new ClassNotFoundException("Unable to find class " + name);
		}
	}

	/**
	 * Attempts to find the given Class with in the {@code GridArchive}. If
	 * found (either as direct class file or with in a {@code .jar} library
	 * inside {@code .nar} file), returns the Class instance for it.
	 * 
	 * @return the {@code Class<?>} instance for the class to be loaded
	 * 
	 * @throws ClassNotFoundException if unable to find the class
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {

			// Convert class name to file name
			String fileName = name.replaceAll("\\.", "/") + ".class";

			// Search in Archive | Exception if failed
			byte[] bytes = findInArchive(fileName);

			// If found, define class and return
			return defineClass(name, bytes, 0, bytes.length);

		} catch (Exception e) {
			throw new ClassNotFoundException("Unable to locate class", e);
		}
	}

	/**
	 * Internal method which does the search for class inside
	 * the {@code GridArchive}. First attempts locate the file directly in 
	 * the {@code .nar} file. If this fails, it then looks in the 
	 * {@code .jar} libraries available in the {@code .nar}
	 * file, and attempts to locate the class inside each of the {@code .jar}
	 * file.
	 * 
	 * @param fileName expected filename of Class file to be loaded
	 * 
	 * @return the {@code byte[]} for the class file
	 * 
	 * @throws IOException if IO errors occur during operation
	 * @throws ClassNotFoundException if unable to locate the class
	 */
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

						// Look in Jar File
						byte[] bytes = findInJarStream(archive
								.getInputStream(zipEntry), fileName);

						// If Found
						if (bytes != null) {
							log.debug("[GridArchiveClassLoader] found class in JAR Library "
										+ fileName);
							return bytes;
						}
					}
				}
			} catch (Exception e) {
				log.warn("[[GridArchiveClassLoader] Exception " +
						"while attempting class loading", e);
			}
			
			// Cannot Find Class
			throw new ClassNotFoundException("No such file as " + fileName);
			
		} else { // Entry not null, Found Class
			log.debug("[GridArchiveClassLoader] found class at " + fileName);
			
			// Get byte[] and return
			return IOSupport.readBytes(archive.getInputStream(entry));
		}
	}

	/**
	 * Searches given JAR file stream within the {@code GridArchive} to
	 * locate a specified class file. 
	 * <p>
	 * Note that as it is implemented to read from Zip file stream, 
	 * this requires each entry with in the JAR to be matched 
	 * against the required class file's expected path.
	 * <p>
	 * An alternate implementation would be to implement this to write each
	 * JAR file as a temporary file and then use direct path to find the 
	 * class file. However, this implementation proved to be much slower
	 * than the current implementation, due to slow disk access times.
	 *  
	 * @param inStream {@code InputStream} for the JAR file
	 * @param fileName expected filename of the Class to be found
	 * 
	 * @return The {@code byte[]} for the class file, if found, or {@code null}
	 * 
	 * @throws IOException if an IO error occurs during operation
	 */
	protected byte[] findInJarStream(InputStream inStream, String fileName)
			throws IOException {

		// Get ZipInputStream to unzip content
		ZipInputStream zipInStream = new ZipInputStream(inStream);
		
		ZipEntry entry = null;
		
		// Compare against each entry
		while ((entry = zipInStream.getNextEntry()) != null) {
			// If match found
			if (entry.getName().equals(fileName)) {
				log.debug("Match Found");
				return IOSupport.readBytes(zipInStream, entry.getSize());
			}
		}
		
		// Not Found, return null
		return null;
	}

}
