/*
 * Package: de.bitub.proitbau.versioning.main
 * Project: Setvers
 * File: VersionedSet.java
 * Date: 25.05.2010
 * Time: 13:26:33
 * Company: TU-Berlin
 * @author: Vladislav Fedotov
 * E-mail: <a href="mailto:vladislav.fedotov@tu-berlin.de">Vladislav Fedotov</a>
 * @version: 3.3.0.0
 */
package de.bitub.proitbau.common.versioning.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.annotations.SetStrategy;
import de.bitub.proitbau.common.versioning.annotations.VersionedEntity;
import de.bitub.proitbau.common.versioning.util.BindingChecker;
import de.bitub.proitbau.common.versioning.util.ReflectionUtil;
import de.bitub.proitbau.common.versioning.util.SubTypeCanNotBeFoundException;

public class VersionedSet {
	
	/*
	 * Contains the simple name of class which are the part of this versioned set
	 * or combination of two simple class names, if versioned set was created with
	 * the SET_PER_OBJECT_TYPE strategy
	 */
	private VersionedSetType type = null;
	
	private String description = "Some description";
	
	private String name = null;
	
	private Set<VersionedSet> predecessorsBinding = Sets.newHashSetWithExpectedSize(30);
	
	private Set<String> predecessorsVersioning = Sets.newHashSetWithExpectedSize(30);
	
	private SetStrategy strategy = SetStrategy.SET_PER_CLASS;
	
	private String uuid = null;
	
	private Set<String> uuidsOfObjects = Sets.newHashSetWithExpectedSize(100);
	
	private Set<Object> versionedObjects = Sets.newHashSetWithExpectedSize(100);
	
	private boolean visible = false;
	
	// This field was added for the reversConverter, it contains pairs of field
	// and uuid's
	// In other words this field has an information from which fields the objects
	// with specific uuid's came
	private Map<String, Set<String>> fieldUuidsPairs = new HashMap<String, Set<String>>(30);
	
	public VersionedSet() {
		this.uuid = UUID.randomUUID().toString();
		this.name = "Default";
	}
	
	public VersionedSet(final String name) {
		this.uuid = UUID.randomUUID().toString();
		this.name = name;
	}
	
	public boolean addPredecessorBinding(final VersionedSet predecessorBinding) throws IllegalArgumentException {
		// TODO This preconditions and method itself will not work with the other
		// strategies, cause versioned sets might have no class type
		Preconditions.checkNotNull(predecessorBinding, "Given predecessor binding is null!");
		Preconditions
			.checkArgument(predecessorBinding.type != null, "The class type of given predecessor binding is null!");
		Preconditions.checkArgument(this.type != null, "The class type of this versioned set is null!");
		Preconditions.checkArgument(this.type != predecessorBinding.type,
			"The class type of this versioned set is differs from given predecessor binding!");
		// @formatter:off
		// if the set of predecessors binding versioned sets is empty 
		//		if predecessor binding versioned set is bound by this versioned set
		//				add predecessor binding versioned set to the predecessors binding set
		//		else
		//				return result equals false
		// else 
		// 		if no others versioned sets with the same type is not contained in the set of predecessors bindings versioned sets and if they bound
		//				add predecessor binding versioned set to the predecessors binding set
		// 		else 
		//        return result equals false
		// @formatter:on
		boolean isAdded = false;
		if (this.predecessorsBinding.isEmpty()) {
			if (BindingChecker.getInstance().isBoundBy(this, predecessorBinding)) {
				this.predecessorsBinding.add(predecessorBinding);
				isAdded = true;
			}
			else {
				isAdded = false;
			}
		}
		else {
			boolean isSetAlreadyContainsGivenVersionedSet = false;
			final Iterator<VersionedSet> predecessorsBindingIterator = this.predecessorsBinding.iterator();
			while (isSetAlreadyContainsGivenVersionedSet && predecessorsBindingIterator.hasNext()) {
				final VersionedSet predecessorBindingVersionedSet = predecessorsBindingIterator.next();
				if (predecessorBindingVersionedSet.type.equals(predecessorBinding.getType())) {
					isSetAlreadyContainsGivenVersionedSet = true;
					isAdded = false;
				}
			}
			if (!isSetAlreadyContainsGivenVersionedSet && BindingChecker.getInstance().isBoundBy(this, predecessorBinding)) {
				this.predecessorsBinding.add(predecessorBinding);
				isAdded = true;
			}
		}
		return isAdded;
	}
	
	public boolean addPredecessorVersioning(final VersionedSet predecessorVersioning) throws Exception {
		Preconditions.checkNotNull(predecessorVersioning, "Given predecessor versioned set is null!");
		Preconditions.checkArgument(predecessorVersioning.type != null,
			"The class type of given predecessor versioned set is null!");
		Preconditions.checkArgument(this.type != null, "The class type of this versioned set is null!");
		boolean isAdded = false;
		if (this.type.equals(predecessorVersioning.type)) {
			this.predecessorsVersioning.add(predecessorVersioning.getUuid());
			isAdded = true;
		}
		return isAdded;
	}
	
	public boolean addVersionedObject(final Object object) throws Exception {
		Preconditions.checkNotNull(object, "Given object is null!");
		Preconditions.checkArgument(object.getClass().isAnnotationPresent(VersionedEntity.class),
			"Given object is not a versioned entity!");
		boolean isSuccessfullyAdded = false;
		switch (this.strategy) {
			case SET_PER_CLASS: {
				isSuccessfullyAdded = this.addObjectSetPerClassStrategy(object);
			}
				break;
			case SET_PER_CONTAINER: {
				isSuccessfullyAdded = this.addObjectSetPerContainerStrategy(object);
			}
				break;
			case SET_PER_SUPERCLASS: {
				isSuccessfullyAdded = this.addObjectSetPerSuperclassStrategy(object);
			}
				break;
			case SET_PER_OBJECT_TYPE: {
				isSuccessfullyAdded = this.addObjectSetPerObjectTypeStrategy(object);
			}
				break;
		}
		return isSuccessfullyAdded;
	}
	
	public void removeAllVersionedObjects() {
		this.uuidsOfObjects.clear();
		this.versionedObjects.clear();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof VersionedSet) {
			final VersionedSet other = (VersionedSet) obj;
			return Objects.equal(this.uuid, other.getUuid());
		}
		return false;
	}
	
	public VersionedSetType getType() {
		return this.type;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<VersionedSet> getPredecessorsBinding() {
		return this.predecessorsBinding;
	}
	
	public Set<String> getPredecessorsVersioning() {
		return this.predecessorsVersioning;
	}
	
	public SetStrategy getStrategy() {
		return this.strategy;
	}
	
	public String getUuid() {
		return this.uuid;
	}
	
	public Set<String> getUuidsOfObjects() {
		return this.uuidsOfObjects;
	}
	
	public Set<Object> getVersionedObjects() {
		return this.versionedObjects;
	}
	
	public void setVersionedObjects(final Set<Object> versionedObjects) {
		this.versionedObjects = versionedObjects;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.uuid);
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public boolean removePredecessorBinding(final VersionedSet versionedSet) {
		if (!this.predecessorsBinding.isEmpty()) {
			return this.predecessorsBinding.remove(versionedSet);
		}
		return false;
	}
	
	public boolean removePredecessorVersioning(final String uuidOfPredecessorVersionedSet) {
		if (!this.predecessorsVersioning.isEmpty()) {
			return this.predecessorsVersioning.remove(uuidOfPredecessorVersionedSet);
		}
		return false;
	}
	
	public void setType(final VersionedSetType type) {
		this.type = type;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public void setStrategy(final SetStrategy strategy) {
		this.strategy = strategy;
	}
	
	public void setUuidsOfObjects(final Set<String> UUIDOfObjects) {
		this.uuidsOfObjects = UUIDOfObjects;
	}
	
	public void setVisible(final boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.add("Name",this.name)
			.add("Predecessor Bindings",this.getPredecessorsBinding())
			.toString();
		// @formatter:on
	}
	
	protected void setPredecessorsBinding(final Set<VersionedSet> predecessorsBinding) {
		this.predecessorsBinding = predecessorsBinding;
	}
	
	protected void setPredecessorsVersioning(final Set<String> predecessorsVersioning) {
		this.predecessorsVersioning = predecessorsVersioning;
	}
	
	protected void setUuid(final String UUID) {
		this.uuid = UUID;
	}
	
	private boolean addObjectSetPerClassStrategy(final Object object) {
		boolean wasAdded = false;
		if (this.type != null) {
			if (this.type.getMainType().equals(object.getClass()) && this.versionedObjects.add(object)) {
				this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
				wasAdded = true;
			}
		}
		else if (this.versionedObjects.add(object)) {
			this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
			this.type = new VersionedSetType(object.getClass(), null);
			wasAdded = true;
		}
		return wasAdded;
	}
	
	private boolean addObjectSetPerContainerStrategy(final Object object) {
		boolean wasAdded = false;
		if (this.name.equals(object.getClass().getAnnotation(VersionedEntity.class).name())) {
			this.versionedObjects.add(object);
			this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
			wasAdded = true;
		}
		return wasAdded;
	}
	
	private boolean addObjectSetPerSuperclassStrategy(final Object object) {
		boolean wasAdded = false;
		if (this.name.equals(ReflectionUtil.getInstance().getNameOfSuperclass(object))) {
			this.versionedObjects.add(object);
			this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
			wasAdded = true;
		}
		return wasAdded;
	}
	
	private boolean addObjectSetPerObjectTypeStrategy(final Object object) {
		boolean wasAdded = false;
		if (this.type != null) {
			if (this.type.getMainType().equals(object.getClass()) && this.versionedObjects.add(object)) {
				this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
				wasAdded = true;
			}
		}
		else if (this.versionedObjects.add(object)) {
			this.uuidsOfObjects.add(ReflectionUtil.getInstance().getUuidOfObject(object));
			try {
				this.type = new VersionedSetType(object.getClass(), ReflectionUtil.getInstance().getSubType(object));
			}
			catch (final SubTypeCanNotBeFoundException e) {
				e.printStackTrace();
			}
			wasAdded = true;
		}
		return wasAdded;
	}
	
	public Map<String, Set<String>> getFieldUuidsPairs() {
		return this.fieldUuidsPairs;
	}
	
	public void setFieldUuidsPairs(final Map<String, Set<String>> fieldUuidsPairs) {
		this.fieldUuidsPairs = fieldUuidsPairs;
	}
	
	public void addFieldUuidPair(final Field field, final String uuid) {
		if (this.fieldUuidsPairs.containsKey(field.toGenericString())) {
			this.fieldUuidsPairs.get(field.toGenericString()).add(uuid);
		}
		else {
			this.fieldUuidsPairs.put(field.toGenericString(), Sets.newHashSet(uuid));
		}
	}
	
	public void addFieldUuidPair(final String field, final String uuid) {
		if (this.fieldUuidsPairs.containsKey(field)) {
			this.fieldUuidsPairs.get(field).add(uuid);
		}
		else {
			this.fieldUuidsPairs.put(field, Sets.newHashSet(uuid));
		}
	}
	
	public void addFieldUuidsPair(final Field field, final Set<String> uuids) {
		if (this.fieldUuidsPairs.containsKey(field.toGenericString())) {
			this.fieldUuidsPairs.get(field.toGenericString()).addAll(uuids);
		}
		else {
			this.fieldUuidsPairs.put(field.toGenericString(), Sets.newHashSet(uuids));
		}
	}
	
}
