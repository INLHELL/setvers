package de.bitub.proitbau.core.couchdb.mixin.document.accessor;

import org.ektorp.util.DocumentAccessor;

import de.bitub.proitbau.core.model.domain.DomainModel;

public class DomainModelDocumentAccessor implements DocumentAccessor {
	
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
	
	private DomainModel cast(final Object o) {
		return (DomainModel) o;
	}
	
}
