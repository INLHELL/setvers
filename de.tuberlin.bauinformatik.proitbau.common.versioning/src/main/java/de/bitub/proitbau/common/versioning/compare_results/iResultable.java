package de.bitub.proitbau.common.versioning.compare_results;

public interface iResultable<T extends Object> {
	
	T getFirst();
	
	void setFirst(T first);
	
	T getSecond();
	
	void setSecond(T second);
	
	String getName();
	
	void setName(final String name);
	
	boolean isEqual();
	
	void setEqual(final boolean equal);
	
}
