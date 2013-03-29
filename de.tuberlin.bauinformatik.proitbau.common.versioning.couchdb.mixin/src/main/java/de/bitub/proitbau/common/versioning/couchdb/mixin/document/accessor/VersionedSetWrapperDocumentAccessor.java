/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 03.09.2012
 * Project: Setvers
 * E-mail: vladislav.fedotov@tu-berlin.de
 * Company: TU Berlin
 * Version: 1.0
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Vladislav Fedotov - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor;

import org.ektorp.util.DocumentAccessor;

import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetWrapper;

public class VersionedSetWrapperDocumentAccessor implements DocumentAccessor {
	
	@Override
	public String getId(final Object o) {
		return this.cast(o).getUuid();
	}
	
	@Override
	public void setId(final Object o, final String id) {
		this.cast(o).setUuid(id);
	}
	
	@Override
	public String getRevision(final Object o) {
		return this.cast(o).getRevision();
	}
	
	@Override
	public void setRevision(final Object o, final String rev) {
		this.cast(o).setRevision(rev);
	}
	
	@Override
	public boolean hasIdMutator() {
		return true;
	}
	
	private VersionedSetWrapper cast(final Object o) {
		return (VersionedSetWrapper) o;
	}
}
