/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 01.08.2012
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
package de.bitub.proitbau.common.versioning.couchdb.binding.graph;

import java.util.Set;

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public interface iBindingGraph {
	
	// void addVersionedSet(final VersionedSet versionedSet);
	//
	// void addVersionedSets(final Set<VersionedSet> versionedSets);
	
	boolean isInitial();
	
	// void clear();
	
	String getAuthor();
	
	long getDate();
	
	String getRevision();
	
	String getUuid();
	
	// Set<VersionedSet> getVersionedSets();
	
	Set<VersionedSetRepresentation> getVersionedSetRepresentations();
	
	VersionedSetWrapper getVersionedSetWrapper();
	
	Set<VersionedSet> obtainVersionedSets();
	
	void setAuthor(String string);
	
	// void setChanges(int changes);
	
	void setComment(String string);
	
	void setDate(long currentTimeMillis);
	
	void setRevision(final String revision);
	
	void specifyNewState(Set<VersionedSet> versionedSets);
	
	void setVersionedSetWrapper(VersionedSetWrapper versionedSetWrapper);
	
	String getVersionedSetWrapperUuid();
	
}
