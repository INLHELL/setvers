/*
 * Package:
 * de.bitub.proitbau.common.versioning.couchdb.repository.support.internal
 * Project: SetVers
 * File: BindingGraphFactory.java
 * Date: 01.08.2012
 * Time: 12:13:12
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.repository.support.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.ektorp.CouchDbConnector;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;

import de.bitub.proitbau.common.versioning.couchdb.binding.graph.BindingGraph;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.iBindingGraph;
import de.bitub.proitbau.common.versioning.couchdb.repository.support.iBindingGraphRepositorySupport;
import de.bitub.proitbau.core.couchdb.manager.iCouchDBConnectionManager;

public class BindingGraphFactory implements IAdapterFactory {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(BindingGraphFactory.class);
	{
		BindingGraphFactory.logger.setLevel(Level.INFO);
	}
	
	private iBindingGraph bindingGraph = null;
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType.equals(iBindingGraph.class)) {
			// If BindingGraphRepositorySupport object was specified, we can try to
			// use it
			if ((adaptableObject instanceof iBindingGraphRepositorySupport)) {
				this.bindingGraph = this.readOrCreate((iBindingGraphRepositorySupport) adaptableObject);
			}
			// If BindingGraphRepositorySupport object wasn't specified, we will try
			// to get it
			else {
				BindingGraphFactory.logger.warn("Unsafe binding graph creation!");
				
				// Lets try to create our own iBindingGraphRepositorySupport
				final iCouchDBConnectionManager couchDBConnectionManager =
					(iCouchDBConnectionManager) Platform.getAdapterManager().getAdapter(new Object(),
						iCouchDBConnectionManager.class);
				// To obtain BindingGraphRepositorySupport object we have to specify
				// CouchDbConnector, we are going to try the current connector, which
				// was used last time somewhere at the application
				final CouchDbConnector currentCouchDbConnector = couchDBConnectionManager.getCurrentCouchDbConnector();
				
				// If we succeed with getting CouchDbConnector object and we have it, we
				// can try to use it
				if (currentCouchDbConnector != null) {
					final iBindingGraphRepositorySupport bindingGraphRepositorySupport =
						(iBindingGraphRepositorySupport) Platform.getAdapterManager().getAdapter(couchDBConnectionManager,
							iBindingGraphRepositorySupport.class);
					this.bindingGraph = this.readOrCreate(bindingGraphRepositorySupport);
					Preconditions.checkNotNull(this.bindingGraph, "Binding graph is null!");
				}
				else {
					BindingGraphFactory.logger.info("New binding graph was created");
					this.bindingGraph = new BindingGraph();
				}
			}
			return this.bindingGraph;
		}
		BindingGraphFactory.logger.warn("Binding graph was not created");
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iBindingGraph.class
		};
	}
	
	private iBindingGraph readOrCreate(final iBindingGraphRepositorySupport bindingGraphRepositorySupport) {
		iBindingGraph temporaryBindingGraph = null;
		// If object exists in the database we load it
		if (bindingGraphRepositorySupport.isAnyExists()) {
			BindingGraphFactory.logger.info("Get binding graph from the database");
			temporaryBindingGraph = bindingGraphRepositorySupport.read(false);
		}
		// If object doesn't exist we create a new one and store it
		else {
			BindingGraphFactory.logger.info("New binding graph was created");
			temporaryBindingGraph = new BindingGraph();
			// bindingGraphRepositorySupport.update(temporaryBindingGraph, false);
		}
		return temporaryBindingGraph;
	}
}
