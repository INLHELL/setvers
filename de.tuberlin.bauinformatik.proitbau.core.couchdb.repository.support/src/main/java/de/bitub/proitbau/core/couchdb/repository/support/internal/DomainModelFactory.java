package de.bitub.proitbau.core.couchdb.repository.support.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdCouchDbConnector;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import de.bitub.proitbau.core.couchdb.manager.iCouchDBConnectionManager;
import de.bitub.proitbau.core.couchdb.mapper.factory.iDomainModelObjectMapperFactory;
import de.bitub.proitbau.core.couchdb.repository.support.iDomainModelRepositorySupport;
import de.bitub.proitbau.core.model.domain.DomainModel;
import de.bitub.proitbau.core.model.domain.iDomainModel;

public class DomainModelFactory implements IAdapterFactory {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(DomainModelFactory.class);
	{
		DomainModelFactory.logger.setLevel(Level.INFO);
	}
	
	private iDomainModel domainModel = null;
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType.equals(iDomainModel.class)) {
			if (this.domainModel == null) {
				if ((adaptableObject instanceof StdCouchDbConnector)) {
					this.domainModel = this.readOrCreate((StdCouchDbConnector) adaptableObject);
					return this.domainModel;
				}
				else if (adaptableObject instanceof File) {
					this.domainModel = this.importFromFile((File) adaptableObject);
					return this.domainModel;
				}
				else {
					DomainModelFactory.logger.warn("Unsafe domain model creation!");
					
					final iCouchDBConnectionManager couchDBConnectionManager =
						(iCouchDBConnectionManager) Platform.getAdapterManager().getAdapter(new Object(),
							iCouchDBConnectionManager.class);
					final CouchDbConnector currentCouchDbConnector = couchDBConnectionManager.getCurrentCouchDbConnector();
					
					if (currentCouchDbConnector != null) {
						this.domainModel = this.readOrCreate((StdCouchDbConnector) currentCouchDbConnector);
					}
					else {
						DomainModelFactory.logger.info("New domain model was created");
						this.domainModel = new DomainModel();
					}
				}
				
			}
		}
		return this.domainModel;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iDomainModel.class
		};
	}
	
	private iDomainModel importFromFile(final File file) {
		iDomainModel importedDomainModel = null;
		final iDomainModelObjectMapperFactory domainModelObjectMapperFactory =
			(iDomainModelObjectMapperFactory) Platform.getAdapterManager().getAdapter(new Object(),
				iDomainModelObjectMapperFactory.class);
		Preconditions.checkNotNull(domainModelObjectMapperFactory, "Domain model mapper factory is null!");
		final ObjectMapper mapper = domainModelObjectMapperFactory.getObjectMapper();
		try {
			final long startTime = System.currentTimeMillis();
			// importedDomainModel = mapper.readValue(file, iDomainModel.class);
			final JsonNode node = mapper.readTree(file);
			importedDomainModel = mapper.treeToValue(node, iDomainModel.class);
			final long estimatedTime = System.currentTimeMillis() - startTime;
			DomainModelFactory.logger.info("Estimated time of domain model import: " + (estimatedTime / 1000f) + " seconds");
		}
		catch (final JsonParseException e) {
			e.printStackTrace();
		}
		catch (final JsonMappingException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		return importedDomainModel;
	}
	
	private iDomainModel readOrCreate(final StdCouchDbConnector couchDBConnector) {
		iDomainModel temporaryDomainModel = null;
		final iDomainModelRepositorySupport domainModelRepositorySupport =
			(iDomainModelRepositorySupport) Platform.getAdapterManager().getAdapter(couchDBConnector,
				iDomainModelRepositorySupport.class);
		
		if (domainModelRepositorySupport.isAnyExists()) {
			DomainModelFactory.logger.info("Get domain model from the " + couchDBConnector.getDatabaseName() + " database");
			temporaryDomainModel = domainModelRepositorySupport.read();
		}
		else {
			DomainModelFactory.logger.info("New domain model was created");
			temporaryDomainModel = new DomainModel();
			domainModelRepositorySupport.update(temporaryDomainModel);
		}
		return temporaryDomainModel;
	}
	
}
