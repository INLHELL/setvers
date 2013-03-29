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

import org.eclipse.core.runtime.IAdapterFactory;

import de.bitub.proitbau.common.couchdb.embedded.CouchDBManager;
import de.bitub.proitbau.common.couchdb.embedded.iCouchDBManager;

public class CouchDBManagerFactory implements IAdapterFactory {
	
	private iCouchDBManager couchDBManager;
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (iCouchDBManager.class.equals(adapterType)) {
			if (this.couchDBManager == null) {
				this.couchDBManager = new CouchDBManager();
			}
			return this.couchDBManager;
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iCouchDBManager.class
		};
	}
	
}
