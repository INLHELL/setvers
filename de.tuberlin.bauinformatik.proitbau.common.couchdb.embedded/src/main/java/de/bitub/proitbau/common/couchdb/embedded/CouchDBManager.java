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
package de.bitub.proitbau.common.couchdb.embedded;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class CouchDBManager implements Runnable, iCouchDBManager {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(CouchDBManager.class);
	{
		CouchDBManager.logger.setLevel(Level.INFO);
	}
	
	private final int defaultCouchDBPort = 5984;
	
	public boolean isPortBusy() {
		boolean isPortBusy = false;
		try {
			@SuppressWarnings("unused")
			final Socket sock = new Socket("127.0.0.1", this.defaultCouchDBPort);
			isPortBusy = true;
		}
		catch (final IOException e) {
			isPortBusy = false;
		}
		return isPortBusy;
	}
	
	@Override
	public void run() {
		this.startServer();
	}
	
	public void startServer() {
		synchronized (this) {
			if (!this.isPortBusy()) {
				if (System.getProperty("os.name").startsWith("Mac")) {
					// start database
					try {
						final URL url =
							FileLocator.find(Platform.getBundle("de.tuberlin.bauinformatik.proitbau.common.couchdb.embedded"),
								new Path("lib/CouchDB Server.app/Contents/MacOS/"), null);
						final URL resolvedUrl = FileLocator.resolve(url);
						final String pathToCouchdb = resolvedUrl.getPath();
						final String[] args = new String[] {
							pathToCouchdb + "CouchDB Server"
						};
						
						Runtime.getRuntime().exec(args);
						while (!this.isPortBusy()) {
							this.notify();
						}
					}
					catch (final IOException e) {
						CouchDBManager.logger.error(e.getMessage());
					}
				}
				if (System.getProperty("os.name").startsWith("Win")) {
					try {
						final URL url =
							FileLocator.find(Platform.getBundle("de.tuberlin.bauinformatik.proitbau.common.couchdb.embedded"),
								new Path("lib/setup-couchdb-1.2.0_otp_R15B.exe"), null);
						final URL resolvedUrl = FileLocator.resolve(url);
						final String pathToCouchdb = resolvedUrl.getPath();
						Runtime.getRuntime().exec(pathToCouchdb);
						while (!this.isPortBusy()) {
							this.notify();
						}
					}
					catch (final IOException e) {
						CouchDBManager.logger.error(e.getMessage());
					}
				}
			}
		}
	}
}
