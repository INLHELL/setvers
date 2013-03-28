/*
 * Package: de.bitub.proitbau.versioning.main
 * Project: Setvers
 * File: NewComparator.java
 * Date: 18.08.2010
 * Time: 12:59:36
 * Company: TU-Berlin
 * Author: Vladislav Fedotov
 * E-mail: <a href="mailto:vladislav.fedotov@tu-berlin.de">Vladislav Fedotov</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.model.logic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.compare_results.FieldResult;
import de.bitub.proitbau.common.versioning.compare_results.ModificationType;
import de.bitub.proitbau.common.versioning.compare_results.ObjectResult;
import de.bitub.proitbau.common.versioning.compare_results.StateResult;
import de.bitub.proitbau.common.versioning.compare_results.VersionedSetResult;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.util.ReflectionUtil;

public class Comparator {
	
	private Comparator() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static Comparator instance = new Comparator();
	}
	
	@SuppressWarnings("synthetic-access")
	public static Comparator getInstance() {
		return Handler.instance;
	}
	
	public StateResult compareStatesOfVersionedSets(final Set<VersionedSet> oldSetOfVersionedSets,
		final Set<VersionedSet> newSetOfVersionedSets) throws Exception {
		Preconditions.checkNotNull(newSetOfVersionedSets, "Given set of second versioned sets is null!");
		Preconditions.checkArgument(!newSetOfVersionedSets.isEmpty(), "Given set of second versioned sets is empty!");
		
		final StateResult stateResult = new StateResult();
		
		final List<VersionedSet> newVersionedSetsWithoutPair = new ArrayList<VersionedSet>(newSetOfVersionedSets);
		// We assume that we compare two states first and second
		if ((oldSetOfVersionedSets != null) && (!oldSetOfVersionedSets.isEmpty())) {
			// Lets find the pairs for all versioned sets based on their class type
			for (final VersionedSet oldVersionedSet : oldSetOfVersionedSets) {
				// boolean hasPairFound = false;
				for (final VersionedSet newVersionedSet : newSetOfVersionedSets) {
					// The VersionedSets are presented in the both states
					final boolean isTypeOfVersionedSetsEqual = oldVersionedSet.getType().equals(newVersionedSet.getType());
					// Hooray, we have found a pair for the versioned set
					if (isTypeOfVersionedSetsEqual) {
						// hasPairFound = true;
						stateResult.addResult(this.compareVersionedSets(oldVersionedSet, newVersionedSet));
						newVersionedSetsWithoutPair.remove(newVersionedSet);
					}
				}
			}
		}
		for (final VersionedSet newVersionedSet : newVersionedSetsWithoutPair) {
			stateResult.addResult(this.compareVersionedSets(null, newVersionedSet));
		}
		return stateResult;
	}
	
	//@formatter:off
	// vs_1 <->   -   - del
	// vs_2 <-> vs_2* - mdf
	//   -  <-> vs_3  - crt
	// vs_4 <-> vs_4  - unm
	// @formatter:on
	public VersionedSetResult
		compareVersionedSets(final VersionedSet oldVersionedSet, final VersionedSet newVersionedSet) throws Exception {
		Preconditions.checkNotNull(newVersionedSet, "Given second versioned set is null!");
		Preconditions.checkArgument(!newVersionedSet.getUuidsOfObjects().isEmpty(), "Given second versioned set is empty!");
		
		final VersionedSetResult versionedSetResult = new VersionedSetResult();
		// We assume that we compare two versioned sets the second and the first
		// both of
		// them should be not null, and have the same class type
		if ((oldVersionedSet != null)) {
			versionedSetResult.setName(oldVersionedSet.getName());
			versionedSetResult.setFirst(oldVersionedSet);
			versionedSetResult.setSecond(newVersionedSet);
			
			// To identify similarity in a fast way, we compare first the number of
			// uuid's in both versioned sets, if they have different number of uuid's
			// then they are different
			final boolean isNumberOfUuidsDifferent =
				oldVersionedSet.getVersionedObjects().size() != newVersionedSet.getVersionedObjects().size();
			if (isNumberOfUuidsDifferent) {
				versionedSetResult.setEqual(false);
			}
			
			// Objects presented only in the firts versioned set
			final Set<Object> objectsPresentedOnlyInOldSet =
				Sets.difference(oldVersionedSet.getVersionedObjects(), newVersionedSet.getVersionedObjects());
			// Objects presented only in the second versioned set
			final Set<Object> objectsPresentedOnlyInNewSet =
				Sets.difference(newVersionedSet.getVersionedObjects(), oldVersionedSet.getVersionedObjects());
			// It might be that the versioned sets contain the similar objects (with
			// same uuid's) and they have no diff.!!!
			
			// Objects presented in the first versioned set and in the second
			// versioned set, the uuid's of these objects are identical but
			// the content might be different
			final List<Object> objectsPresentedInBothSetsFromOldSet =
				new LinkedList<Object>(Sets.difference(oldVersionedSet.getVersionedObjects(), objectsPresentedOnlyInOldSet));
			
			// Objects presented in the second versioned set and in the first
			// versioned set, the uuid's of these objects are identical but
			// the content might be different
			final List<Object> objectsPresentedInBothSetsFromNewSet =
				new LinkedList<Object>(Sets.difference(newVersionedSet.getVersionedObjects(), objectsPresentedOnlyInNewSet));
			
			// 1) Add objects which are presented in both sets
			for (int i = 0; i < objectsPresentedInBothSetsFromOldSet.size(); i++) {
				final Object objectFromOldVersionedSet = objectsPresentedInBothSetsFromOldSet.get(i);
				// Find a pair for the object from firts set in the second set
				final Object objectFromNewVersionedSet =
					objectsPresentedInBothSetsFromNewSet.get(objectsPresentedInBothSetsFromNewSet
						.indexOf(objectFromOldVersionedSet));
				versionedSetResult.addResult(this.compareObjects(objectFromOldVersionedSet, objectFromNewVersionedSet));
			}
			
			// 2) Then add objects presented only in the firts versioned set
			// This simply means we are going to add objects which were deleted in the
			// actual or second state
			for (final Object objectFromOldVersionedSet : objectsPresentedOnlyInOldSet) {
				versionedSetResult.addResult(this.compareObjects(objectFromOldVersionedSet, null));
			}
			
			// 3) Then add objects presented only in the second versioned set
			// This simply means we are going to add objects which were created in the
			// actual or second state
			for (final Object objectFromNewVersionedSet : objectsPresentedOnlyInNewSet) {
				versionedSetResult.addResult(this.compareObjects(null, objectFromNewVersionedSet));
			}
			if (!versionedSetResult.isEqual()) {
				versionedSetResult.setModificationType(ModificationType.MODIFIED);
			}
		}
		// Old versioned set is null
		else {
			versionedSetResult.setName(newVersionedSet.getName());
			versionedSetResult.setSecond(newVersionedSet);
			for (final Object objectFromNewVersionedSet : newVersionedSet.getVersionedObjects()) {
				versionedSetResult.addResult(this.compareObjects(null, objectFromNewVersionedSet));
			}
			versionedSetResult.setModificationType(ModificationType.CREATED);
		}
		return versionedSetResult;
	}
	
	public ObjectResult compareObjects(final Object oldObject, final Object newObject) throws Exception {
		final ObjectResult objectResult = new ObjectResult();
		// 1) First case, object presented in the both versioned sets
		if ((oldObject != null) && (newObject != null) && oldObject.equals(newObject)) {
			objectResult.setFirst(oldObject);
			objectResult.setSecond(newObject);
			objectResult.setName(ReflectionUtil.getInstance().getNameOfVersionedEntity(oldObject.getClass()));
			final Collection<Field> fields = ReflectionUtil.getInstance().getComparableFields(oldObject);
			for (final Field field : fields) {
				objectResult.addResult(this.compareFields(oldObject, newObject, field));
			}
			if (!objectResult.isEqual()) {
				objectResult.setModificationType(ModificationType.MODIFIED);
			}
		}
		// 2) Second case, only object from first versioned set is available
		else if ((oldObject != null) && (newObject == null)) {
			objectResult.setName(ReflectionUtil.getInstance().getNameOfVersionedEntity(oldObject.getClass()));
			objectResult.setFirst(oldObject);
			final Collection<Field> fields = ReflectionUtil.getInstance().getComparableFields(oldObject);
			for (final Field field : fields) {
				objectResult.addResult(this.compareFields(oldObject, null, field));
			}
			objectResult.setModificationType(ModificationType.DELETED);
		}
		// 3) Third case, object only from second versioned set available
		else if ((oldObject == null) && (newObject != null)) {
			objectResult.setName(ReflectionUtil.getInstance().getNameOfVersionedEntity(newObject.getClass()));
			objectResult.setSecond(newObject);
			final Collection<Field> fields = ReflectionUtil.getInstance().getComparableFields(newObject);
			for (final Field field : fields) {
				objectResult.addResult(this.compareFields(null, newObject, field));
			}
			objectResult.setModificationType(ModificationType.CREATED);
		}
		else {
			throw new Exception("Both Objects are null or they are unequal!");
		}
		return objectResult;
	}
	
	public FieldResult compareFields(final Object oldObject, final Object newObject, final Field field) throws Exception {
		Preconditions.checkNotNull(field, "Given field is null!");
		final FieldResult fieldResult = new FieldResult();
		// 1) First case, both objects aren't null and we can compare their values
		if ((oldObject != null) && (newObject != null)) {
			fieldResult.setName(ReflectionUtil.getInstance().getNameOfField(oldObject.getClass(), field));
			fieldResult.setVisible(ReflectionUtil.getInstance().isFiledVisible(oldObject.getClass(), field));
			fieldResult.setOrderIndex(ReflectionUtil.getInstance().getOrderIndexOfField(oldObject.getClass(), field));
			final Object oldValue = ReflectionUtil.getInstance().getValueOfField(oldObject, field);
			final Object newValue = ReflectionUtil.getInstance().getValueOfField(newObject, field);
			fieldResult.setFirst(oldValue);
			fieldResult.setSecond(newValue);
			// @formatter:off
			// First of all we are going to check if the values are null (both of them)
			// then, if the values of a collection type (and contains the same values)
			// then, if they are of a map type (and contains the same keys and values)
			// then, if they are of array type (and contains the same values)
			// @formatter:on
			if ((oldValue == null) && (newValue == null)) {
				fieldResult.setEqual(true);
			}
			else if ((oldValue == null) || (newValue == null)) {
				fieldResult.setEqual(false);
			}
			else if (this.isCollectionEqual(oldValue, newValue)) {
				fieldResult.setEqual(true);
			}
			else if (this.isMapEqual(oldValue, newValue)) {
				fieldResult.setEqual(true);
			}
			else if (this.isArrayEqual(oldValue, newValue)) {
				fieldResult.setEqual(true);
			}
			else if (oldValue.equals(newValue)) {
				fieldResult.setEqual(true);
			}
			else {
				fieldResult.setEqual(false);
			}
			if (!fieldResult.isEqual()) {
				fieldResult.setModificationType(ModificationType.MODIFIED);
			}
		}
		// 2) Second case, first object isn't null but the second one is (this means
		// that it was deleted)
		else if (oldObject != null) {
			fieldResult.setName(ReflectionUtil.getInstance().getNameOfField(oldObject.getClass(), field));
			fieldResult.setVisible(ReflectionUtil.getInstance().isFiledVisible(oldObject.getClass(), field));
			fieldResult.setOrderIndex(ReflectionUtil.getInstance().getOrderIndexOfField(oldObject.getClass(), field));
			final Object oldValue = ReflectionUtil.getInstance().getValueOfField(oldObject, field);
			fieldResult.setFirst(oldValue);
			fieldResult.setEqual(false);
			fieldResult.setModificationType(ModificationType.DELETED);
		}
		// 3) Third case, second object isn't null (this means that it was created)
		else if (newObject != null) {
			fieldResult.setName(ReflectionUtil.getInstance().getNameOfField(newObject.getClass(), field));
			fieldResult.setVisible(ReflectionUtil.getInstance().isFiledVisible(newObject.getClass(), field));
			fieldResult.setOrderIndex(ReflectionUtil.getInstance().getOrderIndexOfField(newObject.getClass(), field));
			final Object newValue = ReflectionUtil.getInstance().getValueOfField(newObject, field);
			fieldResult.setSecond(newValue);
			fieldResult.setEqual(false);
			fieldResult.setModificationType(ModificationType.CREATED);
		}
		return fieldResult;
	}
	
	private boolean isCollectionEqual(final Object oldValue, final Object newValue) {
		boolean isCollentionEqual = false;
		// @formatter:off
		if (
			(oldValue instanceof Collection) 
			&& 
			((Collection<?>) oldValue).containsAll((Collection<?>) newValue)
			&& 
			((Collection<?>) newValue).containsAll((Collection<?>) oldValue)
		) {
		// @formatter:on 
			isCollentionEqual = true;
		}
		return isCollentionEqual;
	}
	
	private boolean isMapEqual(final Object oldValue, final Object newValue) {
		boolean isMapEqual = false;
		// @formatter:off
		if (
			(oldValue instanceof Map) 
			&& 
			((Map<?, ?>) oldValue).values().containsAll(((Map<?, ?>) newValue).values())
			&& 
			((Map<?, ?>) oldValue).keySet().containsAll(((Map<?, ?>) newValue).keySet())
			&& 
			((Map<?, ?>) newValue).values().containsAll(((Map<?, ?>) oldValue).values())
			&& 
			((Map<?, ?>) newValue).keySet().containsAll(((Map<?, ?>) oldValue).keySet())
		) {
		// @formatter:on
			isMapEqual = true;
		}
		return isMapEqual;
	}
	
	private boolean isArrayEqual(final Object oldValue, final Object newValue) {
		boolean isArrayEqual = false;
		// @formatter:off
		if (
			(oldValue instanceof Object[]) 
			&& 
			Arrays.deepEquals((Object[]) oldValue, (Object[]) newValue)
		) {
		// @formatter:on
			isArrayEqual = true;
		}
		return isArrayEqual;
	}
	
}
