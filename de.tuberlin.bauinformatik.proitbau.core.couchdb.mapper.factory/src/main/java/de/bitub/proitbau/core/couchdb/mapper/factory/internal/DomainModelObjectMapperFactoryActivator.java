package de.bitub.proitbau.core.couchdb.mapper.factory.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class DomainModelObjectMapperFactoryActivator extends Plugin {
	
	private static DomainModelObjectMapperFactoryActivator plugin;
	
	public static DomainModelObjectMapperFactoryActivator getDefault() {
		return DomainModelObjectMapperFactoryActivator.plugin;
	}
	
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
		DomainModelObjectMapperFactoryActivator.plugin = this;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		DomainModelObjectMapperFactoryActivator.plugin = null;
		super.stop(context);
	}
	
}
