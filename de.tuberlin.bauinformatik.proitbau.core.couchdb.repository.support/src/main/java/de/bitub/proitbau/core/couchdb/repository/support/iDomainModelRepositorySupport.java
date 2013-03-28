package de.bitub.proitbau.core.couchdb.repository.support;

import java.util.List;

import org.ektorp.Revision;

import de.bitub.proitbau.core.model.domain.iDomainModel;

public interface iDomainModelRepositorySupport {
	
	/*
	 * Returns a domain model object which was loaded from the database.
	 * This implementation is faster then <code>read()</code> without parameters,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
	 * @return the domain model loaded from the database
	 */
	public iDomainModel read(final String uuid);
	
	/*
	 * Returns a domain model object which was loaded from the database.
	 * This implementation is slower then <code>read(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the domain model loaded from the database
	 */
	public iDomainModel read();
	
	/*
	 * Updates a domain model object state in the database.
	 * @param domainModel the reference to the domain model object which has to be
	 * updated
	 */
	public void update(final iDomainModel domainModel);
	
	/*
	 * Deletes a domain model object state from the database.
	 * This implementation is faster then <code>delete()</code> and
	 * <code>delete(String uuid)</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param domainModel the reference to the domain model object which has to be
	 * deleted
	 */
	public void delete(final iDomainModel domainModel);
	
	/*
	 * Deletes a domain model object from the database.
	 * This implementation is faster then <code>delete()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids, but this method is a
	 * little bit slower then
	 * <code>delete(iDomainModel domainModel)</code>,
	 * cause this method loads a domain model by uuid first.
	 * @param uuid uuid of the domain model object
	 */
	public void delete(final String uuid);
	
	/*
	 * Deletes a domain model object state from the database.
	 * This implementation is slower then <code>delete(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 */
	public void delete();
	
	/*
	 * Gets previous state of a domain model object state from the database.
	 * This implementation is faster then <code>getPreviousState()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
	 * @return the previous state of the domain model object
	 */
	public iDomainModel getPreviousState(final String uuid);
	
	/*
	 * Gets previous state of a domain model object state from the database.
	 * This implementation is slower then <code>getPreviousState(String
	 * uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the previous state of the domain model object
	 */
	public iDomainModel getPreviousState();
	
	/*
	 * Gets all available revisions of a domain model object from the database.
	 * This implementation is faster then <code>getRevisions()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	public List<Revision> getRevisions(final String uuid);
	
	/*
	 * Gets all available revisions of a domain model object from the database.
	 * This implementation is slower then <code>getRevisions(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	public List<Revision> getRevisions();
	
	/*
	 * Gets a specific state of a domain model object from the database.
	 * This implementation is faster then
	 * <code>getSpecificState(String specificRevision)</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model
	 * @param actualRevision an actual revision of the domain model
	 * @param specificRevision which revision of the domain model should be loaded
	 * @return the domain model in a specific state
	 */
	public iDomainModel getSpecificState(final String uuid, final String actualRevision, final String specificRevision);
	
	/*
	 * Gets a specific state of a domain model object from the database.
	 * This implementation is slower then
	 * <code>getSpecificState(String uuid, String actualRevision, String
	 * specificRevision)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @param uuid uuid of the domain model
	 * @param actualRevision an actual revision of the domain model
	 * @param specificRevision which revision of the domain model should be loaded
	 * @return the domain model in a specific state
	 */
	public iDomainModel getSpecificState(final String specificRevision);
	
	/*
	 * Gets all states of the domain model object from the database.
	 * This implementation is faster then <code>getAllStates()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model
	 * @return the list with all available states of the domain model
	 */
	public List<iDomainModel> getAllStates(final String uuid);
	
	/*
	 * Gets all states of the domain model object from the database.
	 * This implementation is slower then <code>getAllStates(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the list with all available states of the domain model
	 */
	public List<iDomainModel> getAllStates();
	
	/*
	 * Returns true if any domain model object is available in the database.
	 * @return true - if any domain model object is available in the database,
	 * otherwise returns false
	 */
	public boolean isAnyExists();
	
	/*
	 * Returns true if a domain model object with the given uuid is available in
	 * the database.
	 * @param uuid uuid of the domain model
	 * @return true - if the domain model object with given uuid available in the
	 * database, otherwise returns false
	 */
	public boolean isExists(final String uuid);
	
}
