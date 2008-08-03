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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides common functionality required for custom class loaders
 * of Nebula Framework.
 * <p>
 * Specifically, this class provide routines
 * to identify untrusted code from remote jobs and to mark such code
 * with a code base, and to prohibit loading of several
 * protected classes of the framework itself.
 * <p>
 * Allowing such classes to be accessed by untrusted code
 * may lead to security issues, and thus, all custom class loaders
 * use this functionality for security purposes.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public abstract class AbstractNebulaClassLoader extends SecureClassLoader {
	
	private static final Log log = LogFactory
			.getLog(AbstractNebulaClassLoader.class);
	
	/**
	 * CodeBase URL for remote loaded classes.
	 */
	protected static final String REMOTE_CODEBASE =  "http://codebase.nebulaframework.org/usercode/";
	
	/**
	 * CodeSource for remote loaded classes.
	 */
	protected static final CodeSource REMOTE_CODESOURCE;
	
    /**
     * Initializes the REMOTE_CODESOURCE field.
     */
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
    protected static final String[] PROHIBITED_PACKAGES = {
            "org.nebulaframework.grid.cluster",
            "org.nebulaframework.grid.service",
            "org.nebulaframework.discovery",
            "org.nebulaframework.configuration",
            "org.nebulaframework.ui"
    };
    
    /**
     * The classes specified in this will not be accessible 
     * through instances of this Class Loader.
     */
    protected static final String[] PROHIBITED_CLASSES = {
        "org.nebulaframework.grid.Grid",
    };


	/**
	 * No-args Constructor
	 * 
	 * @throws SecurityException if security policy does not allow ClassLoader creation
	 */
	public AbstractNebulaClassLoader() throws SecurityException {
		super();
	}

	/**
	 * Constructs a new instance of this class, bound to the
	 * specified parent ClassLoader.
	 * 
	 * @param parent Parent ClassLoader
	 * @throws SecurityException if security policy does not allow ClassLoader creation
	 */
	public AbstractNebulaClassLoader(ClassLoader parent) throws SecurityException {
		super(parent);
	}

    /**
     * Checks a given class name against a pre-defined set of
     * prohibited packages and classes. If identified as a 
     * prohibited class, this method throws {@link SecurityException}.
     * <p>
     * This method is used by Nebula Custom ClassLoaders to disallow
     * remote code access to several important classes of the
     * framework, which may lead to security issues otherwise.
     * 
     * @param name name of class
     * @throws SecurityException if class is prohibited
     */
    protected void checkProhibited(String name) throws SecurityException {
    	
    		// Check for Prohibited Packages
            for (String pkg : PROHIBITED_PACKAGES) {
                    if (name.startsWith(pkg)) {
                		log.warn("Attempted to access prohibited package : " + pkg);
                        throw new SecurityException("Package " + pkg + " is not accessible");
                    }
            }
            
            // Check for Prohibited Classes
            for (String cls : PROHIBITED_CLASSES) {
                if (name.equals(cls)) {
                		log.warn("Attempted to access prohibited class : " + name);
                        throw new SecurityException("Class " + cls + " is not accessible");
                }
        }
    }


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected abstract Class<?> findClass(String name) throws ClassNotFoundException;
	
	
}

