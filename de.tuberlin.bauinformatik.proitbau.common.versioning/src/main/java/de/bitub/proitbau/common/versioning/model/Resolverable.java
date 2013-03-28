package de.bitub.proitbau.common.versioning.model;

public interface Resolverable {
	
	public boolean check(Object mergedObject);
	
	public void resolve(Object leadingObject, Object mergedObject, Object nonleadingObject);
}
