/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.repository.support
 * Project: SetVers
 * File: iBindingGraphRepositorySupport.java
 * Date: 24.07.2012
 * Time: 17:20:50
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.repository.support;

import java.util.List;

import org.ektorp.Revision;

import de.bitub.proitbau.common.versioning.couchdb.binding.graph.iBindingGraph;

public interface iBindingGraphRepositorySupport {
	
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
	public iBindingGraph read(final String uuid, final boolean readVersionedSetWrapper);
	
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
	public iBindingGraph read(final boolean readVersionedSetWrapper);
	
	/*
	 * Updates a binding graph object state in the database.
	 * @param bindingGraph the reference to the binding graph object which has to
	 * be
	 * updated
	 * @param updateVersionedSetWrapper update or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	public void update(final iBindingGraph bindingGraph, final boolean updateVersionedSetWrapper);
	
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
	 * be
	 * deleted
	 * @param deleteVersionedSetWrapper delete or not the content of binding graph
	 * (VersionedSetWrapper which contains versioned sets)
	 */
	public void delete(final iBindingGraph bindingGraph, final boolean deleteVersionedSetWrapper);
	
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
	public void delete(final String uuid, final boolean deleteVersionedSetWrapper);
	
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
	public void delete(final boolean deleteVersionedSetWrapper);
	
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
	public iBindingGraph getPreviousState(final String uuid, final boolean readVersionedSetWrapper);
	
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
	public iBindingGraph getPreviousState(final boolean readVersionedSetWrapper);
	
	/*
	 * Gets all available revisions of a binding graph object from the database.
	 * This implementation is faster then <code>getRevisions()</code>,
	 * because this method doesn't execute <code>getAllBindingGraphs()</code>
	 * method to find all binding graphs and their uuids.
	 * @param uuid uuid of the binding graph object
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	public List<Revision> getRevisions(final String uuid);
	
	/*
	 * Gets all available revisions of a binding graph object from the database.
	 * This implementation is slower then <code>getRevisions(String uuid)</code>,
	 * because this method executes <code>getAllBindingGraphs()</code> method to
	 * find all binding graphs and their uuids.
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	public List<Revision> getRevisions();
	
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
	public iBindingGraph getSpecificState(final String uuid, final String actualRevision, final String specificRevision,
		final boolean readVersionedSetWrapper);
	
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
	public iBindingGraph getSpecificState(final String specificRevision, final boolean readVersionedSetWrapper);
	
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
	public List<iBindingGraph> getAllStates(final String uuid, final boolean readVersionedSetWrapper);
	
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
	public List<iBindingGraph> getAllStates(final boolean readVersionedSetWrapper);
	
	/*
	 * Returns true if any binding graph object is available in the database.
	 * @return true - if any binding graph object is available in the database,
	 * otherwise returns false
	 */
	public boolean isAnyExists();
	
	/*
	 * Returns true if a binding graph object with the given uuid is available in
	 * the database.
	 * @param uuid uuid of the binding graph
	 * @return true - if the binding graph object with given uuid available in the
	 * database, otherwise returns false
	 */
	public boolean isExists(final String uuid);
	
	void initializeVersionedSetWrapper(iBindingGraph bindingGraph);
	
	void eraseAndCreate(iBindingGraph bindingGraph);
	
}
