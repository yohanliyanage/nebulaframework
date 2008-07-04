package org.nebulaframework.deployment.classloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	public GridArchiveClassLoader(File archiveFile) {
		super();
		this.archiveFile = archiveFile;
	}

	public GridArchiveClassLoader(GridArchive archive) {
		super();
		try {
			this.archiveFile = createTempArchiveFile(archive);
		} catch (IOException e) {
			throw new RuntimeException("Cannot create class loader for Archive", e);
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


	protected File createTempArchiveFile(GridArchive archive) throws IOException {
		
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
		if(cls != null) {
			return cls;
		}
		else {
			
			try {
				cls = findClass(name);
				if (cls != null) {
					log.debug("GridArchiveClassLoader found class " + name);
					return cls;
				}
			} catch (ClassNotFoundException e) {
				// Ignore
			}
			
			return (parent!=null)? parent.loadClass(name) : super.loadClass(name);
		}
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			
			String fileName = name.replaceAll("\\.", "/") + ".class";
			
			byte[] bytes = null;
			
			if (archiveFile.isFile()) {
				bytes = findInFile(fileName);
			}
			else if (archiveFile.isDirectory()) {
				bytes = findInFolder(fileName);
			}
			
			return defineClass(name, bytes, 0, bytes.length);
			
		} catch (Exception e) {
			throw new ClassNotFoundException("Unable to locate class", e);
		}
	}

	protected byte[] findInFolder(String fileName) throws IOException, ClassNotFoundException {
		File classFile = new File(archiveFile, fileName);
		
		if (!classFile.exists()) {
			throw new ClassNotFoundException("No such file as " + fileName);
		}
		
		log.debug("GridArchiveClassLoader found class at " + fileName);
		
		return IOSupport.readBytes(new FileInputStream(classFile));
	}

	protected byte[] findInFile(String fileName) throws IOException, ClassNotFoundException {
		
		ZipFile archive = new ZipFile(archiveFile);
		
		ZipEntry entry = archive.getEntry(fileName);
		
		// Unable to find file in archive
		if (entry==null) {
			throw new ClassNotFoundException("No such file as " + fileName);
		}
		
		log.debug("GridArchiveClassLoader found class at " + fileName);
		
		return IOSupport.readBytes(archive.getInputStream(entry));
	}

}
