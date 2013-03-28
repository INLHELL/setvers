/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor
 * Project: SetVers
 * File: VersionedSetWrapperDocumentAccessor.java
 * Date: 03.09.2012
 * Time: 14:31:12
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
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
