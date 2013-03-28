package de.bitub.proitbau.common.versioning.model.logic;

import java.util.Map.Entry;
import java.util.Set;

import org.azeckoski.reflectutils.DeepUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.annotations.Constraint;
import de.bitub.proitbau.common.versioning.compare_results.ObjectResult;
import de.bitub.proitbau.common.versioning.compare_results.StateResult;
import de.bitub.proitbau.common.versioning.compare_results.VersionedSetResult;
import de.bitub.proitbau.common.versioning.model.ModelCache;
import de.bitub.proitbau.common.versioning.model.Resolverable;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.util.ReflectionUtil;
import de.bitub.proitbau.common.versioning.util.StringUtil;

public class Merger {
	
	private Merger() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static Merger instance = new Merger();
	}
	
	@SuppressWarnings("synthetic-access")
	public static Merger getInstance() {
		return Handler.instance;
	}
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(Merger.class);
	{
		Merger.logger.setLevel(Level.INFO);
	}
	
	// Lets consider such an example:
	// Set1 -> {1->'a', 2->'b', 3->'c'}
	// Set2 -> {2->'d', 3->'c', 4->'e'}
	// We will have four sets:
	// objectsAvailableOnlyInFirstVersionedSet -> {1->'a'}
	// objectsAvailableOnlyInSecondVersionedSet -> {4->'e'}
	// overwrittenObjectsFromFirstVersinedSet -> {2->'b'}
	// overwrittenObjectsFromSecondVersinedSet -> {2->'d'}
	// Methods like: 'difference', 'intersection', 'union' look only to the
	// identifier of the object in other words if the objects have different
	// content but the same id, they will be considered as the same object
	// and content of one of them will be present in the result set
	
	private Set<Object> objectsAvailableOnlyInFirstVersionedSet = Sets.newHashSet();
	private Set<Object> objectsAvailableOnlyInSecondVersionedSet = Sets.newHashSet();
	private Set<Object> overwrittenObjectsFromFirstVersinedSet = Sets.newHashSet();
	private Set<Object> overwrittenObjectsFromSecondVersinedSet = Sets.newHashSet();
	
	public Set<Object> getObjectsAvailableOnlyInFirstVersionedSet() {
		return this.objectsAvailableOnlyInFirstVersionedSet;
	}
	
	public Set<Object> getObjectsAvailableOnlyInSecondVersionedSet() {
		return this.objectsAvailableOnlyInSecondVersionedSet;
	}
	
	public Object getOverwrittenObjectsFromFirstVersinedSet() {
		return this.overwrittenObjectsFromFirstVersinedSet;
	}
	
	public Object getOverwrittenObjectsFromSecondVersinedSett() {
		return this.overwrittenObjectsFromSecondVersinedSet;
	}
	
	public Set<VersionedSet> merge(final StateResult stateResult, final boolean oldIsLeading) {
		Preconditions.checkNotNull(stateResult, "Given statetResult is null!");
		
		// Clear previous results
		this.objectsAvailableOnlyInFirstVersionedSet.clear();
		this.objectsAvailableOnlyInSecondVersionedSet.clear();
		this.overwrittenObjectsFromFirstVersinedSet.clear();
		this.overwrittenObjectsFromSecondVersinedSet.clear();
		
		// Invalidate cache
		ModelCache.getInstance().invalidateObjectValuesCache();
		
		final Set<VersionedSet> mergedVersionedSets = Sets.newHashSetWithExpectedSize(30);
		
		for (final VersionedSetResult versionedSetResult : stateResult.getResults()) {
			final VersionedSet firstVersionedSet = versionedSetResult.getFirst();
			final VersionedSet secondVersionedSet = versionedSetResult.getSecond();
			VersionedSet mergedVersionedSet = null;
			// 1) old versioned set is null, new versioned set is available
			// If only new versioned set is presented then we add it as merged
			// versioned set, because we don't have a pair for comparison
			Preconditions.checkNotNull(secondVersionedSet, "New state of versioned set can't be null!");
			if (firstVersionedSet == null) {
				mergedVersionedSet = secondVersionedSet;
			}
			// 2) Both versioned sets are presented and we can merge them
			// There are two cases at this step: the same versioned set can available
			// at the both states, cause it was added as predecessor binding to the
			// both states or these versioned sets can be absolutely different and we
			// have to merge them
			// 2.1) Versioned sets are different
			else if (!firstVersionedSet.getUuid().equals(secondVersionedSet.getUuid())) {
				// Create new versioned set and copy from the old one the
				// strategy and the visibility, cause these thing are the
				// same for both versioned sets
				mergedVersionedSet = new VersionedSet();
				mergedVersionedSet.setStrategy(firstVersionedSet.getStrategy());
				mergedVersionedSet.setVisible(firstVersionedSet.isVisible());
				
				this.findDifferencesAndSimilarities(firstVersionedSet, secondVersionedSet, versionedSetResult);
				
				// The old versioned set is the leading one
				if (oldIsLeading) {
					try {
						// The old versioned set was chosen as a leading, this simply means
						// we place all objects from it to the merged versioned set
						// But before that we merge the objects from the first versioned set
						// with the object from the second one
						for (final ObjectResult objectResult : versionedSetResult.getResults()) {
							this.mergeAndResolve(objectResult.getFirst(), objectResult.getSecond(), mergedVersionedSet);
						}
						// Add objects which available only in second versioned set
						for (final Object objectAvailableOnlyInSecondVersionedSet : this.objectsAvailableOnlyInSecondVersionedSet) {
							mergedVersionedSet.addVersionedObject(objectAvailableOnlyInSecondVersionedSet);
						}
						// Merge field->uuid pairs
						this.mergeFieldUuidsPairs(firstVersionedSet, secondVersionedSet, mergedVersionedSet);
					}
					catch (final Exception e) {
						Merger.logger.error(e.getMessage());
					}
				}
				// The new versioned set is the leading one
				else {
					try {
						// The new versioned set was chosen as a leading, this simply means
						// we place all objects from it to the merged versioned set
						// But before that we merge the objects from the second versioned
						// set with the object from the first one
						for (final ObjectResult objectResult : versionedSetResult.getResults()) {
							this.mergeAndResolve(objectResult.getSecond(), objectResult.getFirst(), mergedVersionedSet);
						}
						// Add objects which available only in the first versioned set
						for (final Object objectAvailableOnlyInFirstVersionedSet : this.objectsAvailableOnlyInFirstVersionedSet) {
							mergedVersionedSet.addVersionedObject(objectAvailableOnlyInFirstVersionedSet);
						}
						// Merge field->uuid pairs
						this.mergeFieldUuidsPairs(secondVersionedSet, firstVersionedSet, mergedVersionedSet);
					}
					catch (final Exception e) {
						Merger.logger.error(e.getMessage());
					}
				}
				// Specify versioning relations for newly created versioned set
				try {
					mergedVersionedSet.addPredecessorVersioning(firstVersionedSet);
					mergedVersionedSet.addPredecessorVersioning(secondVersionedSet);
				}
				catch (final Exception e) {
					Merger.logger.error(e.getMessage());
				}
				// Set new name for the merged versioned set
				mergedVersionedSet
					.setName(StringUtil.getInstance().getNameWithNewVersion(
						StringUtil.getInstance()
							.getNameWithEldestVersion(firstVersionedSet.getName(), secondVersionedSet.getName())));
				
				mergedVersionedSets.add(mergedVersionedSet);
			}
			// 2.2) Versioned sets are the same
			// Doesn't matter first or second it should be the same versioned
			// set
			else {
				mergedVersionedSets.add(firstVersionedSet);
			}
		}
		
		// Specify bindings relations between merged versioned sets
		if (!mergedVersionedSets.isEmpty()) {
			for (final VersionedSet owner : mergedVersionedSets) {
				for (final VersionedSet subordinate : mergedVersionedSets) {
					try {
						owner.addPredecessorBinding(subordinate);
					}
					catch (final IllegalArgumentException e) {
						Merger.logger.debug(e.getMessage() + " The versioned set of type - " + owner.getType()
																+ "can't be bound by the versioned set of type - " + subordinate.getType());
					}
				}
			}
		}
		
		return mergedVersionedSets;
	}
	
	/**
	 * @param firstVersionedSet
	 * @param secondVersionedSet
	 * @param mergedVersionedSet
	 */
	private void mergeFieldUuidsPairs(final VersionedSet firstVersionedSet, final VersionedSet secondVersionedSet,
		final VersionedSet mergedVersionedSet) {
		// Merge field->uuid pairs
		// Copy all pairs from the leading versioned set
		mergedVersionedSet.setFieldUuidsPairs(firstVersionedSet.getFieldUuidsPairs());
		// The we have to check, if the uuid of an object is exist after the
		// merge, then we have to copy to the fieldUuidsPairs
		for (final Entry<String, Set<String>> fieldUuidsPair : secondVersionedSet.getFieldUuidsPairs().entrySet()) {
			for (final String uuid : fieldUuidsPair.getValue()) {
				if (firstVersionedSet.getUuidsOfObjects().contains(uuid)) {
					firstVersionedSet.addFieldUuidPair(fieldUuidsPair.getKey(), uuid);
				}
			}
		}
	}
	
	private void mergeAndResolve(final Object firstObject, final Object secondObject,
		final VersionedSet mergedVersionedSet) {
		try {
			if (firstObject != null) {
				
				// If second object not we'll try to resolve and merge it with the first
				// one
				if (secondObject != null) {
					
					boolean shouldBeresolved = false;
					Constraint constraint =
						(Constraint) ReflectionUtil.getInstance().getAnnotation(firstObject, Constraint.class);
					if (constraint != null) {
						shouldBeresolved = true;
					}
					
					Object firstObjectBeforeMerge = null;
					
					// TODO Review and modify
					if (shouldBeresolved) {
						firstObjectBeforeMerge = DeepUtils.getInstance().deepClone(firstObject, 3, null);
					}
					
					// Merge internal structure of the objects
					ReflectionUtil.getInstance().mergeObjects(firstObject, secondObject);
					
					if (shouldBeresolved) {
						// Before we add a leading object to the merged versioned
						// set we have to check its consistency, in case if this
						// object was annotated with @Constraint annotation
						final Class<? extends Resolverable> resolverClass = constraint.resolver();
						// Create instance of the resolver class
						// which was specified at the annotation
						final Resolverable resolverInstanceOfClass = resolverClass.newInstance();
						// Take the status of the resolve process
						final boolean isConsistent = resolverInstanceOfClass.check(firstObject);
						// System.out.println("Is Consistent " + isConsistent + " " +
						// firstObject.getClass());
						if (!isConsistent) {
							resolverInstanceOfClass.resolve(firstObjectBeforeMerge, firstObject, secondObject);
							// System.out.println("Resolve " + firstObject.getClass());
						}
					}
					
				}
				
				mergedVersionedSet.addVersionedObject(firstObject);
			}
		}
		catch (final Exception e) {
			Merger.logger.error(e.getMessage());
		}
	}
	
	private void findDifferencesAndSimilarities(final VersionedSet firstVersionedSet,
		final VersionedSet secondVersionedSet, final VersionedSetResult versionedSetResult) {
		
		this.objectsAvailableOnlyInFirstVersionedSet = Sets.newHashSet();
		this.objectsAvailableOnlyInSecondVersionedSet = Sets.newHashSet();
		this.overwrittenObjectsFromFirstVersinedSet = Sets.newHashSet();
		this.overwrittenObjectsFromSecondVersinedSet = Sets.newHashSet();
		
		this.objectsAvailableOnlyInFirstVersionedSet.addAll(Sets.difference(firstVersionedSet.getVersionedObjects(),
			secondVersionedSet.getVersionedObjects()));
		this.objectsAvailableOnlyInSecondVersionedSet.addAll(Sets.difference(secondVersionedSet.getVersionedObjects(),
			firstVersionedSet.getVersionedObjects()));
		
		// Objects of objectsAvailableInBothVersionedSets will contain objects
		// with content from the first versioned set like 2->'b' object
		final Set<Object> objectsAvailableInBothConetntFromFirst =
			Sets.intersection(firstVersionedSet.getVersionedObjects(), secondVersionedSet.getVersionedObjects());
		for (final Object object : objectsAvailableInBothConetntFromFirst) {
			// Some objects are similar so indeed they won't be overwritten, so
			// we have to find out which object are not similar, but available in
			// both sets
			if ((versionedSetResult.getResult(object) != null) && !versionedSetResult.getResult(object).isEqual()) {
				this.overwrittenObjectsFromFirstVersinedSet.add(object);
			}
		}
		
		// Objects of objectsAvailableInBothVersionedSets will contain objects
		// with content from the second versioned set like 2->'d' object
		final Set<Object> objectsAvailableInBothConetntFromSecond =
			Sets.intersection(secondVersionedSet.getVersionedObjects(), firstVersionedSet.getVersionedObjects());
		for (final Object object : objectsAvailableInBothConetntFromSecond) {
			// Some objects are similar so indeed they won't be overwritten, so
			// we have to find out which object are not similar, but available in
			// both sets
			if (!versionedSetResult.getResult(object).isEqual()) {
				this.overwrittenObjectsFromSecondVersinedSet.add(object);
			}
		}
		
	}
}
