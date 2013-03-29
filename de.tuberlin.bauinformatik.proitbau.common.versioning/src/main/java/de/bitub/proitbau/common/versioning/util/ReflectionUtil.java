/*******************************************************************************
 * Author:		"Vladislav Fedotov"
 * Written:		2013
 * Project:		Setvers
 * E-mail:		vladislav.fedotov@tu-berlin.de
 * Company:		TU Berlin
 * Version:		1.0
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladislav Fedotov - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.versioning.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.annotations.Comparable;
import de.bitub.proitbau.common.versioning.annotations.DomainModel;
import de.bitub.proitbau.common.versioning.annotations.Id;
import de.bitub.proitbau.common.versioning.annotations.Ignore;
import de.bitub.proitbau.common.versioning.annotations.SuperclassSet;
import de.bitub.proitbau.common.versioning.annotations.TypeDivisor;
import de.bitub.proitbau.common.versioning.annotations.VersionedEntity;
import de.bitub.proitbau.common.versioning.model.ModelCache;
import de.bitub.proitbau.common.versioning.model.Versionable;

public class ReflectionUtil {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(ReflectionUtil.class);
	{
		ReflectionUtil.logger.setLevel(Level.INFO);
	}
	private static final int UUID_LENGTH = 36;
	
	private ReflectionUtil() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static ReflectionUtil instance = new ReflectionUtil();
	}
	
	@SuppressWarnings("synthetic-access")
	public static ReflectionUtil getInstance() {
		return Handler.instance;
	}
	
	public Collection<Field> getComparableFields(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Collection<Field> comparableFields = Lists.newArrayList();
		Class<?> cls = object.getClass();
		// // @formatter:off
		// Ask cache for this field
		// if the field is in cache then check  
		// 		add comparable fields from cache to the returned collection
		// else
		// 		while classes available (after current class we will read super class of this class, then the super class of the super class etc.)
		//				access the fields of the object
		//				go through all the fields
		//						if the field is non static
		//								if the field was not annotated with the @Ignore annotation
		//										if the field or getter was annotated with @Comparable annotation
		//												add field to the returned collection 
		//				get the super class of the current class
		//		put the returned collection to the cache
		// // @formatter:on
		final boolean isInCache = ModelCache.getInstance().containsComparableField(cls);
		if (isInCache) {
			comparableFields = ModelCache.getInstance().getComparableFields(cls);
		}
		else {
			while (cls != null) {
				final Field[] fieldsOfObject = cls.getDeclaredFields();
				AccessibleObject.setAccessible(fieldsOfObject, true);
				for (final Field fieldOfObject : fieldsOfObject) {
					// Eliminate static fields
					if (this.isFieldComparable(fieldOfObject, object)) {
						comparableFields.add(fieldOfObject);
					}
				}
				cls = cls.getSuperclass();
			}
			ModelCache.getInstance().addComarableFields(object.getClass(), comparableFields);
		}
		return comparableFields;
	}
	
	private boolean isFieldComparable(final Field fieldOfObject, final Object object) {
		boolean isFieldComparable = false;
		final boolean isStatic = Modifier.isStatic(fieldOfObject.getModifiers());
		// @formatter:off
		if (
			!isStatic 
			&& 
			!fieldOfObject.isAnnotationPresent(Ignore.class) 
			&& 
			(fieldOfObject.isAnnotationPresent(Comparable.class)
			|| 
			this.isGetterAnnotatedWith(fieldOfObject, object.getClass(), Comparable.class))
		// @formatter:on
		) {
			isFieldComparable = true;
		}
		return isFieldComparable;
	}
	
	public Collection<Field> getFields(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		final Collection<Field> fields = Lists.newArrayList();
		Class<?> cls = object.getClass();
		final boolean isInCache = ModelCache.getInstance().containsNonStaticField(cls);
		if (isInCache) {
			fields.addAll(ModelCache.getInstance().getNonStaticFields(cls));
		}
		else {
			while (cls != null) {
				final Field[] fieldsOfObject = cls.getDeclaredFields();
				AccessibleObject.setAccessible(fieldsOfObject, true);
				for (final Field fieldOfObject : fieldsOfObject) {
					final boolean isStatic = Modifier.isStatic(fieldOfObject.getModifiers());
					if (!isStatic) {
						fields.add(fieldOfObject);
					}
				}
				cls = cls.getSuperclass();
			}
			ModelCache.getInstance().addNonStaticFields(object.getClass(), fields);
		}
		return fields;
	}
	
	/*
	 * Takes object and a specific annotation, method returns this annotations if
	 * it have been founded otherwise method returns null. Method checks all
	 * Superclass's for the annotation.
	 */
	public Annotation getAnnotation(final Object object, final Class<? extends Annotation> annotation) {
		Preconditions.checkNotNull(object, "Given object is null!");
		// TODO Add caching
		Annotation foundAnnotation = null;
		Class<?> cls = object.getClass();
		while ((cls != null) && (foundAnnotation == null)) {
			if (cls.isAnnotationPresent(annotation)) {
				foundAnnotation = cls.getAnnotation(annotation);
			}
			cls = cls.getSuperclass();
		}
		return foundAnnotation;
	}
	
	public String getNameOfField(final Class<?> cls, final Field field) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		Preconditions.checkNotNull(field, "Given field is null!");
		String fieldName = "";
		try {
			final boolean isInCache = ModelCache.getInstance().containsFieldName(field);
			if (isInCache) {
				fieldName = ModelCache.getInstance().getFieldName(field);
			}
			else {
				final Method method = new PropertyDescriptor(field.getName(), cls).getReadMethod();
				if (field.isAnnotationPresent(Comparable.class)) {
					fieldName = field.getAnnotation(Comparable.class).name();
				}
				else if (method.isAnnotationPresent(Comparable.class)) {
					fieldName = method.getAnnotation(Comparable.class).name();
				}
				ModelCache.getInstance().addFieldName(field, fieldName);
			}
		}
		catch (final IntrospectionException e) {
			ReflectionUtil.logger.info(cls.toString());
			ReflectionUtil.logger.error(e.getMessage());
		}
		return fieldName;
	}
	
	public String getNameOfSuperclass(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		String superclassName = "";
		Class<?> cls = object.getClass();
		final boolean isInCache = ModelCache.getInstance().containsSuperclassName(cls);
		if (isInCache) {
			superclassName = ModelCache.getInstance().getSuperclassName(cls);
		}
		else {
			while (cls != null) {
				final boolean isAnnotationSupeclassSetPresentedAboveClass = cls.isAnnotationPresent(SuperclassSet.class);
				if (isAnnotationSupeclassSetPresentedAboveClass) {
					superclassName = cls.getAnnotation(SuperclassSet.class).name();
				}
				cls = cls.getSuperclass();
			}
			ModelCache.getInstance().addSuperclassName(object.getClass(), superclassName);
		}
		return superclassName;
	}
	
	public String getNameOfVersionedEntity(final Class<?> cls) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		String versionedEntityName = "";
		final boolean isInCache = ModelCache.getInstance().containsVersionedEntityName(cls);
		if (isInCache) {
			versionedEntityName = ModelCache.getInstance().getVersionedEntityName(cls);
		}
		else {
			final boolean isAnnotationVersionedEntityPresented = cls.isAnnotationPresent(VersionedEntity.class);
			if (isAnnotationVersionedEntityPresented) {
				versionedEntityName = cls.getAnnotation(VersionedEntity.class).name();
			}
			else {
				versionedEntityName = cls.getCanonicalName();
			}
			ModelCache.getInstance().addVersionedEntityName(cls, versionedEntityName);
		}
		return versionedEntityName;
	}
	
	public int getOrderIndexOfField(final Class<?> cls, final Field field) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		Preconditions.checkNotNull(field, "Given field is null!");
		int orderIndex = 0;
		final boolean isInCache = ModelCache.getInstance().containsOrderIndexOfField(field);
		if (isInCache) {
			orderIndex = ModelCache.getInstance().getOrderIndexOfField(field);
		}
		else {
			if (field.isAnnotationPresent(Comparable.class)) {
				orderIndex = field.getAnnotation(Comparable.class).orderIndex();
			}
			else if (this.isGetterAnnotatedWith(field, cls, Comparable.class)) {
				try {
					orderIndex =
						new PropertyDescriptor(field.getName(), cls).getReadMethod().getAnnotation(Comparable.class).orderIndex();
				}
				catch (final IntrospectionException e) {
					ReflectionUtil.logger.info(cls.toString());
					ReflectionUtil.logger.error(e.getMessage());
				}
			}
			ModelCache.getInstance().addOrderIndexOfField(field, orderIndex);
		}
		return orderIndex;
	}
	
	public String getUuidOfObject(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		String uuid = "";
		final boolean isInCache = ModelCache.getInstance().containsObjectUuid(object);
		if (isInCache) {
			uuid = ModelCache.getInstance().getObjectUuid(object).toString();
		}
		else {
			Class<? extends Object> cls = object.getClass();
			
			// First of all try to find field with the @Id annotation
			while ((cls != null) && (uuid.isEmpty())) {
				final Field[] fieldsOfObject = cls.getDeclaredFields();
				AccessibleObject.setAccessible(fieldsOfObject, true);
				for (int i = 0; (i < fieldsOfObject.length) && (uuid.isEmpty()); i++) {
					final boolean isIdAnnotationPresentedAboveField = fieldsOfObject[i].isAnnotationPresent(Id.class);
					if (isIdAnnotationPresentedAboveField) {
						try {
							uuid = (String) fieldsOfObject[i].get(object);
						}
						catch (final IllegalArgumentException e) {
							ReflectionUtil.logger.error(e.getMessage());
						}
						catch (final IllegalAccessException e) {
							ReflectionUtil.logger.error(e.getMessage());
						}
						Preconditions.checkArgument(!uuid.isEmpty(), "This object has empty UUID!");
					}
				}
				cls = cls.getSuperclass();
			}
			// Then try to find the method with the @Id annotation
			cls = object.getClass();
			while ((cls != null) && (uuid.isEmpty())) {
				final Method[] methodsOfObject = cls.getDeclaredMethods();
				AccessibleObject.setAccessible(methodsOfObject, true);
				for (int i = 0; (i < methodsOfObject.length) && (uuid.isEmpty()); i++) {
					final boolean isIdAnnotationPresentedAboveGetter = methodsOfObject[i].isAnnotationPresent(Id.class);
					if (isIdAnnotationPresentedAboveGetter) {
						try {
							uuid = (String) methodsOfObject[i].invoke(object);
						}
						catch (final IllegalAccessException e) {
							ReflectionUtil.logger.error(e.getMessage());
						}
						catch (final IllegalArgumentException e) {
							ReflectionUtil.logger.error(e.getMessage());
						}
						catch (final InvocationTargetException e) {
							ReflectionUtil.logger.error(e.getMessage());
						}
						Preconditions.checkArgument(!uuid.isEmpty(), "This object has empty UUID!");
					}
				}
				cls = cls.getSuperclass();
			}
			Preconditions.checkArgument(uuid.length() == ReflectionUtil.UUID_LENGTH, "UUID is incorrect!");
			
			ModelCache.getInstance().addObjectUuid(object, UUID.fromString(uuid));
		}
		
		return uuid;
	}
	
	public Object getValueOfField(final Object object, final Field field) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Object value = null;
		try {
			value = field.get(object);
		}
		catch (final IllegalAccessException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		catch (final SecurityException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		return value;
	}
	
	// @formatter:off
	/*
	 * Returns a map of class type associated with a multimap,
	 * where the keys are fields of the given object and values are uuids, 
	 * the result be look like such a sequence:
	 * class1--->field1->uuid1
	 *       \         \->uuid2
	 *        |->field2->uuid3
	 *                 \->uuid4   
	 * class2--->field1->uuid5
	 *       \         \->uuid6
	 *        |->field3->uuid7
	 *                 \->uuid8   
	 * @param object must be a model object, the class of this object must be annotated with @DOmainModel annotation
	 * @return the binding graph loaded from the database
	 */
	// @formatter:on   
	public Map<Class<?>, Multimap<Field, String>> getFieldUuidsPairsBasedOnClassType(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Preconditions.checkArgument(object.getClass().isAnnotationPresent(DomainModel.class),
			"@DomainModel annotation isn't presented in the class of the given object!");
		final Set<Field> fields = Sets.newHashSet(this.getFields(object));
		final Map<Class<?>, Multimap<Field, String>> fieldUuidsPairsBasedOnClassType = Maps.newHashMapWithExpectedSize(30);
		for (final Field fieldOfObject : fields) {
			final Set<Object> versionedEntityValues = this.obtainVersionedEntityValues(object, fieldOfObject);
			for (final Object versionedEntityValue : versionedEntityValues) {
				final String uuid = this.getUuidOfObject(versionedEntityValue);
				final Class<?> classType = versionedEntityValue.getClass();
				// If the entry of the specific class type was already added to the
				// multimap
				if (fieldUuidsPairsBasedOnClassType.containsKey(versionedEntityValue.getClass())) {
					// If the field key already exists, assign uuid to this field
					if (fieldUuidsPairsBasedOnClassType.get(versionedEntityValue.getClass()).containsKey(fieldOfObject)) {
						fieldUuidsPairsBasedOnClassType.get(versionedEntityValue.getClass()).get(fieldOfObject).add(uuid);
					}
					// The field doesn't exist, add new field-> uuid pair to the map
					else {
						fieldUuidsPairsBasedOnClassType.get(versionedEntityValue.getClass()).put(fieldOfObject, uuid);
					}
				}
				// Create new multimap, create new field->uuid pair and assign it to the
				// specific class type
				else {
					final Multimap<Field, String> fieldUuidsPairs = HashMultimap.create();
					fieldUuidsPairs.put(fieldOfObject, uuid);
					fieldUuidsPairsBasedOnClassType.put(classType, fieldUuidsPairs);
				}
			}
		}
		return fieldUuidsPairsBasedOnClassType;
	}
	
	// @formatter:off
	// Return set of objects which are in the given object
	// Value (or object) of the field should be not @Transient, @TransientBinding, null and should be @VersionedEntity
	// Collection types unwrapped and discovered afterwards
	// @formatter:on
	public Set<Object> getVersionedValuesOfVersionedObject(final Object object) {
		Preconditions.checkNotNull(object, "Given object is null!");
		Set<Object> valuesOfObject = null;
		// If values of object have already cached, return; otherwise create, cache
		// and return
		try {
			valuesOfObject = ModelCache.getInstance().getObjectValues().get(object, new Callable<Set<Object>>() {
				
				@Override
				public Set<Object> call() throws Exception {
					return ReflectionUtil.this.findValuesOfVersionedEntity(object);
				}
			});
		}
		catch (final ExecutionException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		return valuesOfObject;
	}
	
	public boolean isFiledVisible(final Class<?> cls, final Field field) {
		boolean visible = false;
		final boolean isInCache = ModelCache.getInstance().containsVisibleField(field);
		if (isInCache) {
			visible = ModelCache.getInstance().isFieldVisible(field);
		}
		else {
			if (field.isAnnotationPresent(Comparable.class)) {
				visible = field.getAnnotation(Comparable.class).visible();
			}
			else if (this.isGetterAnnotatedWith(field, cls, Comparable.class)) {
				try {
					visible =
						new PropertyDescriptor(field.getName(), cls).getReadMethod().getAnnotation(Comparable.class).visible();
				}
				catch (final IntrospectionException e) {
					ReflectionUtil.logger.info(cls.toString());
					ReflectionUtil.logger.error(e.getMessage());
				}
			}
			ModelCache.getInstance().addVisibilityOfField(field, visible);
		}
		return visible;
	}
	
	public boolean isGetterAnnotatedWith(final Field field, final Class<?> cls,
		final Class<? extends Annotation> annotation) {
		boolean isAnnotated = false;
		try {
			final Method method = new PropertyDescriptor(field.getName(), cls).getReadMethod();
			final boolean isAnnotationPresentedAboveGetter = method.isAnnotationPresent(annotation);
			if (isAnnotationPresentedAboveGetter) {
				isAnnotated = true;
			}
		}
		catch (final IntrospectionException e) {
			ReflectionUtil.logger.info(cls.toString());
			ReflectionUtil.logger.error(e.getMessage());
		}
		return isAnnotated;
	}
	
	// Checks if the class of the given object implements Versionable interface
	// and annotated with @VersionedEntity annotation
	private boolean isVersionedEntity(final Object value) {
		if (value.getClass().isAnnotationPresent(VersionedEntity.class)
				&& Versionable.class.isAssignableFrom(value.getClass())) {
			return true;
		}
		return false;
	}
	
	Set<Object> findValuesOfVersionedEntity(final Object object) {
		final Set<Object> valuesOfObject = new HashSet<Object>();
		final Class<?> cls = object.getClass();
		final boolean isInCache = ModelCache.getInstance().containsNonStaticField(cls);
		if (isInCache) {
			final Set<Field> fields = (Set<Field>) ModelCache.getInstance().getNonStaticFields(cls);
			for (final Field fieldOfObject : fields) {
				valuesOfObject.addAll(this.obtainVersionedEntityValues(object, fieldOfObject));
			}
		}
		else {
			final Set<Field> fields = Sets.newHashSet(this.getFields(object));
			for (final Field fieldOfObject : fields) {
				valuesOfObject.addAll(this.obtainVersionedEntityValues(object, fieldOfObject));
				
			}
			ModelCache.getInstance().addNonStaticFields(object.getClass(), fields);
		}
		return valuesOfObject;
	}
	
	private Set<Object> obtainVersionedEntityValues(final Object object, final Field fieldOfObject) {
		final Set<Object> valuesOfObject = new HashSet<Object>();
		try {
			// @formatter:off
			// TODO
			// Find out, is this field has a @TransientBinding annotation or @Transient annotation
			// Do we rally need cache for this?
			// @formatter:on
			boolean notIgnored = true;
			// Tries to get status from the cache
			final boolean existsInCache = ModelCache.getInstance().containsTransientField(fieldOfObject);
			if (existsInCache) {
				// Get status from cache
				notIgnored = ModelCache.getInstance().isFieldTransient(fieldOfObject);
			}
			else {
				// @formatter:off
				if (
					fieldOfObject.isAnnotationPresent(Ignore.class)
					|| 
					this.isGetterAnnotatedWith(fieldOfObject, object.getClass(), Ignore.class)
				) {
				// @formatter:on
					notIgnored = false;
				}
				// Add result to the cache
				ModelCache.getInstance().addTransientBindingField(fieldOfObject, Boolean.valueOf(notIgnored));
			}
			
			// Get value of the current field
			final Object value = fieldOfObject.get(object);
			
			if (notIgnored && (value != null)) {
				// Required conditions
				final boolean isVersionedEntity = this.isVersionedEntity(value);
				final boolean isCollection = value instanceof Collection;
				final boolean isMap = value instanceof Map;
				final boolean isArray = value instanceof Object[];
				
				if (isVersionedEntity) {
					valuesOfObject.add(value);
				}
				// If value is:
				// - a Collection
				// - not empty
				// - first element of the collection is annotated with VersionedEntity
				// annotation
				// TODO if the collection contains elements of different types and some
				// of them don't have VersionedEntity annotation
				// then this statement won't work
				
				// @formatter:off
				else if ( isCollection && 
									!((Collection<?>) value).isEmpty() && 
									((Collection<?>) value).iterator().next().getClass().isAnnotationPresent(VersionedEntity.class)
				) {
					for (final Object elementOfCollection : (Collection<?>) value) {
						valuesOfObject.add(elementOfCollection);
					}
				}
				// @formatter:on
				
				// If value is:
				// - a Map
				// - not empty
				// - first value of the map is annotated with VersionedEntity annotation
				// TODO if the map contains values of different types and some of them
				// don't have VersionedEntity annotation
				// then this statement won't work
				// This also won't work if the map' keys will be annotated with
				// VersionedEntity annotation and not the values
				
				// @formatter:off
				else if ( isMap && 
									!((Map<?, ?>) value).isEmpty() && 
									((Map<?, ?>) value).values().iterator().next().getClass().isAnnotationPresent(VersionedEntity.class)
				) {
				// @formatter:on
					for (final Object elementOfMap : ((Map<?, ?>) value).values()) {
						valuesOfObject.add(elementOfMap);
					}
				}
				
				// If value is:
				// - an Array
				// - not empty
				// - first element of the array is annotated with VersionedEntity
				// annotation
				// TODO if the array contains values of different types and some of them
				// don't have VersionedEntity annotation
				// then this statement won't work
				
				// @formatter:off
				else if ( isArray && 
									(((Object[]) value).length > 0) && 
									((Object[]) value)[0].getClass().isAnnotationPresent(VersionedEntity.class)
				) {
				// @formatter:on
					for (final Object elementOfArray : (Object[]) value) {
						valuesOfObject.add(elementOfArray);
					}
				}
			}
		}
		catch (final IllegalAccessException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		catch (final SecurityException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		
		return valuesOfObject;
	}
	
	@SuppressWarnings({
		"null",
		"unchecked"
	})
	private Set<Object>
		performMerge(final Object leadingObject, final Object nonleadingObject, final Field fieldOfObject) {
		final Set<Object> valuesOfObject = new HashSet<Object>();
		try {
			// TODO
			// Find out, is this field has a @TransientBinding annotation or
			// @Transient annotation
			// Do we rally need cache for this?
			boolean notIgnored = true;
			// Tries to get status from the cache
			final boolean existsInCache = ModelCache.getInstance().containsTransientField(fieldOfObject);
			if (existsInCache) {
				// Get status from cache
				notIgnored = ModelCache.getInstance().isFieldTransient(fieldOfObject);
			}
			else {
				// @formatter:off
				if (
					fieldOfObject.isAnnotationPresent(Ignore.class) || 
					this.isGetterAnnotatedWith(fieldOfObject, leadingObject.getClass(), Ignore.class)
				) {
				// @formatter:on
					notIgnored = false;
				}
				// Add result to the cache
				ModelCache.getInstance().addTransientBindingField(fieldOfObject, Boolean.valueOf(notIgnored));
			}
			
			// Get value of the current field
			Object valueFromLeadingObject = fieldOfObject.get(leadingObject);
			final Object valueFromNonleadingObject = fieldOfObject.get(nonleadingObject);
			
			if (notIgnored && (valueFromNonleadingObject != null)) {
				
				// Required conditions
				final boolean isCollection = valueFromNonleadingObject instanceof Collection;
				final boolean isMap = valueFromNonleadingObject instanceof Map;
				final boolean isArray = fieldOfObject.getType().isArray();
				
				// If value is:
				// - a Collection
				// - not empty
				// - first element of the collection is annotated with VersionedEntity
				// annotation
				// TODO if the collection contains elements of different types
				// and some of them don't have VersionedEntity annotation
				// then this statement won't work
				// @formatter:off
				if ( isCollection && 
						 !((Collection<?>) valueFromNonleadingObject).isEmpty() && 
						 ((Collection<?>) valueFromNonleadingObject).iterator().next().getClass().isAnnotationPresent(VersionedEntity.class)
				) {
				// @formatter:on
					// If property of leading object is null, this means we can't
					// put any values from another object into it, so we have to
					// initialize it
					if (valueFromLeadingObject == null) {
						valueFromLeadingObject = leadingObject.getClass().newInstance();
					}
					for (final Object elementOfCoLlection : (Collection<?>) valueFromNonleadingObject) {
						if (!((Collection<Object>) valueFromLeadingObject).contains(elementOfCoLlection)) {
							((Collection<Object>) valueFromLeadingObject).add(elementOfCoLlection);
						}
					}
				}
				
				// If value is:
				// - a Map
				// - not empty
				// - first value of the map is annotated with VersionedEntity annotation
				// TODO if the map contains values of different types and some
				// of them don't have VersionedEntity annotation
				// then this statement won't work
				// This also won't work if the map' keys will be annotated with
				// VersionedEntity annotation and not the values
				// @formatter:off
				else if ( isMap && 
								  !((Map<?, ?>) valueFromNonleadingObject).isEmpty() && 
								  ((Map<?, ?>) valueFromNonleadingObject).values().iterator().next().getClass().isAnnotationPresent(VersionedEntity.class)
				) {
				// @formatter:on
					if (valueFromLeadingObject == null) {
						// If property of leading object is null, this means we
						// can't
						// put any values from another object into it, so we have to
						// initialize it
						valueFromLeadingObject = leadingObject.getClass().newInstance();
					}
					for (final Entry<?, ?> entry : ((Map<?, ?>) valueFromNonleadingObject).entrySet()) {
						if (!((Map<Object, Object>) valueFromLeadingObject).values().contains(entry.getValue())) {
							((Map<Object, Object>) valueFromLeadingObject).put(entry.getKey(), entry.getValue());
						}
					}
				}
				
				// If value is:
				// - an Array
				// - not empty
				// - first element of the array is annotated with VersionedEntity
				// annotation
				// TODO if the array contains values of different types and some
				// of them don't have VersionedEntity annotation
				// then this statement won't work
				
				// @formatter:off
				else if ( isArray && 
								  (((Object[]) valueFromNonleadingObject).length > 0) && 
								  ((Object[]) valueFromNonleadingObject)[0].getClass().isAnnotationPresent(VersionedEntity.class)
				) {
				// @formatter:on
					
					// If property of leading object is null, this means we can't
					// put any values from another object into it, so we have to
					// initialize it
					if (valueFromLeadingObject == null) {
						valueFromLeadingObject = leadingObject.getClass().newInstance();
					}
					
					// Find the objects which are not presented in the leading object
					// We will put these object in a specific array -
					// arrayWithObjectWhichHaveToBeAdded
					// We will also count the number of such an objects -
					// numberOfElementsWhichShouldBeAdded
					final Object[] arrayWithObjectWhichHaveToBeAdded = new Object[Array.getLength(valueFromNonleadingObject)];
					int numberOfElementsWhichShouldBeAdded = 0;
					for (int i = 0; i < Array.getLength(valueFromLeadingObject); i++) {
						if (!Arrays.asList(((Object[]) valueFromLeadingObject)).contains(((Object[]) valueFromNonleadingObject)[i])) {
							arrayWithObjectWhichHaveToBeAdded[numberOfElementsWhichShouldBeAdded] =
								((Object[]) valueFromNonleadingObject)[i];
							numberOfElementsWhichShouldBeAdded++;
						}
					}
					
					// Calculate a new size for an array which will contain objects from
					// both objects and create it
					final Object newArray =
						Array.newInstance(fieldOfObject.getType().getComponentType(), Array.getLength(valueFromLeadingObject)
																																					+ numberOfElementsWhichShouldBeAdded);
					
					// Put elements of both arrays into one array
					System.arraycopy(arrayWithObjectWhichHaveToBeAdded, 0, newArray, 0, numberOfElementsWhichShouldBeAdded);
					System.arraycopy(valueFromLeadingObject, 0, newArray, numberOfElementsWhichShouldBeAdded,
						Array.getLength(valueFromLeadingObject));
					
					// Set the new value with merged array to the leading object
					fieldOfObject.set(leadingObject, newArray);
				}
			}
		}
		catch (final IllegalAccessException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		catch (final SecurityException e) {
			ReflectionUtil.logger.error(e.getMessage());
		}
		catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valuesOfObject;
	}
	
	public void mergeObjects(final Object leadingObject, final Object nonleadingObject) {
		Preconditions.checkNotNull(leadingObject, "Leading object is null!");
		Preconditions.checkNotNull(nonleadingObject, "Leading object is null!");
		Preconditions.checkArgument(leadingObject.getClass().equals(nonleadingObject.getClass()),
			"Class type of the leading object doen't coincide with the class type of nonleading object!");
		Preconditions.checkArgument(leadingObject.getClass().isAnnotationPresent(VersionedEntity.class),
			"The leading object doesn't annotated with @VersionedEntity annotation!");
		Preconditions.checkArgument(nonleadingObject.getClass().isAnnotationPresent(VersionedEntity.class),
			"The leading object doesn't annotated with @VersionedEntity annotation!");
		
		Class<?> cls = leadingObject.getClass();
		final boolean isInCache = ModelCache.getInstance().containsNonStaticField(cls);
		if (isInCache) {
			final Set<Field> fields = (Set<Field>) ModelCache.getInstance().getNonStaticFields(cls);
			for (final Field fieldOfObject : fields) {
				final boolean isStatic = Modifier.isStatic(fieldOfObject.getModifiers());
				if (!isStatic) {
					this.performMerge(leadingObject, nonleadingObject, fieldOfObject);
				}
			}
		}
		else {
			final Set<Field> fieldsWillBeAddedToCache = Sets.newHashSet();
			while (cls != null) {
				final Field[] fieldsOfObject = cls.getDeclaredFields();
				AccessibleObject.setAccessible(fieldsOfObject, true);
				for (final Field fieldOfObject : fieldsOfObject) {
					final boolean isStatic = Modifier.isStatic(fieldOfObject.getModifiers());
					if (!isStatic) {
						this.performMerge(leadingObject, nonleadingObject, fieldOfObject);
					}
				}
				cls = cls.getSuperclass();
			}
			ModelCache.getInstance().addNonStaticFields(leadingObject.getClass(), fieldsWillBeAddedToCache);
		}
	}
	
	public Field getFieldByName(final Class<?> cls, final String fieldName) throws SubTypeCanNotBeFoundException {
		if (cls.equals(Object.class)) {
			throw new SubTypeCanNotBeFoundException("Subtype can't be, the given field name wasn't found in the given class");
		}
		Field field = null;
		try {
			field = cls.getDeclaredField(fieldName);
		}
		catch (final SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final NoSuchFieldException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			field = this.getFieldByName(cls.getSuperclass(), fieldName);
		}
		return field;
	}
	
	// This method analyze all fields of a complete class hierarchy and it looks
	// for TypeDivisor annotation, if it finds such a field or its getter with
	// that annotation, method takes class type of an object which is placed in
	// this field and returns it.
	public Class<?> getSubType(final Object object) throws SubTypeCanNotBeFoundException {
		
		// TODO cache UUID -> SubType pair !!!
		Preconditions.checkNotNull(object, "Given object is null");
		
		Class<?> subType = null;
		boolean isSubTypeFound = false;
		
		final String[] divisorFieldNames = object.getClass().getAnnotation(VersionedEntity.class).divisorFieldNames();
		
		int divisorFieldNamesCounter = 0;
		while (!isSubTypeFound && (divisorFieldNamesCounter < divisorFieldNames.length)) {
			// TODO this must be cached
			final Field field = this.getFieldByName(object.getClass(), divisorFieldNames[divisorFieldNamesCounter]);
			
			if (!field.isAnnotationPresent(TypeDivisor.class)) {
				throw new SubTypeCanNotBeFoundException("Subtype can't be found, TypeDivisor annotation is missing");
			}
			
			field.setAccessible(true);
			final Object fieldValue = this.getValueOfField(object, field);
			
			if (fieldValue == null) {
				throw new SubTypeCanNotBeFoundException("Subtype can't be found for the field with the null value");
			}
			
			final Class<?> fieldValueClass = fieldValue.getClass();
			final boolean isPrimitive = fieldValueClass.isArray();
			final boolean isArray = fieldValueClass.isPrimitive();
			final boolean isCollection = Collection.class.isAssignableFrom(fieldValueClass);
			final boolean isMap = Map.class.isAssignableFrom(fieldValueClass);
			boolean isBasedOnInternalObjectField = false;
			
			if (field.getAnnotation(TypeDivisor.class).basedOnInternalObjectField()) {
				isBasedOnInternalObjectField = true;
			}
			
			if (isPrimitive) {
				throw new SubTypeCanNotBeFoundException("Subtype can't be found for the field of primitive type");
			}
			else if (isArray) {
				final boolean isEmpty = ((Object[]) fieldValue).length == 0;
				if (!isEmpty) {
					for (final Object arrayValue : (Object[]) fieldValue) {
						final boolean isNull = arrayValue == null;
						if (!isNull) {
							if (subType == null) {
								if (isBasedOnInternalObjectField) {
									subType = this.getSubType(arrayValue);
								}
								else {
									subType = arrayValue.getClass();
								}
							}
							else { // if (subType != null)
								if (isBasedOnInternalObjectField) {
									final boolean isCurrentObjectHasSameSubType = subType.equals(this.getSubType(arrayValue));
									if (!isCurrentObjectHasSameSubType) {
										throw new SubTypeCanNotBeFoundException(
											"Subtype can't be found for the array which contains different sub elements");
									}
								}
								else {
									final boolean isCurrentObjectHasSameType = subType.equals(arrayValue);
									if (!isCurrentObjectHasSameType) {
										throw new SubTypeCanNotBeFoundException(
											"Subtype can't be found for the array which contains different elements");
									}
								}
							}
						}
					}
					if (subType != null) {
						isSubTypeFound = true;
					}
				}
			}
			else if (isCollection) {
				final boolean isEmpty = ((Collection<?>) fieldValue).isEmpty();
				if (!isEmpty) {
					for (final Object collectionValue : (Collection<?>) fieldValue) {
						final boolean isNull = collectionValue == null;
						if (!isNull) {
							if (subType == null) {
								if (isBasedOnInternalObjectField) {
									subType = this.getSubType(collectionValue);
								}
								else {
									subType = collectionValue.getClass();
								}
							}
							else { // if (subType != null)
								if (isBasedOnInternalObjectField) {
									final boolean isCurrentObjectHasSameSubType = subType.equals(this.getSubType(collectionValue));
									if (!isCurrentObjectHasSameSubType) {
										throw new SubTypeCanNotBeFoundException(
											"Subtype can't be found for the collection which contains different sub elements");
									}
								}
								else {
									final boolean isCurrentObjectHasSameType = subType.equals(collectionValue);
									if (!isCurrentObjectHasSameType) {
										throw new SubTypeCanNotBeFoundException(
											"Subtype can't be found for the collection which contains different elements");
									}
								}
							}
						}
					}
					if (subType != null) {
						isSubTypeFound = true;
					}
				}
			}
			else if (isMap) {
				// TODO Must be implemented
			}
			else {
				if (isBasedOnInternalObjectField) {
					subType = this.getSubType(fieldValue);
					final boolean isNull = subType == null;
					if (!isNull) {
						isSubTypeFound = true;
					}
				}
				else {
					subType = fieldValue.getClass();
					isSubTypeFound = true;
				}
			}
			
			divisorFieldNamesCounter++;
		}
		
		if (isSubTypeFound) {
			Preconditions.checkArgument(!subType.isArray());
			Preconditions.checkArgument(!subType.isPrimitive());
			Preconditions.checkArgument(!Collection.class.isAssignableFrom(subType));
			Preconditions.checkArgument(!Map.class.isAssignableFrom(subType));
		}
		
		return subType;
	}
	
	// This method simply checks if one class is bound by another via boundBy
	// parameter
	// of the VersionedEntity annotation, it check the complete class hierarchy of
	// both classes.
	public boolean isBoundByClass(Class<?> boundClassType, Class<?> bindClassType) {
		// TODO add preconditions
		// TODO add caching Class -> Class
		boolean isBoundByClass = false;
		
		// Find names of all classes which are bind this class
		final Set<String> namesOfBindClasses = Sets.newHashSet();
		// TODO add caching for class -> Set<String> namesOfBindClasses
		while (boundClassType != null) {
			if (boundClassType.isAnnotationPresent(VersionedEntity.class)
					&& (boundClassType.getAnnotation(VersionedEntity.class).boundBy() != null)
					&& (boundClassType.getAnnotation(VersionedEntity.class).boundBy().length > 0)) {
				namesOfBindClasses.addAll(Arrays.asList(boundClassType.getAnnotation(VersionedEntity.class).boundBy()));
			}
			boundClassType = boundClassType.getSuperclass();
		}
		
		if (!namesOfBindClasses.isEmpty()) {
			while ((bindClassType != null) && !isBoundByClass) {
				if (namesOfBindClasses.contains(bindClassType.getSimpleName())) {
					isBoundByClass = true;
				}
				bindClassType = bindClassType.getSuperclass();
			}
		}
		return isBoundByClass;
	}
}
