/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor
 * Project: SetVers
 * File: BindingGraphDocumentAccessor.java
 * Date: 24.07.2012
 * Time: 17:11:11
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor;

import org.ektorp.util.DocumentAccessor;

import de.bitub.proitbau.common.versioning.couchdb.binding.graph.BindingGraph;

public class BindingGraphDocumentAccessor implements DocumentAccessor {
	
	@Override
	public String getId(final Object o) {
		return this.cast(o).getUuid();
	}
	
	@Override
	public String getRevision(final Object o) {
		return this.cast(o).getRevision();
	}
	
	@Override
	public boolean hasIdMutator() {
		return true;
	}
	
	@Override
	public void setId(final Object o, final String id) {
		this.cast(o).setUuid(id);
	}
	
	@Override
	public void setRevision(final Object o, final String rev) {
		this.cast(o).setRevision(rev);
	}
	
	private BindingGraph cast(final Object o) {
		return (BindingGraph) o;
	}
}
