/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.repository.support
 * Project: SetVers
 * File: BindingGraphRepositorySupport.java
 * Date: 24.07.2012
 * Time: 17:19:06
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.repository.support;

import java.util.ArrayList;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.Options;
import org.ektorp.Revision;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetWrapper;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.iBindingGraph;

@View(
	name = "all",
	map = "function(doc) { if (doc.T == 'BindingGraph' ) emit( null, doc._id )}")
public class BindingGraphRepositorySupport extends CouchDbRepositorySupport<iBindingGraph> implements
	iBindingGraphRepositorySupport {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(BindingGraphRepositorySupport.class);
	{
		BindingGraphRepositorySupport.logger.setLevel(Level.DEBUG);
	}
	
	private static final int MAXIMAL_REVISION_LENGTH = 36;
	private static final int MINIMAL_REVISION_LENGTH = 34;
	private static final int UUID_LENGTH = 36;
	private static final int NUMBER_OF_PREV_REV = 0;
	private static final int NUMBER_OF_ACTUAL_REV = 0;
	
	public BindingGraphRepositorySupport(final CouchDbConnector couchDbConnector) {
		super(iBindingGraph.class, couchDbConnector);
	}
	
	/*
	 * Returns a binding graph object which was loaded from the database.
	 * This implementation is faster then <code>read(boolean
	 * readVersionedSetWrapper)</code>, because this method doesn't execute
	 * <code>getAllBindingGraphs()</code> method to find
	 * all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph object
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the binding graph loaded from the database
	 */
	@Override
	public iBindingGraph read(final String uuid, final boolean readVersionedSetWrapper) {
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		final iBindingGraph bindingGraph = super.get(uuid);
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		// // To keep uuid consistency we assign the same uuid for this temporary
		// // VersionedSetWrapper
		// else {
		// bindingGraph.getVersionedSetWrapper().setUuid(bindingGraph.getVersionedSetWrapperUuid());
		// }
		return bindingGraph;
	}
	
	/*
	 * Returns a binding graph object which was loaded from the database.
	 * This implementation is slower then <code>read(String uuid, boolean
	 * readVersionedSetWrapper)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code> method to
	 * find all binding graphs and their uuids.
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the binding graph loaded from the database
	 */
	@Override
	public iBindingGraph read(final boolean readVersionedSetWrapper) {
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		final iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		// // To keep uuid consistency we assign the same uuid for this temporary
		// // VersionedSetWrapper
		// else {
		// bindingGraph.getVersionedSetWrapper().setUuid(bindingGraph.getVersionedSetWrapperUuid());
		// }
		return bindingGraph;
	}
	
	/*
	 * Updates a binding graph object state in the database.
	 * @param bindingGraph the reference to the binding graph object which has to
	 * be
	 * updated
	 * @param updateVersionedSetWrapper update or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	@Override
	public void update(final iBindingGraph bindingGraph, final boolean updateVersionedSetWrapper) {
		Preconditions.checkNotNull(bindingGraph, "Given binding graph  is null!");
		if (!this.isAnyExists() || this.isExists(bindingGraph.getUuid())) {
			try {
				super.update(bindingGraph);
			}
			catch (final org.ektorp.UpdateConflictException e) {
				BindingGraphRepositorySupport.logger
					.error(
						"Some problems during the update process, the revision number of the binding graph object might be in inconsistent state",
						e);
			}
			if (updateVersionedSetWrapper) {
				Preconditions.checkNotNull(bindingGraph.getVersionedSetWrapper(), "VersionedSetWrapper is null!");
				this.db.update(bindingGraph.getVersionedSetWrapper());
			}
		}
		else {
			throw new IllegalArgumentException("The binding graph already exists in the database");
		}
	}
	
	/*
	 * Deletes a binding graph object state from the database.
	 * This implementation is faster then
	 * <code>delete(String uuid, boolean deleteVersionedSetWrapper)</code>
	 * and <code>delete(boolean deleteVersionedSetWrapper)</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids and doesn't load binding
	 * graph object by its uuid
	 * <code>read(String uuid, boolean readVersionedSetWrapper)</code>.
	 * @param bindingGraph the reference to the binding graph object which has to
	 * be deleted
	 * @param deleteVersionedSetWrapper delete or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	@Override
	public void delete(final iBindingGraph bindingGraph, final boolean deleteVersionedSetWrapper) {
		Preconditions.checkNotNull(bindingGraph, "Binding graph is null!");
		final String uuid = bindingGraph.getUuid();
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		if (deleteVersionedSetWrapper) {
			this.deleteVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid());
		}
		super.remove(bindingGraph);
	}
	
	/*
	 * Deletes a binding graph object from the database.
	 * This implementation is faster then <code>delete(boolean
	 * deleteVersionedSetWrapper)</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids, but this method is a
	 * little bit slower then <code>delete(iBindingGraph bindingGraph, boolean
	 * deleteVersionedSetWrapper)</code>,
	 * cause this method loads a binding graph by uuid first, it calls
	 * <code>read(String uuid, boolean readVersionedSetWrapper)</code> internaly .
	 * @param uuid uuid of the binding graph object
	 * @param deleteVersionedSetWrapper delete or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	@Override
	public void delete(final String uuid, final boolean deleteVersionedSetWrapper) {
		final iBindingGraph bindingGraph = this.read(uuid, deleteVersionedSetWrapper);
		if (deleteVersionedSetWrapper) {
			this.deleteVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid());
		}
		super.remove(bindingGraph);
	}
	
	/*
	 * Deletes a binding graph object state from the database.
	 * This implementation is slower then
	 * <code>delete(iBindingGraph bindingGraph, boolean
	 * deleteVersionedSetWrapper)</code>
	 * and <code>delete(String uuid, boolean deleteVersionedSetWrapper)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code> method to
	 * find all binding graphs and their uuids.
	 * @param deleteVersionedSetWrapper delete or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	@Override
	public void delete(final boolean deleteVersionedSetWrapper) {
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		final iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		super.remove(bindingGraph);
		if (deleteVersionedSetWrapper) {
			this.deleteVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid());
		}
	}
	
	/*
	 * Gets previous state of a binding graph object state from the database.
	 * This implementation is faster then <code>getPreviousState(boolean
	 * readVersionedSetWrapper)</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph object
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the previous state of the binding graph object
	 */
	@Override
	public iBindingGraph getPreviousState(final String uuid, final boolean readVersionedSetWrapper) {
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		final List<Revision> revisions = super.db.getRevisions(uuid);
		Preconditions.checkArgument(revisions.size() > 1,
			"Only one state of the binding graph is avaliable in the database!");
		// [0] - actual state, [n-1] - eldest state
		final String previousRevision = revisions.get(BindingGraphRepositorySupport.NUMBER_OF_PREV_REV).getRev();
		final Options options = new Options().revision(previousRevision);
		final iBindingGraph bindingGraph = super.get(uuid, options);
		// This is absolutely necessary, otherwise if we try to update this binding
		// graph, we will have an exception
		bindingGraph.setRevision(revisions.get(BindingGraphRepositorySupport.NUMBER_OF_ACTUAL_REV).getRev());
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		return bindingGraph;
	}
	
	/*
	 * Gets previous state of a binding graph object state from the database.
	 * This implementation is slower then <code>getPreviousState(String uuid,
	 * boolean readVersionedSetWrapper)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code> method to
	 * find all binding graphs and their uuids.
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the previous state of the binding graph object
	 */
	@Override
	public iBindingGraph getPreviousState(final boolean readVersionedSetWrapper) {
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		final List<Revision> revisions = super.db.getRevisions(bindingGraph.getUuid());
		Preconditions.checkArgument(revisions.size() > 1,
			"Only one state of the binding graph is avaliable in the database!");
		// [0] - actual state, [n-1] - eldest state
		final String previousRevision = revisions.get(BindingGraphRepositorySupport.NUMBER_OF_PREV_REV).getRev();
		final Options options = new Options().revision(previousRevision);
		bindingGraph = super.get(bindingGraph.getUuid(), options);
		// This is absolutely necessary, otherwise if we try to update this binding
		// graph, we will have an exception
		bindingGraph.setRevision(revisions.get(BindingGraphRepositorySupport.NUMBER_OF_ACTUAL_REV).getRev());
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		return bindingGraph;
	}
	
	/*
	 * Gets all available revisions of a binding graph object from the database.
	 * This implementation is faster then <code>getRevisions()</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph object
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	@Override
	public List<Revision> getRevisions(final String uuid) {
		this.isExists(uuid);
		Preconditions.checkArgument(super.contains(uuid), "Database doesn't contain this object!");
		final List<Revision> revisions = super.db.getRevisions(uuid);
		return revisions;
	}
	
	/*
	 * Gets all available revisions of a binding graph object from the database.
	 * This implementation is slower then <code>getRevisions(String uuid)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code> method to
	 * find all binding graphs and their uuids.
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	@Override
	public List<Revision> getRevisions() {
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		final iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		final List<Revision> revisions = super.db.getRevisions(bindingGraph.getUuid());
		return revisions;
	}
	
	/*
	 * Gets a specific state of a binding graph object from the database.
	 * This implementation is faster then
	 * <code>getSpecificState(String specificRevision, boolean
	 * getVersionedSetWrapper)</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph
	 * @param actualRevision an actual revision of the binding graph
	 * @param specificRevision which revision of the binding graph should be
	 * loaded
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the binding graph in a specific state
	 */
	@Override
	public iBindingGraph getSpecificState(final String uuid, final String actualRevision, final String specificRevision,
		final boolean readVersionedSetWrapper) {
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		this.checkRevisionCorrectness(actualRevision);
		this.checkRevisionCorrectness(specificRevision);
		final List<Revision> revisions = this.getRevisions(uuid);
		final Revision objectRevision = new Revision(specificRevision, "disk");
		Preconditions.checkArgument(revisions.contains(objectRevision),
			"Can not find the binding graph object with the given revision number!");
		final Options options = new Options().revision(specificRevision);
		final iBindingGraph bindingGraph = super.get(uuid, options);
		// This is absolutely necessary, otherwise if we try to update this binding
		// graph, we will have an exception
		bindingGraph.setRevision(actualRevision);
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		return bindingGraph;
	}
	
	/*
	 * Gets a specific state of a binding graph object from the database.
	 * This implementation is slower then
	 * <code>getSpecificState(String uuid, String actualRevision, String
	 * specificRevision, final boolean readVersionedSetWrapper)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param actualRevision an actual revision of the binding graph
	 * @param specificRevision which revision of the binding graph should be
	 * loaded
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the binding graph in a specific state
	 */
	@Override
	public iBindingGraph getSpecificState(final String specificRevision, final boolean readVersionedSetWrapper) {
		this.checkRevisionCorrectness(specificRevision);
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		final String actualRevision = bindingGraph.getRevision();
		final List<Revision> revisions = this.getRevisions(bindingGraph.getUuid());
		final Revision objectRevision = new Revision(specificRevision, "disk");
		Preconditions.checkArgument(revisions.contains(objectRevision),
			"Can not find the binding graph object with the given revision number!");
		final Options options = new Options().revision(specificRevision);
		bindingGraph = super.get(bindingGraph.getUuid(), options);
		// This is absolutely necessary, otherwise if we try to update this binding
		// graph, we will have an exception
		bindingGraph.setRevision(actualRevision);
		if (readVersionedSetWrapper) {
			bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
		}
		return bindingGraph;
	}
	
	/*
	 * Gets all states of the binding graph object from the database.
	 * This implementation is faster then <code>getAllStates(boolean
	 * getVersionedSetWrapper)</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the list with all available states of the binding graph
	 */
	@Override
	public List<iBindingGraph> getAllStates(final String uuid, final boolean readVersionedSetWrapper) {
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		final List<iBindingGraph> bindingGraphs = Lists.newArrayList();
		final List<Revision> revisions = super.db.getRevisions(uuid);
		for (final Revision revision : revisions) {
			final Options options = new Options().revision(revision.getRev());
			final iBindingGraph bindingGraph = super.get(uuid, options);
			if (readVersionedSetWrapper) {
				bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
			}
			bindingGraphs.add(bindingGraph);
		}
		return bindingGraphs;
	}
	
	/*
	 * Gets all states of the binding graph object from the database.
	 * This implementation is slower then <code>getAllStates(String uuid, boolean
	 * getVersionedSetWrapper)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param readVersionedSetWrapper load or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 * @return the list with all available states of the binding graph
	 */
	@Override
	public List<iBindingGraph> getAllStates(final boolean readVersionedSetWrapper) {
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
			"No binding graph objects have been found in the database!");
		Preconditions.checkArgument(listOfBindingGraphs.size() == 1, "More then one binding graph exsits in the database!");
		final List<iBindingGraph> bindingGraphs = new ArrayList<iBindingGraph>(10);
		final iBindingGraph bindingGraph = listOfBindingGraphs.get(0);
		final List<Revision> revisions = super.db.getRevisions(bindingGraph.getUuid());
		for (final Revision revision : revisions) {
			final Options options = new Options().revision(revision.getRev());
			final iBindingGraph bindingGraphInSpecificState = super.get(bindingGraph.getUuid(), options);
			if (readVersionedSetWrapper) {
				bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
			}
			bindingGraphs.add(bindingGraphInSpecificState);
		}
		return bindingGraphs;
	}
	
	/*
	 * Returns true if any binding graph object is available in the database.
	 * @return true - if any binding graph object is available in the database,
	 * otherwise returns false
	 */
	@Override
	public boolean isAnyExists() {
		BindingGraphRepositorySupport.logger.info("Load all binding graph objects from the database");
		final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
		if (listOfBindingGraphs.size() > 0) {
			return true;
		}
		return false;
	}
	
	/*
	 * Returns true if a binding graph object with the given uuid is available in
	 * the database.
	 * @param uuid uuid of the binding graph
	 * @return true - if the binding graph object with given uuid available in the
	 * database, otherwise returns false
	 */
	@Override
	public boolean isExists(final String uuid) {
		this.checkUuidCorrectness(uuid);
		return super.contains(uuid);
	}
	
	private List<iBindingGraph> getAllBindingGraphs() {
		super.initStandardDesignDocument();
		final List<iBindingGraph> listOfBindingGraphs = super.getAll();
		return listOfBindingGraphs;
	}
	
	private boolean checkRevisionCorrectness(final String revision) {
		Preconditions.checkNotNull(revision, "Revision is null!");
		Preconditions.checkArgument(revision.length() != 0, "Revision is empty!");
		Preconditions.checkArgument(BindingGraphRepositorySupport.MINIMAL_REVISION_LENGTH <= revision.length(),
			"Revision Hash has the wrong length!");
		Preconditions.checkArgument(revision.length() <= BindingGraphRepositorySupport.MAXIMAL_REVISION_LENGTH,
			"Revision Hash has the wrong length!");
		return true;
	}
	
	private boolean checkUuidCorrectness(final String uuid) {
		Preconditions.checkNotNull(uuid, "UUID is null!");
		Preconditions.checkArgument(uuid.length() != 0, "UUID is empty!");
		Preconditions.checkArgument(uuid.length() == BindingGraphRepositorySupport.UUID_LENGTH,
			"UUID has the wrong length!");
		return true;
	}
	
	@Override
	public void initializeVersionedSetWrapper(final iBindingGraph bindingGraph) {
		Preconditions.checkNotNull(bindingGraph, "Binding graph is null!");
		final String uuid = bindingGraph.getUuid();
		Preconditions.checkArgument(this.isExists(uuid), "Binding graph doesn't exist!");
		bindingGraph.setVersionedSetWrapper(this.readVersionedSetWrapper(bindingGraph.getVersionedSetWrapperUuid()));
	}
	
	@Override
	public void eraseAndCreate(final iBindingGraph bindingGraph) {
		Preconditions.checkNotNull(bindingGraph, "Binding graph is null!");
		if (this.isAnyExists()) {
			final List<iBindingGraph> listOfBindingGraphs = this.getAllBindingGraphs();
			Preconditions.checkArgument(listOfBindingGraphs.size() != 0,
				"No binding graph objects have been found in the database!");
			Preconditions.checkArgument(listOfBindingGraphs.size() == 1,
				"More then one binding graph exsits in the database!");
			final iBindingGraph oldBindingGraph = listOfBindingGraphs.get(0);
			this.deleteVersionedSetWrapper(oldBindingGraph.getVersionedSetWrapperUuid());
			super.remove(oldBindingGraph);
		}
		// This is necessary, because if for some reasons the revision number won't
		// be equal null when we try to store the binding graph object first time we
		// will have an exception
		bindingGraph.setRevision(null);
		super.update(bindingGraph);
		Preconditions.checkNotNull(bindingGraph.getVersionedSetWrapper(), "VersionedSetWrapper  is null!");
		this.db.update(bindingGraph.getVersionedSetWrapper());
	}
	
	private VersionedSetWrapper readVersionedSetWrapper(final String versionedSetWrapperUuid) {
		this.checkUuidCorrectness(versionedSetWrapperUuid);
		BindingGraphRepositorySupport.logger.info("Trying to get VersionedSetWrapper object from the: "
																							+ this.db.getDatabaseName());
		BindingGraphRepositorySupport.logger.info("VersionedSetWrapper's UUID: " + versionedSetWrapperUuid);
		Preconditions.checkArgument(this.db.contains(versionedSetWrapperUuid),
			"The VersionedSetWrapper with the given identifier doesn't exist!");
		final VersionedSetWrapper versionedSetWrapper = this.db.get(VersionedSetWrapper.class, versionedSetWrapperUuid);
		return versionedSetWrapper;
	}
	
	private void deleteVersionedSetWrapper(final String versionedSetWrapperUuid) {
		this.checkUuidCorrectness(versionedSetWrapperUuid);
		Preconditions.checkArgument(this.db.contains(versionedSetWrapperUuid),
			"The VersionedSetWrapper with the given identifier doesn't exist!");
		final VersionedSetWrapper versionedSetWrapper = this.db.get(VersionedSetWrapper.class, versionedSetWrapperUuid);
		this.db.delete(versionedSetWrapper);
	}
	
}
