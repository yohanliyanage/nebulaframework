package org.nebulaframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.AccessController;
import java.security.PrivilegedAction;

// TODO FixDoc
public class NebulaObjectInputStream extends ObjectInputStream {

	protected ClassLoader classLoader = null;
	
	public NebulaObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


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
		
		return Class.forName(desc.getName(), true, classLoader);
		
	}

	
}
