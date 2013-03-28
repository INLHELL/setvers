package de.bitub.proitbau.core.couchdb.repository.support;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;

import de.bitub.proitbau.core.model.project.Project;

public class ProjectRepositorySupport extends CouchDbRepositorySupport<Project> implements iProjectRepositorySupport {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(ProjectRepositorySupport.class);
	{
		ProjectRepositorySupport.logger.setLevel(Level.INFO);
	}
	
	private static final int UUID_LENGTH = 36;
	
	public ProjectRepositorySupport(final CouchDbConnector couchDbConnector) {
		super(Project.class, couchDbConnector);
	}
	
	@Override
	public void delete(final Project project) {
		Preconditions.checkNotNull(project, "Given project is null!");
		final String uuid = project.getUuid();
		Preconditions.checkArgument(this.isExists(uuid), "Project with the given uuid doesn't exist!");
		super.remove(project);
	}
	
	@Override
	public boolean isAnyExists() {
		final List<Project> listOfProjects = super.getAll();
		if (listOfProjects.size() > 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isExists(final String uuid) {
		this.checkUuidCorrectness(uuid);
		return super.contains(uuid);
	}
	
	@Override
	public Project read(final String uuid) {
		Preconditions.checkArgument(this.isExists(uuid), "Project with the given uuid doesn't exist!");
		final Project project = super.get(uuid);
		return project;
	}
	
	@Override
	public List<Project> readAll() {
		final List<Project> projects = super.getAll();
		ProjectRepositorySupport.logger.info(projects.size() + " projects were selected");
		return projects;
	}
	
	@Override
	public void update(final Project project) {
		Preconditions.checkNotNull(project, "Given project is null!");
		super.update(project);
		
	}
	
	private boolean checkUuidCorrectness(final String uuid) {
		Preconditions.checkNotNull(uuid, "UUID is null!");
		Preconditions.checkArgument(uuid.length() != 0, "UUID is empty!");
		Preconditions.checkArgument(uuid.length() == ProjectRepositorySupport.UUID_LENGTH, "UUID has the wrong length!");
		return true;
	}
	
}
