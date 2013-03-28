package de.bitub.proitbau.core.couchdb.repository.support.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.ektorp.impl.StdCouchDbConnector;

import de.bitub.proitbau.core.couchdb.repository.support.DomainModelRepositorySupport;
import de.bitub.proitbau.core.couchdb.repository.support.ProjectRepositorySupport;
import de.bitub.proitbau.core.couchdb.repository.support.iDomainModelRepositorySupport;
import de.bitub.proitbau.core.couchdb.repository.support.iProjectRepositorySupport;

public class RepositorySupportFactory implements IAdapterFactory {
	
	private iDomainModelRepositorySupport domainModelRepositorySupport;
	private iProjectRepositorySupport projectRepositorySupport;
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType.equals(iDomainModelRepositorySupport.class) && (adaptableObject instanceof StdCouchDbConnector)) {
			if ((this.domainModelRepositorySupport == null)) {
				this.domainModelRepositorySupport = new DomainModelRepositorySupport((StdCouchDbConnector) adaptableObject);
			}
			return this.domainModelRepositorySupport;
		}
		if (adapterType.equals(iProjectRepositorySupport.class) && (adaptableObject instanceof StdCouchDbConnector)) {
			if ((this.projectRepositorySupport == null)) {
				this.projectRepositorySupport = new ProjectRepositorySupport((StdCouchDbConnector) adaptableObject);
			}
			return this.projectRepositorySupport;
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iDomainModelRepositorySupport.class,
			iProjectRepositorySupport.class
		};
	}
	
}
