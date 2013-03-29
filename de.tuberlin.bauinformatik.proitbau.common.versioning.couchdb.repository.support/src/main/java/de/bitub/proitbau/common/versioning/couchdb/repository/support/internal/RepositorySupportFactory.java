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
package de.bitub.proitbau.common.versioning.couchdb.repository.support.internal;

import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.ektorp.impl.StdCouchDbConnector;

import com.google.common.collect.Maps;

import de.bitub.proitbau.common.versioning.couchdb.repository.support.BindingGraphRepositorySupport;
import de.bitub.proitbau.common.versioning.couchdb.repository.support.iBindingGraphRepositorySupport;

public class RepositorySupportFactory implements IAdapterFactory {
	
	private final Map<String, iBindingGraphRepositorySupport> bindingGraphRepositorySupportRegistry = Maps
		.newHashMapWithExpectedSize(5);
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof StdCouchDbConnector) {
			final String databaseName = ((StdCouchDbConnector) adaptableObject).getDatabaseName();
			
			if (adapterType.equals(iBindingGraphRepositorySupport.class)) {
				iBindingGraphRepositorySupport bindingGraphModelRepositorySupport =
					this.bindingGraphRepositorySupportRegistry.get(databaseName);
				if (bindingGraphModelRepositorySupport == null) {
					bindingGraphModelRepositorySupport = new BindingGraphRepositorySupport((StdCouchDbConnector) adaptableObject);
					this.bindingGraphRepositorySupportRegistry.put(databaseName, bindingGraphModelRepositorySupport);
				}
				return bindingGraphModelRepositorySupport;
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iBindingGraphRepositorySupport.class,
		};
	}
	
}
