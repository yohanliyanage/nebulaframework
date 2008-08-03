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
package org.nebulaframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Custom Implementation of {@link ObjectInputStream}, which allows
 * to specify a ClassLoader instance to be used for resolving classes,
 * unlike the default implementation of JVM which uses the last 
 * user defined ClassLoader in stack.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class NebulaObjectInputStream extends ObjectInputStream {

	/**
	 * ClassLoader to be used
	 */
	protected ClassLoader classLoader = null;
	
	/**
	 * Constructs a {@link NebulaObjectInputStream} for 
	 * given InputStream.
	 * 
	 * @param in input stream
	 * @throws IOException if occurred during process
	 */
	public NebulaObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Sets the {@link ClassLoader} to be used for resolving
	 * classes.
	 * 
	 * @param classLoader ClassLoader
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	/**
	 * Invoked to resolve classes when reading a Object from
	 * the given input stream.
	 * 
	 * @param desc ObjectStreamClass description
	 */
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		
		// If no ClassLoader set
		if (classLoader == null) {
			
			// Attempt loading using Thread context ClassLoader
			ClassLoader contextLoader = AccessController.
					doPrivileged(new PrivilegedAction<ClassLoader>() {

				@Override
				public ClassLoader run() {
					return Thread.currentThread().getContextClassLoader();
				}
				
			});
			
			if (contextLoader!=null) {
				return Class.forName(desc.getName(), true, contextLoader);
			}
			
			
			// If no context class loader, delegate to superclass
			return super.resolveClass(desc);
		}
		
		// Load Class and return
		return Class.forName(desc.getName(), true, classLoader);
		
	}

	
}
