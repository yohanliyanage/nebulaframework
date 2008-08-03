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
package org.nebulaframework.grid.service.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.grid.cluster.node.GridNode;
import org.nebulaframework.grid.service.message.ServiceMessage;
import org.nebulaframework.grid.service.message.ServiceMessageType;

/**
 * {@link ServiceEventsSupport} allows the framework components to
 * execute a given code segment (wrapped in a {@code ServiceHookCallback} 
 * to be invoked when a given {@code ServiceEvent} triggers.
 * <p>
 * A {@code ServiceEvent} is said to be triggered when a matching 
 * {@link ServiceMessage} arrives at a {@code GridNode} or in case of a 
 * {@code ClusterManager}, when a {@code ServiceMessage} is written to 
 * the Service Topic.
 * <p>
 * <i>Singleton</i>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
public class ServiceEventsSupport {

	
	/** Singleton Instance */
	private static ServiceEventsSupport instance;
	
	private static Log log = LogFactory.getLog(ServiceEventsSupport.class);
	
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private List<ServiceHookElement> hooks = Collections.synchronizedList(new ArrayList<ServiceHookElement>());
		
	
	/**
	 * Private constructor prohibits external instantiation
	 * to ensure singleton state
	 */
	private ServiceEventsSupport() {
		// No instantiation : Singleton
	}
	
	public synchronized static void initialize() {
		if (instance==null) {
			instance = new ServiceEventsSupport();
		}
	}
	
	/**
	 * Returns the {@code ServiceEventsSupport} singleton instance.
	 * 
	 * @return singleton instance of {@code ServiceEventsSupport} 
	 */
	public static ServiceEventsSupport getInstance() {
		return instance; 
	}
	
	/**
	 * Invoked to reset the {@link ServiceEventsSupport}
	 * by destroying singleton instance. This is used
	 * by {@link GridNode}s to reset state when
	 * disconnected from Cluster.
	 */
	public synchronized static void destroySingleton() {
		instance = null;
	}
	
	/**
	 * Fires a <b>local</b> service event.
	 * 
	 * @param message ServiceMessage for event
	 */
	public static void fireServiceEvent(ServiceMessage message) {
		if (instance==null) throw new IllegalStateException("Not initialized");
		
		if (log.isDebugEnabled()){
			log.debug("[Events] Fired Local Event " + message.getType() + " : " + message.getMessage());
		}
		instance.onServiceMessage(message);
	}
	
	/**
	 * Adds a ServiceHook to the {@link ServiceEventsSupport}, of which the
	 * {@code callback} will be invoked when the given {@code event} 
	 * triggers.
	 * 
	 * @param event Service Event
	 * @param callback ServiceHookCallback
	 */
	public static void addServiceHook(ServiceEvent event, ServiceHookCallback callback) {
		if (instance==null) throw new IllegalStateException("Not initialized");
		getInstance().hooks.add(new ServiceHookElement(event,callback));
	}
	
	/**
	 * Convenience overloaded version of {@link #addServiceHook(ServiceEvent, ServiceHookCallback)},
	 * which allows users to specify the attributes of ServiceEvent directly, but without a
	 * message body.
	 * 
	 * @param serviceHookCallback callback
	 * @param types ServiceMessageTypes for hook
	 * @throws IllegalStateException if {@link ServiceEventsSupport} is not initialized
	 */
	public static void addServiceHook(ServiceHookCallback serviceHookCallback, ServiceMessageType... types) throws IllegalStateException {
		if (instance==null) throw new IllegalStateException("Not initialized");
		addServiceHook(serviceHookCallback, null, types);
	}
	
	/**
	 * Convenience overloaded version of {@link #addServiceHook(ServiceEvent, ServiceHookCallback)},
	 * which allows users to specify the attributes of ServiceEvent directly, with the
	 * message body.
	 * 
	 * @param serviceHookCallback callback
	 * @param message message body
	 * @param types ServiceMessageTypes for hook
	 * @throws IllegalStateException if {@link ServiceEventsSupport} is not initialized
	 */
	public static void addServiceHook(ServiceHookCallback serviceHookCallback, String message, ServiceMessageType... types) throws IllegalArgumentException {
		
		if (instance==null) throw new IllegalStateException("Not initialized");
		
		// No Event
		if ((types.length==0)&&(message==null)) {
			throw new IllegalArgumentException("Both message and event type is null.");
		}
		
		ServiceEvent event = new ServiceEvent();
		event.setMessage(message);
		
		for (ServiceMessageType type : types) {
			event.addType(type);
		}
		
		//Register Hook
		addServiceHook(event, serviceHookCallback);
	}
	
	
	/**
	 * Removes ServiceHook from the {@link ServiceEventsSupport}, for the given
	 * {@code event} and {@code callback}, if exists.
	 * 
	 * @param event Service Event
	 * @param callback ServiceHookCallback
	 */	
	public void removeServiceHook(ServiceEvent event, ServiceHookCallback callback) {
		this.hooks.remove(new ServiceHookElement(event, callback));
	}

	/**
	 * Invoked by {@code ServiceMessagesSupport} ({@code GridNode}) 
	 * or {@code ServiceMessageSender} ({@code ClusterManager}),
	 * when a message is received / dispatched.
	 * 
	 * @param message ServiceMessage
	 */
	public void onServiceMessage(ServiceMessage message) {
		notifyHooks(message);
	}
	
	/**
	 * Notifies all registered hooks about the {@code ServiceMessage}.
	 * Callbacks of matching events will be invoked during the process.
	 * 
	 * @param message ServiceMessage
	 */
	private void notifyHooks(final ServiceMessage message) {
		
		final ServiceHookElement[] elements = hooks.toArray(new ServiceHookElement[hooks.size()]);
		
		executorService.execute(new Runnable() {
			public void run() {
					for (final ServiceHookElement hook : elements ) {
						if (hook.getEvent().isEvent(message)) {
							
							// Execute on Thread Pool
							executorService.execute(new Runnable(){
								public void run() {
									try {
										hook.getCallback().onServiceEvent(message);
									} catch (RuntimeException e) {
										log.error("[Events] Exception in ServiceHookCallback - " + e.getMessage());
									}
								}
							});
						}
					}
			}
		});
	}

	/**
	 * Nested Class which represents a ServiceHookElement, which wraps
	 * a {@code ServicEvent} and a {@code ServiceHookCallback}, to be
	 * inserted into the Collection of ServiceHooks.
	 * 
	 * @author Yohan Liyanage
	 * @version 1.0
	 */
	private static class ServiceHookElement {
		
		private ServiceEvent event;
		private ServiceHookCallback callback;

		/**
		 * Constructs a ServiceHookElement which wraps the given {@code event}
		 * and {@code callback}.
		 * 
		 * @param event ServiceEvent
		 * @param callback ServiceHookCallback
		 */
		public ServiceHookElement(ServiceEvent event, ServiceHookCallback callback) {
			super();
			this.event = event;
			this.callback = callback;
		}

		/**
		 * Returns the ServiceEvent of this ServiceHookElement.
		 * 
		 * @return ServiceEvent
		 */
		public ServiceEvent getEvent() {
			return event;
		}

		/**
		 * Returns the ServiceHookCallback of this ServiceHookElement.
		 * 
		 * @return ServiceHookCallback
		 */
		public ServiceHookCallback getCallback() {
			return callback;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			
			if (!(obj instanceof ServiceHookElement)) {
				return false;
			}
			ServiceHookElement elm = (ServiceHookElement) obj;
			return (this.getCallback()==elm.getCallback() && this.getEvent().equals(elm.getEvent()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return 31 + this.getCallback().hashCode() * this.getEvent().hashCode() / 2;
		}

		
	}
}
