/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.binding.graph
 * Project: SetVers
 * File: iVersionedSetWrapper.java
 * Date: 29.08.2012
 * Time: 11:38:48
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.binding.graph;

import de.bitub.proitbau.common.versioning.model.VersionedSetType;

public interface iVersionedSetWrapper {
	
	public String getUuid();
	
	public String getVersionedSetUuid();
	
	public void setVersionedSetUuid(final String versionedSetUuid);
	
	public String getVersionedSetName();
	
	public void setVersionedSetName(final String versionedSetName);
	
	public int getVersionedSetSize();
	
	public void setVersionedSetSize(final int versionedSetSize);
	
	public VersionedSetType getVersionedSetType();
	
	public void setVersionedSetType(final VersionedSetType versionedSetClassType);
	
}
