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

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.deployment.classloading.service.ClassLoadingService;
import org.nebulaframework.grid.cluster.manager.ClusterManager;
import org.springframework.util.Assert;

/**
 * {@code GridNodeClassLoader} allows to load classes remotely from the
 * {@code GridNode} which submitted the {@code GridJob} to the Grid. At worker
 * nodes, this class loader relies on the {@link ClassLoadingService}, which is
 * implemented at the {@link ClusterManager} as a remote service, to obtain the
 * required classes.
 * <p>
 * The {@code ClassLoadingService} then communicates with the {@code GridNode}
 * which submitted the {@code GridJob}, and obtains the required class, and
 * returns it to the requesting worker {@code GridNode}.
 * <p>
 * Furthermore, the {@code GridNodeClassLoader} keeps a local cache of loaded
 * classes, which is used to improve performance when required to load same
 * class multiple times, for each GridTask execution.
 * <p>
 * This local cache is implemented along with the support provided by the JVM to
 * load previously loaded classes from its own internal cache.
 * <p>
 * {@code GridNodeClassLoader} is supposed to be instantiated for each
 * {@code GridJob} executed at a node. The JobId is required to communicate with
 * the {@code ClassLoadingService}, to identify the node which originally
 * submitted the {@code GridJob}.
 * 
 * @author Yohan Liyanage
 * @version 1.0
 * 
 * @see GridArchiveClassLoader
 * @see ClassLoadingService
 */
public class GridNodeClassLoader extends AbstractNebulaClassLoader {

	private static Log log = LogFactory.getLog(GridNodeClassLoader.class);

	/**
	 * JobId for this GridNodeClassLoader instance
	 */
	protected String jobId; 
	
	/**
	 * OwnerId of the GridJob, if known (can be null)
	 */
	protected UUID ownerId;
	
	/**
	 * Allows to load class definitions from remote nodes.
	 */
	protected ClassLoadingService classLoadingService; 


	 /**
     * Constructs a {@code GridNodeClassLoader} for the given {@code GridJob},
     * using the specified {@code ClassLoadingService}.
     *
     * @param jobId
     *            JobId for which the {@code GridNodeClassLoader} is
     *            instantiated
     * @param classLoadingService
     *            Proxy for remote {@code ClassLoadingService} at
     *            {@code ClusterManager}
     *
     * @throws IllegalArgumentException
     *             if either {@code jobId} or {@code ClassLoadingService} is
     *             {@code null}
     */
    public GridNodeClassLoader(String jobId,
                    ClassLoadingService classLoadingService)
                    throws IllegalArgumentException {

            super();
    	
            Assert.notNull(jobId);
            Assert.notNull(classLoadingService);

            this.jobId = jobId;
            this.classLoadingService = classLoadingService;
    }
    
    /**
     * Constructs a {@code GridNodeClassLoader} for the given {@code GridJob},
     * using the specified {@code ClassLoadingService}.
     *
     * @param ownerId Job Owner Node Id
     * @param classLoadingService
     *            Proxy for remote {@code ClassLoadingService} at
     *            {@code ClusterManager}
     *
     * @throws IllegalArgumentException
     *             if either {@code jobId} or {@code ClassLoadingService} is
     *             {@code null}
     */
    public GridNodeClassLoader(UUID ownerId, 
                    ClassLoadingService classLoadingService)
                    throws IllegalArgumentException {

            super();
    	
            Assert.notNull(ownerId);
            Assert.notNull(classLoadingService);

            this.ownerId = ownerId;
            this.classLoadingService = classLoadingService;
    }
    
    /**
     * Constructs a {@code GridNodeClassLoader} for the given {@code GridJob},
     * using the specified {@code ClassLoadingService} and the parent
     * {@code ClassLoader}.
     *
     * @param jobId
     *            JobId for which the {@code GridNodeClassLoader} is
     *            instantiated
     * @param classLoadingService
     *            Proxy for remote {@code ClassLoadingService} at
     *            {@code ClusterManager}
     * @param parent
     *            the parent {@code ClassLoader}, or {@code null} if none
     *
     * @throws IllegalArgumentException
     *             if either {@code jobId} or {@code ClassLoadingService} is
     *             {@code null}
     */
    public GridNodeClassLoader(String jobId,
                    ClassLoadingService classLoadingService, final ClassLoader parent)
                    throws IllegalArgumentException {

            super(parent);
    	
            Assert.notNull(jobId);
            Assert.notNull(classLoadingService);

            this.jobId = jobId;
            this.classLoadingService = classLoadingService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	
		// Check if the class is Prohibited to be accessed
    	// (throws SecurityException in such cases)
		checkProhibited(name);
		
    	return super.loadClass(name);
    }

    
	/**
     * Attempts to find the class definition for the given class name, by first
     * searching the local cache, and then through the remote
     * {@link ClassLoadingService}.
     *
     * @param name
     *            binary name of the class
     *
     * @return The {@code Class<?>} object for requested class, if found
     *
     * @throws ClassNotFoundException
     *             if unable to find the class
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {

            log.debug("[GridNodeClassLoader] Finding Class : " + name);

            Class<?> cls = null;
            // Attempt remote load
            try {
                    log.debug("[GridNodeClassLoader] Attempt Remote Loading : " + name);
                    
                    // Get bytes for class from remote service
                    byte[] bytes = AccessController.doPrivileged(new PrivilegedExceptionAction<byte[]>() {

						@Override
						public byte[] run() throws ClassNotFoundException {
							
							// If we don't know owner
							if (ownerId==null) {
								return classLoadingService.findClass(jobId, name);
							}
							else { // If we know owner
								return classLoadingService.findClass(ownerId,name);
							}
						}
                    	
                    });
                    
                    
                    cls = defineClass(name, bytes, 0, bytes.length, REMOTE_CODESOURCE);
                    log.debug("[GridNodeClassLoader] Remote Loaded : " + name);

                    return cls;

            } catch (Exception ex) {
                    log.warn("[GridNodeClassLoader] Exception while Remote Loading ",
                                            ex);
                    throw new ClassNotFoundException(
                                    "Class not found due to Exception", ex);
            }
    }

  

}
