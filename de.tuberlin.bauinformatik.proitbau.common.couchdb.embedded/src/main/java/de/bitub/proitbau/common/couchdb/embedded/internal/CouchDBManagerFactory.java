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
