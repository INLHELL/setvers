/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.binding.graph
 * Project: SetVers
 * File: iBindingGraph.java
 * Date: 01.08.2012
 * Time: 12:21:45
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
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
