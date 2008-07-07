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

package org.nebulaframework.core.job.archive;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.nebulaframework.deployment.classloading.GridArchiveClassLoader;
import org.nebulaframework.util.hashing.SHA1Generator;
import org.nebulaframework.util.io.IOSupport;
import org.springframework.util.Assert;

/**
 * Represents an archived {@code GridJob}. An archived {@code GridJob} is a special
 * format of {@code JAR}, which is referred to as {@code Nebula Archive}, identified by
 * the extension {@code .nar}.
 * <p>
 * Nebula Archives allow required libraries ({@code .jar}) to be included within the 
 * archive itself, unlike the standard {@code JAR} file format.
 * <p>
 * The structure of Nebula Archive is as follows:
 *  <code>
 *  <pre>
 *   META-INF/
 *           |
 *           - Manifest.mf
 *           - ...
 *   NEBULA-INF/
 *             |
 *             - lib /
 *                   |
 *                   - library1.jar
 *                   - ...
 *             - ...
 *   yourpackage /
 *               |
 *               - ...
 *   your.class
 *   ...
 *  </pre>
 *  </code>
 * The libraries are to be packaged in {@code NEBULA-INF/lib} directory. A special 
 * class loader is used by Nebula Framework to load required classes from the
 * libraries included in a Nebula Archive. Refer to {@link GridArchiveClassLoader}
 * for additional information regarding class loading.
 * <p>
 * {@code GridArchive} keeps the {@code byte[]} of a {@code .nar} file, and SHA1 
 * Hash for the {@code byte[]}, for verification purposes. The hash for the 
 * {@code byte[]} is generated at the time of creation of the {@code GridArchive} 
 * instance for a {@code .nar} file. At each remote node, this hash will be compared 
 * with a SHA1-Hash generated at the time, to ensure that the {@code GridArchive} 
 * contains valid data.
 * <p>
 * To instantiate a {@code GridArchive}, use the following factory methods
 * <ul>
 * 	<li>{@link #fromFile(File)}</li>
 * </ul>
 * <p>
 * This class implements {@link Externalizable} interface, instead of {@link Serializable}
 * to improve performance in communications, by reducing the data transfer amount and
 * serialization time [Grosso, W. 2001. "Java RMI", Section 10.7.1].
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridJob
 * @see GridArchiveClassLoader
 */
public class GridArchive implements Externalizable {

	private static Log log = LogFactory.getLog(GridArchive.class);

	/**
	 * Default directory name for {@code NEBULA-INF} inside a 
	 * Nebula Archive file.
	 */
	public static final String NEBULA_INF = "NEBULA-INF"; 
	
	/**
	 * Default path for JAR Libraries inside a Nebula Archive 
	 * file.
	 */
	public static final String LIBRARY_PATH = NEBULA_INF + "/lib";
	
	private String[] jobClassNames;	// Class Names of GridJobs in .nar
	private byte[] bytes;			// bytes of .nar file
	private String hash;			// SHA1 Hash for bytes
	
	/**
	 * Constructs a {@code GridArchive} with given bytes of 
	 * {@code .nar} file, and the names of {@code GridJob}
	 * classes.
	 * <p>
	 * SHA-1 Hash for the given {@code byte[]} will be calculated
	 * during the instantiation process.
	 * <p>
	 * Note that the constructor is of <b>{@code protected}</b> scope. To
	 * instantiate this type, use the factory method 
	 * {@link #fromFile(File)}.
	 * 
	 * @param bytes			{@code byte[]} of  {@code .nar} file
	 * @param jobClassNames {@code String[]} of fully qualified class names of
	 * {@code GridJob} classes inside the .nar file.
	 * 
	 * @see #fromFile(File)
	 */
	protected GridArchive(byte[] bytes, String[] jobClassNames) {
		super();
		
		// Assertions
		Assert.notNull(bytes);
		Assert.notNull(jobClassNames);
		
		this.bytes = bytes;
		this.jobClassNames = jobClassNames;
		
		// Generate SHA1 Hash for bytes
		hash = SHA1Generator.generateAsString(bytes);
	}

	/**
	 * Returns the bytes of the  {@code .nar} file, represented by this
	 * {@code GridArchive} instance.
	 * 
	 * @return bytes of  {@code .nar} file
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Returns the SHA-1 Hash generated at the time of creation of
	 * this {@code GridArchive}, for the bytes of  {@code .nar} file.
	 * 
	 * @return SHA-1 Hash as {@code String}
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Returns an array of {@code String}s, which contains
	 * fully qualified class names of {@code GridJob}s 
	 * inside the  {@code .nar} file.
	 * 
	 * @return Class names of {@code GridJob}s inside  
	 * {@code .nar} file
	 */
	public String[] getJobClassNames() {
		return jobClassNames;
	}

	/**
	 * <b>Factory Method</b> to create a {@code GridArchive} 
	 * instance for the given {@code File} instance of a 
	 * {@code .nar} file.
	 * 
	 * @param file {@code File} instance of {@code .nar} file.
	 * 
	 * @return {@code GridArchive} instance for given {@code .nar} file.
	 * 
	 * @throws GridArchiveException if processing of {@code File} failed.
	 */
	public static GridArchive fromFile(File file) throws GridArchiveException {
		try {
			// Assertions
			Assert.notNull(file);
			
			// Verify file integrity
			if (!verify(file)) {
				throw new SecurityException("Grid Archive Verification failed of " + file);
			}
			
			// Detect the GridJob Class names
			String[] jobClassNames = findJobClassNames(file);
			
			// Read byte[] from File
			byte[] bytes = IOSupport.readBytes(new FileInputStream(file));
			
			// Create and return GridArchive
			return new GridArchive(bytes, jobClassNames);
			
		} catch (Exception e) {
			throw new GridArchiveException("Cannot create Grid Archive", e);
		}
	}
	
	/**
	 * Verifies the integrity of the given {@code File},
	 * as a Nebula Archive.
	 *  
	 * @param file {@code File} to be verified.
	 * @return if success, {@code true}, otherwise {@code false}.
	 */
	protected static boolean verify(File file) {
		// TODO Implement to verify the NAR
		return true;
	}	
	
	/**
	 * Returns the {@code GridJob} classes with in the given {@code .nar} file.
	 * Uses {@link GridArchiveClassLoader}.
	 * 
	 * @param file {@code File} instance for {@code .nar} file.
	 * 
	 * @return Fully qualified class names of {@code GridJob} classes in the file.
	 * 
	 * @throws IOException if occurred during File I/O operations
	 * 
	 * @see GridArchiveClassLoader
	 */
	protected static String[] findJobClassNames(File file) throws IOException {
		
		// Instantiate ClassLoader for given File
		GridArchiveClassLoader classLoader = new GridArchiveClassLoader(file);
		
		// Find ClassNames of all classes inside the file (except in NEBULA-INF)
		// Content inside .jar files will not be processed
		String[] allClassNames = getAllClassNames(file);
		
		// Holds Class<?> instances loaded by ClassLoader, for all classes
		List<String> jobClassNames = new ArrayList<String>();
		
		for (String className : allClassNames) {
			try {
				// Load each Class and check if its a GridJob Class
				if (isGridJobClass(classLoader.loadClass(className))) {
					jobClassNames.add(className);
				}
			} catch (ClassNotFoundException e) {
				// Log and continue with rest
				log.debug("[GridArchive] Unable to load class " + className);
			}
		}
		return jobClassNames.toArray(new String[] {});
	}	
	
	/**
	 * Detects all classes inside the given {@code .nar} file and
	 * returns an array of fully qualified class name of each class,
	 * as {@code String}.
	 *  
	 * @param file {@code .nar File}
	 * 
	 * @return Fully qualified class names classes in {@code File}
	 * 
	 * @throws IOException if occurred during File I/O operations
	 */
	protected static String[] getAllClassNames(File file) throws IOException {
		
		// Holds Class Names
		List<String> names = new ArrayList<String>();
		
		// Create ZipArchive for File
		ZipFile archive = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = archive.entries();
		
		// Read each entry in archive
		while(entries.hasMoreElements()) {
			
			ZipEntry entry = entries.nextElement();
			
			// Ignore Directories
			if (entry.isDirectory()) continue;
			
			// Ignore content in NEBULA-INF
			if (entry.getName().startsWith(GridArchive.NEBULA_INF)) {
				continue;
			}
			
			// Add each file which is a valid class file to list
			if (isClass(entry.getName())) {
				names.add(toClassName(entry.getName()));
			}
		}
		return names.toArray(new String[] {});
	}	
	
	/**
	 * Detects whether a given {@code Class} implements {@code GridJob interface},
	 * using Reflection API.
	 * 
	 * @param cls {@code Class} to be checked
	 * @return if {@code GridJob} class, {@code true}, otherwise {@code false}
	 */
	protected static boolean isGridJobClass(Class<?> cls) {
		
		// Get all interfaces, and process each
		for(Class<?> iface : cls.getInterfaces()) {
			// If class implements GridJob interface
			if (iface.getName().equals(GridJob.class.getName())) {
				log.debug("[GridArchive] Found GridJob Class " + cls.getName());
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Converts the given file name to fully qualified class name.
	 * For example, for '{@code org/nebulaframework/Grid.class}', this method
	 * returns '{@code org.nebulaframework.Grid}'.
	 * 
	 * @param fileName File name to be converted
	 * @return Fully qualified Class Name
	 */
	protected static String toClassName(String fileName) {
		String name = fileName.substring(0, fileName.length() - ".class".length());
		return name.replaceAll("\\/|\\\\", "."); // Replace all path separators (Win/Linux)
	}

	/**
	 * Returns {@code true} if the given file name (path) identifies a 
	 * class file. The identification is done by checking if the file name
	 * ends with '{@code .class}'.
	 * 
	 * @param fileName File Name to be checked
	 * @return if class, {@code true}, otherwise {@code false}
	 */
	protected static boolean isClass(String fileName) {
		return fileName.endsWith(".class");
	}

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int size = in.readInt();
		this.bytes = new byte[size];
		in.read(bytes,0, size);
		this.hash = in.readUTF();
		this.jobClassNames = (String[]) in.readObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(bytes.length);
		out.write(bytes);
		out.writeUTF(hash);
		out.writeObject(jobClassNames);
	}

}
