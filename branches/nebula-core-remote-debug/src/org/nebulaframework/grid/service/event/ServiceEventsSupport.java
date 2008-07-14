package org.nebulaframework.grid.service.event;

import java.util.ArrayList;
import java.util.List;

import org.nebulaframework.grid.service.message.ServiceMessage;

//TODO FixDoc : Singleton
public class ServiceEventsSupport {

	private static final ServiceEventsSupport instance = new ServiceEventsSupport();
	
	private List<ServiceHookElement> hooks = new ArrayList<ServiceHookElement>();
	
	
	private ServiceEventsSupport() {
		// No instantiation : Singleton
	}
	
	public static ServiceEventsSupport getInstance() {
		return instance; 
	}
	
	public void addServiceHook(ServiceEvent event, ServiceHookCallback callback) {
		this.hooks.add(new ServiceHookElement(event,callback));
	}
	
	public void removeServiceHook(ServiceEvent event, ServiceHookCallback callback) {
		this.hooks.remove(new ServiceHookElement(event, callback));
	}

	public void onServiceMessage(ServiceMessage message) {
		notifyHooks(message);
	}
	
	private void notifyHooks(final ServiceMessage message) {
		
		new Thread(new Runnable() {
			public void run() {
				for (ServiceHookElement hook : hooks) {
					if (hook.getEvent().isEvent(message)) {
						hook.getCallback().onServiceEvent();
					}
				}
			}
		}).start();
	}
	// TODO FixDoc
	private static class ServiceHookElement {
		private ServiceEvent event;
		private ServiceHookCallback callback;

		public ServiceHookElement(ServiceEvent event, ServiceHookCallback callback) {
			super();
			this.event = event;
			this.callback = callback;
		}

		public ServiceEvent getEvent() {
			return event;
		}

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
