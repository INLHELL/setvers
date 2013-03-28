package de.bitub.proitbau.core.couchdb.repository.support;

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

import de.bitub.proitbau.core.model.domain.iDomainModel;

@View(
	name = "all",
	map = "function(doc) { if (doc.T == 'DomainModel' ) emit( null, doc._id )}")
public class DomainModelRepositorySupport extends CouchDbRepositorySupport<iDomainModel> implements
	iDomainModelRepositorySupport {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(DomainModelRepositorySupport.class);
	{
		DomainModelRepositorySupport.logger.setLevel(Level.INFO);
	}
	
	private static final int MAXIMAL_REVISION_LENGTH = 36;
	private static final int MINIMAL_REVISION_LENGTH = 34;
	private static final int UUID_LENGTH = 36;
	private static final int NUMBER_OF_PREV_REV = 0;
	private static final int NUMBER_OF_ACTUAL_REV = 0;
	
	public DomainModelRepositorySupport(final CouchDbConnector couchDbConnector) {
		super(iDomainModel.class, couchDbConnector);
	}
	
	/*
	 * Returns a domain model object which was loaded from the database.
	 * This implementation is faster then <code>read()</code> without parameters,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
	 * @return the domain model loaded from the database
	 */
	@Override
	public iDomainModel read(final String uuid) {
		Preconditions.checkArgument(this.isExists(uuid), "Domain model doesn't exist!");
		final iDomainModel domainModel = super.get(uuid);
		return domainModel;
	}
	
	/*
	 * Returns a domain model object which was loaded from the database.
	 * This implementation is slower then <code>read(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the domain model loaded from the database
	 */
	@Override
	public iDomainModel read() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		final iDomainModel domainModel = listOfDomainModels.get(0);
		return domainModel;
	}
	
	/*
	 * Updates a domain model object state in the database.
	 * @param domainModel the reference to the domain model object which has to be
	 * updated
	 */
	@Override
	public void update(final iDomainModel domainModel) {
		Preconditions.checkNotNull(domainModel, "Given domain model  is null!");
		if (this.isExists(domainModel.getUuid()) || !this.isAnyExists()) {
			super.update(domainModel);
		}
		else {
			throw new IllegalArgumentException("The domain model already exists in the database");
		}
	}
	
	/*
	 * Deletes a domain model object state from the database.
	 * This implementation is faster then <code>delete()</code> and
	 * <code>delete(String uuid)</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param domainModel the reference to the domain model object which has to be
	 * deleted
	 */
	@Override
	public void delete(final iDomainModel domainModel) {
		Preconditions.checkNotNull(domainModel, "Domain model is null!");
		final String uuid = domainModel.getUuid();
		Preconditions.checkArgument(this.isExists(uuid), "Domain model doesn't exist!");
		super.remove(domainModel);
	}
	
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
	@Override
	public void delete(final String uuid) {
		// Existing of the uuid is checking at the read method
		final iDomainModel domainModel = this.read(uuid);
		super.remove(domainModel);
	}
	
	/*
	 * Deletes a domain model object state from the database.
	 * This implementation is slower then <code>delete(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 */
	@Override
	public void delete() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		final iDomainModel domainModel = listOfDomainModels.get(0);
		super.remove(domainModel);
	}
	
	/*
	 * Gets previous state of a domain model object state from the database.
	 * This implementation is faster then <code>getPreviousState()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
	 * @return the previous state of the domain model object
	 */
	@Override
	public iDomainModel getPreviousState(final String uuid) {
		Preconditions.checkArgument(this.isExists(uuid), "Domain model doesn't exist!");
		final List<Revision> revisions = super.db.getRevisions(uuid);
		Preconditions.checkArgument(revisions.size() > 1,
			"Only one state of the domain model is avaliable in the database!");
		// [0] - actual state, [n-1] - eldest state
		final String previousRevision = revisions.get(DomainModelRepositorySupport.NUMBER_OF_PREV_REV).getRev();
		final Options options = new Options().revision(previousRevision);
		final iDomainModel domainModel = super.get(uuid, options);
		// This is absolutely necessary, otherwise if we try to update this domain
		// model, we will have an exception
		domainModel.setRevision(revisions.get(DomainModelRepositorySupport.NUMBER_OF_ACTUAL_REV).getRev());
		return domainModel;
	}
	
	/*
	 * Gets previous state of a domain model object state from the database.
	 * This implementation is slower then <code>getPreviousState(String
	 * uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the previous state of the domain model object
	 */
	@Override
	public iDomainModel getPreviousState() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		iDomainModel domainModel = listOfDomainModels.get(0);
		final List<Revision> revisions = super.db.getRevisions(domainModel.getUuid());
		Preconditions.checkArgument(revisions.size() > 1,
			"Only one state of the domain model is avaliable in the database!");
		// [0] - actual state, [n-1] - eldest state
		final String previousRevision = revisions.get(DomainModelRepositorySupport.NUMBER_OF_PREV_REV).getRev();
		final Options options = new Options().revision(previousRevision);
		domainModel = super.get(domainModel.getUuid(), options);
		// This is absolutely necessary, otherwise if we try to update this domain
		// model, we will have an exception
		domainModel.setRevision(revisions.get(DomainModelRepositorySupport.NUMBER_OF_ACTUAL_REV).getRev());
		return domainModel;
	}
	
	/*
	 * Gets all available revisions of a domain model object from the database.
	 * This implementation is faster then <code>getRevisions()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model object
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
	 * Gets all available revisions of a domain model object from the database.
	 * This implementation is slower then <code>getRevisions(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the list which contains all available revisions of the given domain
	 * model object
	 */
	@Override
	public List<Revision> getRevisions() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		final iDomainModel domainModel = listOfDomainModels.get(0);
		final List<Revision> revisions = super.db.getRevisions(domainModel.getUuid());
		return revisions;
	}
	
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
	@Override
	public iDomainModel getSpecificState(final String uuid, final String actualRevision, final String specificRevision) {
		Preconditions.checkArgument(this.isExists(uuid), "Domain model doesn't exist!");
		this.checkRevisionCorrectness(actualRevision);
		this.checkRevisionCorrectness(specificRevision);
		final List<Revision> revisions = this.getRevisions(uuid);
		final Revision objectRevision = new Revision(specificRevision, "disk");
		Preconditions.checkArgument(revisions.contains(objectRevision),
			"Can not find the domain model object with the given revision number!");
		final Options options = new Options().revision(specificRevision);
		final iDomainModel domainModel = super.get(uuid, options);
		// This is absolutely necessary, otherwise if we try to update this domain
		// model, we will have an exception
		domainModel.setRevision(actualRevision);
		return domainModel;
	}
	
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
	@Override
	public iDomainModel getSpecificState(final String specificRevision) {
		this.checkRevisionCorrectness(specificRevision);
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		iDomainModel domainModel = listOfDomainModels.get(0);
		final String actualRevision = domainModel.getRevision();
		final List<Revision> revisions = this.getRevisions(domainModel.getUuid());
		final Revision objectRevision = new Revision(specificRevision, "disk");
		Preconditions.checkArgument(revisions.contains(objectRevision),
			"Can not find the domain model object with the given revision number!");
		final Options options = new Options().revision(specificRevision);
		domainModel = super.get(domainModel.getUuid(), options);
		// This is absolutely necessary, otherwise if we try to update this domain
		// model, we will have an exception
		domainModel.setRevision(actualRevision);
		return domainModel;
	}
	
	/*
	 * Gets all states of the domain model object from the database.
	 * This implementation is faster then <code>getAllStates()</code>,
	 * because this method doesn't execute <code>getAllDomainModels()</code>
	 * method to find all domain models and their uuids.
	 * @param uuid uuid of the domain model
	 * @return the list with all available states of the domain model
	 */
	@Override
	public List<iDomainModel> getAllStates(final String uuid) {
		Preconditions.checkArgument(this.isExists(uuid), "Domain model doesn't exist!");
		final List<iDomainModel> domainModels = new ArrayList<iDomainModel>(10);
		final List<Revision> revisions = super.db.getRevisions(uuid);
		for (final Revision revision : revisions) {
			final Options options = new Options().revision(revision.getRev());
			domainModels.add(super.get(uuid, options));
		}
		return domainModels;
	}
	
	/*
	 * Gets all states of the domain model object from the database.
	 * This implementation is slower then <code>getAllStates(String uuid)</code>,
	 * because this method executes <code>getAllDomainModels()</code> method to
	 * find all domain models and their uuids.
	 * @return the list with all available states of the domain model
	 */
	@Override
	public List<iDomainModel> getAllStates() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		Preconditions.checkArgument(listOfDomainModels.size() != 0,
			"No domain model objects have been found in the database!");
		Preconditions.checkArgument(listOfDomainModels.size() == 1, "More then one domain model exsits in the database!");
		final List<iDomainModel> domainModels = new ArrayList<iDomainModel>(10);
		final iDomainModel domainModel = listOfDomainModels.get(0);
		final List<Revision> revisions = super.db.getRevisions(domainModel.getUuid());
		for (final Revision revision : revisions) {
			final Options options = new Options().revision(revision.getRev());
			domainModels.add(super.get(domainModel.getUuid(), options));
		}
		return domainModels;
	}
	
	/*
	 * Returns true if any domain model object is available in the database.
	 * @return true - if any domain model object is available in the database,
	 * otherwise returns false
	 */
	@Override
	public boolean isAnyExists() {
		final List<iDomainModel> listOfDomainModels = this.getAllDomainModels();
		if (listOfDomainModels.size() > 0) {
			return true;
		}
		return false;
	}
	
	/*
	 * Returns true if a domain model object with the given uuid is available in
	 * the database.
	 * @param uuid uuid of the domain model
	 * @return true - if the domain model object with given uuid available in the
	 * database, otherwise returns falset
	 */
	@Override
	public boolean isExists(final String uuid) {
		this.checkUuidCorrectness(uuid);
		return super.contains(uuid);
	}
	
	private List<iDomainModel> getAllDomainModels() {
		super.initStandardDesignDocument();
		final List<iDomainModel> listOfDomainModels = super.getAll();
		return listOfDomainModels;
	}
	
	private boolean checkRevisionCorrectness(final String revision) {
		Preconditions.checkNotNull(revision, "Revision is null!");
		Preconditions.checkArgument(revision.length() != 0, "Revision is empty!");
		Preconditions.checkArgument(DomainModelRepositorySupport.MINIMAL_REVISION_LENGTH <= revision.length(),
			"Revision Hash has the wrong length!");
		Preconditions.checkArgument(revision.length() <= DomainModelRepositorySupport.MAXIMAL_REVISION_LENGTH,
			"Revision Hash has the wrong length!");
		return true;
	}
	
	private boolean checkUuidCorrectness(final String uuid) {
		Preconditions.checkNotNull(uuid, "UUID is null!");
		Preconditions.checkArgument(uuid.length() != 0, "UUID is empty!");
		Preconditions
			.checkArgument(uuid.length() == DomainModelRepositorySupport.UUID_LENGTH, "UUID has the wrong length!");
		return true;
	}
	
}
