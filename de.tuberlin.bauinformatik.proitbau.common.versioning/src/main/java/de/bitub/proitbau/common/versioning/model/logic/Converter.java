package de.bitub.proitbau.common.versioning.model.logic;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.annotations.DomainModel;
import de.bitub.proitbau.common.versioning.annotations.SetStrategy;
import de.bitub.proitbau.common.versioning.annotations.VersionedEntity;
import de.bitub.proitbau.common.versioning.model.ModelCache;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.model.VersionedSetType;
import de.bitub.proitbau.common.versioning.util.ReflectionUtil;
import de.bitub.proitbau.common.versioning.util.StringUtil;
import de.bitub.proitbau.common.versioning.util.SubTypeCanNotBeFoundException;

public class Converter {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(Converter.class);
	{
		Converter.logger.setLevel(Level.INFO);
	}
	
	private Converter() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static Converter instance = new Converter();
	}
	
	@SuppressWarnings("synthetic-access")
	public static Converter getInstance() {
		return Handler.instance;
	}
	
	public Set<VersionedSet> convert(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Set<VersionedSet> newlyCreatedSetOfVersionedSets = this.convertToVersionedSets(Sets.newHashSet(object));
		Preconditions.checkNotNull(newlyCreatedSetOfVersionedSets, "Newly created set of versioned sets is null!");
		Preconditions.checkArgument(!newlyCreatedSetOfVersionedSets.isEmpty(),
			"Newly created set of versioned sets is empty!");
		newlyCreatedSetOfVersionedSets = this.specifyBindings(newlyCreatedSetOfVersionedSets);
		return newlyCreatedSetOfVersionedSets;
	}
	
	public Set<VersionedSet> convert(final Object... objects) {
		Preconditions.checkNotNull(objects, "Given objects is null!");
		Preconditions.checkArgument(objects.length > 0, "Given object array is empty!");
		Set<VersionedSet> newlyCreatedSetOfVersionedSets = this.convertToVersionedSets(Sets.newHashSet(objects));
		Preconditions.checkNotNull(newlyCreatedSetOfVersionedSets, "Newly created set of versioned sets is null!");
		Preconditions.checkArgument(!newlyCreatedSetOfVersionedSets.isEmpty(),
			"Newly created set of versioned sets is empty!");
		newlyCreatedSetOfVersionedSets = this.specifyBindings(newlyCreatedSetOfVersionedSets);
		return newlyCreatedSetOfVersionedSets;
	}
	
	public Set<VersionedSet> convert(final Set<Object> model) {
		Preconditions.checkNotNull(model, "Given model is null!");
		Preconditions.checkArgument(!model.isEmpty(), "Given model is empty!");
		Set<VersionedSet> newlyCreatedSetOfVersionedSets = this.convertToVersionedSets(model);
		Preconditions.checkNotNull(newlyCreatedSetOfVersionedSets, "Newly created set of versioned sets is null!");
		Preconditions.checkArgument(!newlyCreatedSetOfVersionedSets.isEmpty(),
			"Newly created set of versioned sets is empty!");
		newlyCreatedSetOfVersionedSets = this.specifyBindings(newlyCreatedSetOfVersionedSets);
		return newlyCreatedSetOfVersionedSets;
	}
	
	private SetStrategy chooseStrategy(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Preconditions.checkArgument(object.getClass().isAnnotationPresent(VersionedEntity.class),
			"Given object is not a Versioned Entity!");
		
		final VersionedEntity specifiedStrategy = object.getClass().getAnnotation(VersionedEntity.class);
		
		return specifiedStrategy.setStrategy();
	}
	
	private Set<VersionedSet> convertToVersionedSets(final Set<Object> model) {
		Preconditions.checkNotNull(model, "Given set is null!");
		Preconditions.checkArgument(model.size() > 0, "Given set is empty!");
		
		// Invalidate model cache, cause it might contain the results of the
		// previous conversion
		ModelCache.getInstance().invalidateObjectValuesCache();
		
		final Map<VersionedSetType, VersionedSet> createdVersionedSets = Maps.newHashMap();
		final Multimap<Boolean, Object> stackOfModelObjects = ArrayListMultimap.create(2, 5000);
		
		for (final Object modelObject : model) {
			stackOfModelObjects.put(Boolean.FALSE, modelObject);
		}
		
		// A map of class type associated with a multimap,
		// where the keys are fields of the given object and values are uuids
		Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType = null;
		
		// We will continue until there is an object which wasn't visited is existed
		// in the stack
		while (stackOfModelObjects.containsKey(Boolean.FALSE)) {
			final Object modelObject = stackOfModelObjects.get(Boolean.FALSE).iterator().next();
			stackOfModelObjects.remove(Boolean.FALSE, modelObject);
			stackOfModelObjects.put(Boolean.TRUE, modelObject);
			
			final Class<?> classOfModelObject = modelObject.getClass();
			// We found the container object, so called domain model object
			// We form such a structure class->field->uuid, afterwards we will use
			// this structure, we will put it into the newly created versioned sets
			// We need this information for the Revert class which can create domain
			// model based on versioned sets and this information
			if (classOfModelObject.isAnnotationPresent(DomainModel.class)) {
				fieldUuidsPairsBasedOnClassType = ReflectionUtil.getInstance().getFieldUuidsPairsBasedOnClassType(modelObject);
			}
			else if (classOfModelObject.isAnnotationPresent(VersionedEntity.class)) {
				final SetStrategy versionedSetStrategyOfThisObject = this.chooseStrategy(modelObject);
				switch (versionedSetStrategyOfThisObject) {
					case SET_PER_CLASS: {
						this.createVersionedSetWithSetPerClassStrategy(createdVersionedSets, fieldUuidsPairsBasedOnClassType,
							modelObject);
					}
						break;
					case SET_PER_CONTAINER: {
						this.createVersionedSetWithSetPerConrainerStrategy(createdVersionedSets, fieldUuidsPairsBasedOnClassType,
							modelObject);
					}
						break;
					case SET_PER_SUPERCLASS: {
						this.createVersionedSetWithSetPerSuperclassStrategy(createdVersionedSets, fieldUuidsPairsBasedOnClassType,
							modelObject);
					}
					case SET_PER_OBJECT_TYPE: {
						this.createVersionedSetWithSetPerObjectTypeStrategy(createdVersionedSets, fieldUuidsPairsBasedOnClassType,
							modelObject);
					}
						break;
					// Use SET_PER_CLASS by default
					default: {
						this.createVersionedSetWithSetPerClassStrategy(createdVersionedSets, fieldUuidsPairsBasedOnClassType,
							modelObject);
					}
				} // end of switch
			}
			// Let's add internal objects of the current object
			final Collection<Object> internalObjects =
				ReflectionUtil.getInstance().getVersionedValuesOfVersionedObject(modelObject);
			for (final Object internalObject : internalObjects) {
				final boolean objectIsInStack = stackOfModelObjects.containsValue(internalObject);
				if (!objectIsInStack) {
					stackOfModelObjects.put(Boolean.FALSE, internalObject);
				}
			}
		}
		return Sets.newHashSet(createdVersionedSets.values());
	}
	
	/**
	 * @param versionedSets
	 * @param fieldUuidsPairsBasedOnClassType
	 * @param modelObject
	 */
	private void createVersionedSetWithSetPerSuperclassStrategy(final Map<VersionedSetType, VersionedSet> versionedSets,
		final Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType, final Object modelObject) {
		
		final Class<?> classOfModelObject = modelObject.getClass();
		final String versionedSetName = ReflectionUtil.getInstance().getNameOfSuperclass(modelObject);
		final VersionedSetType versionedSetTypeForModelObject = new VersionedSetType(modelObject.getClass());
		
		// Determine whether VersionedSet already exist or not
		if (versionedSets.containsKey(versionedSetTypeForModelObject)) {
			try {
				versionedSets.get(versionedSetTypeForModelObject).addVersionedObject(modelObject);
			}
			catch (final Exception e) {
				Converter.logger.error(e.getMessage());
			}
		}
		// If versioned set hasn't been created yet
		else {
			// Find field->uuids pairs for this newly created versioned set
			Multimap<Field, String> fieldUuidsPairs = null;
			if (fieldUuidsPairsBasedOnClassType != null) {
				fieldUuidsPairs = fieldUuidsPairsBasedOnClassType.get(classOfModelObject);
			}
			
			final VersionedSet newlyCreatedVersionedSet =
				this.versionedSetFactory(versionedSetName, SetStrategy.SET_PER_SUPERCLASS, modelObject, fieldUuidsPairs,
					versionedSetTypeForModelObject);
			versionedSets.put(newlyCreatedVersionedSet.getType(), newlyCreatedVersionedSet);
		}
	}
	
	/**
	 * @param versionedSets
	 * @param fieldUuidsPairsBasedOnClassType
	 * @param modelObject
	 */
	private void createVersionedSetWithSetPerConrainerStrategy(final Map<VersionedSetType, VersionedSet> versionedSets,
		final Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType, final Object modelObject) {
		
		final Class<?> classOfModelObject = modelObject.getClass();
		final VersionedSetType versionedSetTypeForModelObject = new VersionedSetType(classOfModelObject);
		final String versionedSetName = classOfModelObject.getAnnotation(VersionedEntity.class).name();
		
		// If versioned set has been already created
		if (versionedSets.containsKey(versionedSetTypeForModelObject)) {
			try {
				versionedSets.get(versionedSetTypeForModelObject).addVersionedObject(modelObject);
			}
			catch (final Exception e) {
				Converter.logger.error(e.getMessage());
			}
		}
		// If versioned set hasn't been created yet
		else {
			// Find field->uuids pairs for this newly created versioned set
			Multimap<Field, String> fieldUuidsPairs = null;
			if (fieldUuidsPairsBasedOnClassType != null) {
				fieldUuidsPairs = fieldUuidsPairsBasedOnClassType.get(classOfModelObject);
			}
			
			final VersionedSet newlyCreatedVersionedSet =
				this.versionedSetFactory(versionedSetName, SetStrategy.SET_PER_CONTAINER, modelObject, fieldUuidsPairs,
					versionedSetTypeForModelObject);
			versionedSets.put(newlyCreatedVersionedSet.getType(), newlyCreatedVersionedSet);
		}
	}
	
	/**
	 * @param versionedSets
	 * @param fieldUuidsPairsBasedOnClassType
	 * @param modelObject
	 */
	private void createVersionedSetWithSetPerClassStrategy(final Map<VersionedSetType, VersionedSet> versionedSets,
		final Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType, final Object modelObject) {
		
		final Class<?> classOfModelObject = modelObject.getClass();
		final VersionedSetType versionedSetTypeForModelObject = new VersionedSetType(classOfModelObject);
		
		// Determine whether VersionedSet already exist or not
		if (versionedSets.containsKey(versionedSetTypeForModelObject)) {
			try {
				versionedSets.get(versionedSetTypeForModelObject).addVersionedObject(modelObject);
			}
			catch (final Exception e) {
				Converter.logger.error(e.getMessage());
			}
		}
		else {
			// If not exist, create a new one if versioned set wasn't created yet
			final String versionedSetName = classOfModelObject.getAnnotation(VersionedEntity.class).name();
			
			// Find field->uuids pairs for this newly created versioned set
			Multimap<Field, String> fieldUuidsPairs = null;
			if (fieldUuidsPairsBasedOnClassType != null) {
				fieldUuidsPairs = fieldUuidsPairsBasedOnClassType.get(classOfModelObject);
			}
			
			final VersionedSet newlyCreatedVersionedSet =
				this.versionedSetFactory(versionedSetName, SetStrategy.SET_PER_CLASS, modelObject, fieldUuidsPairs,
					versionedSetTypeForModelObject);
			// Add newly created versioned set to the set of newly created
			// versioned sets
			versionedSets.put(newlyCreatedVersionedSet.getType(), newlyCreatedVersionedSet);
		}
	}
	
	private void createVersionedSetWithSetPerObjectTypeStrategy(final Map<VersionedSetType, VersionedSet> versionedSets,
		final Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType, final Object modelObject) {
		
		final Class<?> classOfModelObject = modelObject.getClass();
		VersionedSetType versionedSetType = null;
		try {
			final Class<?> versionedSetSubType = ReflectionUtil.getInstance().getSubType(modelObject);
			versionedSetType = new VersionedSetType(classOfModelObject, versionedSetSubType);
		}
		catch (final SubTypeCanNotBeFoundException e1) {
			e1.printStackTrace();
		}
		
		// Determine whether VersionedSet already exist or not
		if (versionedSets.containsKey(versionedSetType)) {
			try {
				versionedSets.get(versionedSetType).addVersionedObject(modelObject);
			}
			catch (final Exception e) {
				Converter.logger.error(e.getMessage());
			}
		}
		else {
			// If not exist, create a new one if versioned set wasn't created yet
			String versionedSetName = classOfModelObject.getAnnotation(VersionedEntity.class).name();
			// Add sub type of object must be added to the name
			if ((versionedSetType.getSubType() != null)
					&& versionedSetType.getSubType().isAnnotationPresent(VersionedEntity.class)) {
				versionedSetName += " - " + versionedSetType.getSubType().getAnnotation(VersionedEntity.class).name();
			}
			
			// Find field->uuids pairs for this newly created versioned set
			Multimap<Field, String> fieldUuidsPairs = null;
			if (fieldUuidsPairsBasedOnClassType != null) {
				fieldUuidsPairs = fieldUuidsPairsBasedOnClassType.get(classOfModelObject);
			}
			
			final VersionedSet newlyCreatedVersionedSet =
				this.versionedSetFactory(versionedSetName, SetStrategy.SET_PER_OBJECT_TYPE, modelObject, fieldUuidsPairs,
					versionedSetType);
			// Add newly created versioned set to the set of newly created versioned
			// sets
			versionedSets.put(newlyCreatedVersionedSet.getType(), newlyCreatedVersionedSet);
		}
		
	}
	
	private Set<VersionedSet> specifyBindings(final Set<VersionedSet> newlyCreatedSetOfVersionedSets) {
		Preconditions.checkNotNull(newlyCreatedSetOfVersionedSets, "Given model is null!");
		Preconditions.checkArgument(!newlyCreatedSetOfVersionedSets.isEmpty(), "Given model is empty!");
		for (final VersionedSet owner : newlyCreatedSetOfVersionedSets) {
			for (final VersionedSet subordinate : newlyCreatedSetOfVersionedSets) {
				if (!owner.equals(subordinate)) {
					owner.addPredecessorBinding(subordinate);
				}
			}
		}
		return newlyCreatedSetOfVersionedSets;
	}
	
	private VersionedSet versionedSetFactory(final String versionedSetName, final SetStrategy versionedSetStrategy,
		final Object modelObject, final Multimap<Field, String> fieldUuidsPairs,
		final VersionedSetType versionedSetTypeForModelObject) {
		final VersionedSet newVersionedSet = new VersionedSet();
		final boolean isDefaultNameWasSpecified = versionedSetName.equals("Default");
		if (isDefaultNameWasSpecified) {
			newVersionedSet.setName(StringUtil.getInstance().specifyVersionedSetName(modelObject));
		}
		else {
			newVersionedSet.setName(StringUtil.getInstance().specifyVersionedSetName(versionedSetName));
		}
		newVersionedSet.setStrategy(versionedSetStrategy);
		newVersionedSet.setType(versionedSetTypeForModelObject);
		try {
			newVersionedSet.addVersionedObject(modelObject);
			// By default the versioned set refers to itself
			newVersionedSet.addPredecessorVersioning(newVersionedSet);
		}
		catch (final Exception e) {
			Converter.logger.error(e.getMessage());
		}
		final boolean isVisible = modelObject.getClass().getAnnotation(VersionedEntity.class).visible();
		if (isVisible) {
			newVersionedSet.setVisible(true);
		}
		// Set field->uuids pairs to this versioned set
		if (fieldUuidsPairs != null) {
			for (final Entry<Field, String> fieldUuidsPair : fieldUuidsPairs.entries()) {
				newVersionedSet.addFieldUuidPair(fieldUuidsPair.getKey(), fieldUuidsPair.getValue());
			}
		}
		return newVersionedSet;
	}
}
