package de.bitub.proitbau.core.couchdb.manager.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class CouchDBConnectionManagerActivator extends Plugin {
	
	private static CouchDBConnectionManagerActivator plugin;
	
	public static CouchDBConnectionManagerActivator getDefault() {
		return CouchDBConnectionManagerActivator.plugin;
	}
	
	/**
	 * Returns the ID of this plug-in from the manifest file.
	 * 
	 * @return ID of this plug-in from the manifest file
	 */
	public String getId() {
		return this.getBundle().getSymbolicName();
		
	}
	
	public void log(final int severity, final String message) {
		final IStatus status = new Status(severity, this.getId(), message);
		this.getLog().log(status);
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		CouchDBConnectionManagerActivator.plugin = this;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		CouchDBConnectionManagerActivator.plugin = null;
	}
	
}
