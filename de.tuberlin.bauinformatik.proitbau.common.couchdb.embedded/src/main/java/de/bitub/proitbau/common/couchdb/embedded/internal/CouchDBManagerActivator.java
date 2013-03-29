/*******************************************************************************
 * Author:		"Vladislav Fedotov"
 * Written:		2013
 * Project:		Setvers
 * E-mail:		vladislav.fedotov@tu-berlin.de
 * Company:		TU Berlin
 * Version:		1.0
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladislav Fedotov - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.couchdb.embedded.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class CouchDBManagerActivator extends Plugin {
	
	private static CouchDBManagerActivator plugin;
	
	public static CouchDBManagerActivator getDefault() {
		return CouchDBManagerActivator.plugin;
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
		CouchDBManagerActivator.plugin = this;
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		CouchDBManagerActivator.plugin = null;
	}
	
}
