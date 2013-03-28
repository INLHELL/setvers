package de.bitub.proitbau.core.couchdb.repository.support;

import java.util.List;

import de.bitub.proitbau.core.model.project.Project;

public interface iProjectRepositorySupport {
	
	Project read(final String uuid);
	
	List<Project> readAll();
	
	void update(final Project project);
	
	void delete(final Project project);
	
	boolean isAnyExists();
	
	boolean isExists(final String uuid);
	
}
