package org.nebulaframework.deployment.classloading;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

// TODO FixDoc
public abstract class AbstractNebulaClassLoader extends SecureClassLoader {
	
	/**
	 * CodeBase URL for remote loaded classes.
	 */
	protected static final String REMOTE_CODEBASE =  "http://codebase.nebulaframework.org/usercode/";
	
	/**
	 * CodeSource for remote loaded classes.
	 */
	protected static final CodeSource REMOTE_CODESOURCE;
	
	static {
		try {
			// Initialize CodeSource
			REMOTE_CODESOURCE = new CodeSource(new URL(REMOTE_CODEBASE), (Certificate[]) null);
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError("Malformed Remote Code Base");
		}
	}
	
	/**
	 * The classes belonging to these packages and sub-packages
	 * of these packages, will not be accessible through instances
	 * of this Class Loader.
	 */
	// TODO Ensure List is Up to Date
	protected static final String[] PROHIBITED_PACKAGES = {
		"org.nebulaframework.grid",
		"org.nebulaframework.deployment",
		"org.nebulaframework.discovery",
		"org.nebulaframework.configuration"
	};
	
	
	public AbstractNebulaClassLoader() throws SecurityException {
		super();
	}

	public AbstractNebulaClassLoader(ClassLoader parent) throws SecurityException {
		super(parent);
	}

	
	// TODO FixDoc
	protected void checkProhibited(String name) throws SecurityException {
		for (String pkg : PROHIBITED_PACKAGES) {
			if (name.startsWith(pkg)) {
				throw new SecurityException("Package " + pkg + " is not accessible");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract Class<?> findClass(String name) throws ClassNotFoundException;
	
	
}

