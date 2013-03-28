package de.bitub.proitbau.core.couchdb.manager.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;

import com.google.common.base.Preconditions;

import de.bitub.proitbau.core.couchdb.manager.CouchDBConnectionManager;
import de.bitub.proitbau.core.couchdb.manager.iCouchDBConnectionManager;
import de.bitub.proitbau.core.couchdb.mapper.factory.iDomainModelObjectMapperFactory;

public class CouchDBConnectionManagerFactory implements IAdapterFactory {
	
	private iCouchDBConnectionManager couchDBConnectionManager;
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (iCouchDBConnectionManager.class.equals(adapterType)) {
			if (this.couchDBConnectionManager == null) {
				this.couchDBConnectionManager = new CouchDBConnectionManager();
				final iDomainModelObjectMapperFactory domainModelObjectMapperFactory =
					(iDomainModelObjectMapperFactory) Platform.getAdapterManager().getAdapter(new Object(),
						iDomainModelObjectMapperFactory.class);
				this.couchDBConnectionManager.setObjectMapperFactory(domainModelObjectMapperFactory);
				Preconditions.checkNotNull(couchDBConnectionManager,"CouchDB connection manager is null!");
			}
			return this.couchDBConnectionManager;
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iCouchDBConnectionManager.class
		};
	}
	
}
