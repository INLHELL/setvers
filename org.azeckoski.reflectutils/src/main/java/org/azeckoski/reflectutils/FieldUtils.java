/**
 * $Id: FieldUtils.java 29 2008-10-02 22:30:48Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/FieldUtils.java $
 * FieldUtils.java - genericdao - May 19, 2008 10:10:15 PM - azeckoski
 ************************************************************************** 
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this
 * distribution and is available at:
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @
 * caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ClassProperty.IndexedProperty;
import org.azeckoski.reflectutils.ClassProperty.MappedProperty;
import org.azeckoski.reflectutils.beanutils.DefaultResolver;
import org.azeckoski.reflectutils.beanutils.FieldAdapter;
import org.azeckoski.reflectutils.beanutils.FieldAdapterManager;
import org.azeckoski.reflectutils.beanutils.Resolver;
import org.azeckoski.reflectutils.exceptions.FieldGetValueException;
import org.azeckoski.reflectutils.exceptions.FieldSetValueException;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;

/**
 * Class which provides methods for dealing with the fields in objects and
 * classes,
 * this provides the core functionality for the reflect util class<br/>
 * <br/>
 * Setting and getting fields supports simple, nested, indexed, and mapped
 * values:<br/>
 * <b>Simple:</b> Get/set a field in a bean (or map), Example: "title", "id"<br/>
 * <b>Nested:</b> Get/set a field in a bean which is contained in another bean,
 * Example: "someBean.title", "someBean.id"<br/>
 * <b>Indexed:</b> Get/set a list/array item by index in a bean, Example:
 * "myList[1]", "anArray[2]"<br/>
 * <b>Mapped:</b> Get/set a map entry by key in a bean, Example: "myMap(key)",
 * "someMap(thing)"<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class FieldUtils {
	
	public static final class Holder {
		
		public String name;
		public Object object;
		
		public Holder(final String name, final Object object) {
			this.name = name;
			this.object = object;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Object getObject() {
			return this.object;
		}
	}
	
	protected static SoftReference<FieldUtils> instanceStorage;
	
	private static int timesCreated = 0;
	
	protected FieldAdapterManager fieldAdapterManager;
	
	protected Resolver nameResolver = null;
	
	private boolean singleton = false;
	
	/**
	 * Constructor which allows the field path name resolver to be set
	 * specifically <br/>
	 * <b>WARNING:</b> if you don't need this control then just use the
	 * {@link #getInstance()} method to get this
	 * 
	 * @param resolver controls the resolution of indexed, nested, and mapped
	 *          field paths
	 */
	public FieldUtils(final Resolver resolver) {
		this.setResolver(resolver);
		
		this.fieldAdapterManager = new FieldAdapterManager();
		
		FieldUtils.setInstance(this);
	}
	
	/**
	 * Empty constructor - protected
	 */
	protected FieldUtils() {
		this(null);
	}
	
	/**
	 * Get a singleton instance of this class to work with (stored statically) <br/>
	 * <b>WARNING</b>: do not hold onto this object or cache it yourself, call
	 * this method again if you need it again
	 * 
	 * @return a singleton instance of this class
	 */
	public static FieldUtils getInstance() {
		FieldUtils instance = (FieldUtils.instanceStorage == null ? null : FieldUtils.instanceStorage.get());
		if (instance == null) {
			instance = FieldUtils.setInstance(null);
		}
		return instance;
	}
	
	public static int getTimesCreated() {
		return FieldUtils.timesCreated;
	}
	
	/**
	 * Set the singleton instance of the class which will be stored statically
	 * 
	 * @param instance the instance to use as the singleton instance
	 */
	public static FieldUtils setInstance(final FieldUtils newInstance) {
		FieldUtils instance = newInstance;
		if (instance == null) {
			instance = new FieldUtils();
			instance.singleton = true;
		}
		FieldUtils.timesCreated++;
		FieldUtils.instanceStorage = new SoftReference<FieldUtils>(instance);
		return instance;
	}
	
	/**
	 * Analyze a class and produce an object which contains information about it
	 * and its fields
	 * 
	 * @param <T>
	 * @param cls any class
	 * @return the ClassFields analysis object which contains the information
	 *         about this object class
	 * @throws IllegalArgumentException if class is null or primitive
	 */
	public <T> ClassFields<T> analyzeClass(final Class<T> cls) {
		final ClassFields<T> cf = this.getClassDataCacher().getClassFields(cls);
		return cf;
	}
	
	/**
	 * Analyze an object and produce an object which contains information about it
	 * and its fields
	 * 
	 * @param obj any object
	 * @return the ClassFields analysis object which contains the information
	 *         about this object class
	 * @throws IllegalArgumentException if obj is null
	 */
	@SuppressWarnings("unchecked")
	public <T> ClassFields<T> analyzeObject(final Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if (Class.class.equals(obj)) {
			// this is a class so we should pass it over to the other method
			return this.analyzeClass((Class) obj);
		}
		final Class<T> cls = (Class<T>) obj.getClass();
		return this.analyzeClass(cls);
	}
	
	public ConstructorUtils getConstructorUtils() {
		return ConstructorUtils.getInstance();
	}
	
	public ConversionUtils getConversionUtils() {
		return ConversionUtils.getInstance();
	}
	
	/**
	 * INTERNAL USAGE
	 * 
	 * @return the field adapter being used by this set of field utils
	 */
	public FieldAdapter getFieldAdapter() {
		return this.fieldAdapterManager.getFieldAdapter();
	}
	
	/**
	 * Get the names of all fields in a class
	 * 
	 * @param cls any class
	 * @return a list of the field names
	 */
	public <T> List<String> getFieldNames(final Class<T> cls) {
		final ClassFields<T> cf = this.analyzeClass(cls);
		return cf.getFieldNames();
	}
	
	public <T> List<String> getFieldNames(final Class<T> cls, final FieldsFilter filter) {
		final ClassFields<T> cf = this.analyzeClass(cls);
		return cf.getFieldNames();
	}
	
	/**
	 * Finds the type for a field based on the given class and the field name
	 * 
	 * @param type any class type
	 * @param name the name of a field in this class (can be nested, indexed,
	 *          mapped, etc.)
	 * @return the type of the field (will be {@link Object} if the type is
	 *         indeterminate)
	 */
	public Class<?> getFieldType(Class<?> type, String name) {
		if ((type == null) || (name == null)) {
			throw new IllegalArgumentException("type and name must not be null");
		}
		// get the nested class or return Object.class as a cop out
		while (this.getResolver().hasNested(name)) {
			final String next = this.getResolver().next(name);
			Class<?> nestedClass = null;
			if (Object.class.equals(type) || Map.class.isAssignableFrom(type) || this.getResolver().isMapped(next)
					|| this.getResolver().isIndexed(next)) {
				// these can contain objects or it is Object so we bail out
				return Object.class;
			}
			else {
				// a real class, hooray, analyze it
				final ClassFields<?> cf = this.analyzeClass(type);
				nestedClass = cf.getFieldType(name);
			}
			type = nestedClass;
			name = this.getResolver().remove(name);
		}
		final String targetName = this.getResolver().getProperty(name); // simple name of
																													// target field
		// get the type
		Class<?> fieldType = null;
		if (ConstructorUtils.isClassObjectHolder(type) || Object.class.equals(type)) {
			// special handling for the holder types, needed because attempting to
			// analyze a map or other container will cause a failure
			fieldType = Object.class;
		}
		else {
			// normal object
			final ClassFields<?> cf = this.analyzeClass(type);
			try {
				fieldType = cf.getFieldType(targetName);
			}
			catch (final FieldnameNotFoundException fnfe) {
				// could not find this as a standard field so handle as internal lookup
				final ClassData<?> cd = cf.getClassData();
				final Field field = this.getFieldIfPossible(cd, name);
				if (field == null) {
					throw new FieldnameNotFoundException("Could not find field with name (" + name + ") in class (" + type
																								+ ") after extended look into non-visible fields", fnfe);
				}
				fieldType = field.getType();
			}
		}
		// special handling for indexed and mapped names
		if (this.getResolver().isIndexed(name) || this.getResolver().isMapped(name)) {
			if (ConstructorUtils.isClassArray(fieldType)) {
				// use the array component type
				fieldType = type.getComponentType();
			}
			else {
				// default for contained type of holders
				fieldType = Object.class;
			}
		}
		return fieldType;
	}
	
	/**
	 * Finds the type for a field based on the containing object and the field
	 * name
	 * 
	 * @param obj any object
	 * @param name the name of a field in this object (can be nested, indexed,
	 *          mapped, etc.)
	 * @return the type of the field (will be {@link Object} if the type is
	 *         indeterminate)
	 * @throws FieldnameNotFoundException if the name is invalid for this obj
	 * @throws IllegalArgumentException if the params are null
	 */
	@SuppressWarnings("unchecked")
	public Class<?> getFieldType(Object obj, String name) {
		if ((obj == null) || (name == null)) {
			throw new IllegalArgumentException("obj and name must not be null");
		}
		if (Class.class.equals(obj)) {
			// this is a class so we should pass it over to the other method
			return this.getFieldType((Class<?>) obj, name); // EXIT
		}
		if (Object.class.equals(obj.getClass())) {
			return Object.class; // EXIT
		}
		// get the nested object or die
		while (this.getResolver().hasNested(name)) {
			final String next = this.getResolver().next(name);
			Object nestedBean = null;
			if (Map.class.isAssignableFrom(obj.getClass())) {
				nestedBean = this.getValueOfMap((Map) obj, next);
			}
			else if (this.getResolver().isMapped(next)) {
				nestedBean = this.getMappedValue(obj, next);
			}
			else if (this.getResolver().isIndexed(next)) {
				nestedBean = this.getIndexedValue(obj, next);
			}
			else {
				nestedBean = this.getSimpleValue(obj, next);
			}
			if (nestedBean == null) {
				// no auto create so we have to fail here
				throw new NullPointerException("Nested traversal failure: null field value for name (" + name
																				+ ") on object class (" + obj.getClass() + ") for object: " + obj);
			}
			obj = nestedBean;
			name = this.getResolver().remove(name);
		}
		final String targetName = this.getResolver().getProperty(name); // simple name of
																													// target field
		// get the type
		Class<?> fieldType = null;
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			fieldType = this.fieldAdapterManager.getFieldAdapter().getFieldType(obj, targetName);
		}
		else if (ConstructorUtils.isClassObjectHolder(obj.getClass()) || Object.class.equals(obj.getClass())) {
			// special handling for the holder types, needed because attempting to
			// analyze a map or other container will cause a failure
			fieldType = Object.class;
		}
		else {
			// normal object
			final ClassFields<?> cf = this.analyzeObject(obj);
			try {
				final ClassProperty cp = cf.getClassProperty(targetName);
				fieldType = cp.getType();
			}
			catch (final FieldnameNotFoundException fnfe) {
				// could not find this as a standard field so handle as internal lookup
				final ClassData<?> cd = cf.getClassData();
				final Field field = this.getFieldIfPossible(cd, name);
				if (field == null) {
					throw new FieldnameNotFoundException("Could not find field with name (" + name + ") on object (" + obj
																								+ ") after extended look into non-visible fields", fnfe);
				}
				fieldType = field.getType();
			}
		}
		// special handling for indexed and mapped names
		if (this.getResolver().isIndexed(name) || this.getResolver().isMapped(name)) {
			if (ConstructorUtils.isClassArray(fieldType)) {
				// use the array component type
				fieldType = fieldType.getComponentType();
			}
			else {
				// default for contained type of holders
				fieldType = Object.class;
			}
		}
		return fieldType;
	}
	
	/**
	 * Get the types of the fields of a specific class type <br/>
	 * returns the method names as fields (without the "get"/"is" part and
	 * camelCased)
	 * 
	 * @param type any class
	 * @param filter (optional) indicates the fields to return the types for, can
	 *          be null for defaults
	 * @return a map of field name -> class type
	 */
	public Map<String, Class<?>> getFieldTypes(final Class<?> type, final FieldsFilter filter) {
		final ClassFields<?> cf = this.analyzeClass(type);
		final Map<String, Class<?>> types = cf.getFieldTypes(filter);
		return types;
	}
	
	/**
	 * Get the value of a field on an object,
	 * name can be nested, indexed, or mapped
	 * 
	 * @param obj any object
	 * @param name the name of a field on this object
	 * @return the value of the field
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws FieldGetValueException if the field is not readable or not visible
	 * @throws IllegalArgumentException if there is a failure getting the value
	 */
	@SuppressWarnings("unchecked")
	public Object getFieldValue(Object obj, String name) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		// Resolve nested references
		final Holder holder = this.unpackNestedName(name, obj, false);
		name = holder.getName();
		obj = holder.getObject();
		
		Object value = null;
		if (Map.class.isAssignableFrom(obj.getClass())) {
			value = this.getValueOfMap((Map) obj, name);
		}
		else if (this.getResolver().isMapped(name)) {
			value = this.getMappedValue(obj, name);
		}
		else if (this.getResolver().isIndexed(name)) {
			value = this.getIndexedValue(obj, name);
		}
		else {
			value = this.getSimpleValue(obj, name);
		}
		return value;
	}
	
	/**
	 * Get the value of a field on an object as a specific type,
	 * name can be nested, indexed, or mapped
	 * 
	 * @param obj any object
	 * @param name the name of a field on this object
	 * @param asType the type to return the value as (converts as needed)
	 * @return the value in the field as the type requested
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws FieldGetValueException if the field is not readable or not visible
	 * @throws UnsupportedOperationException if the value cannot be converted to
	 *           the type requested
	 * @throws IllegalArgumentException if there is a failure getting the value
	 */
	public <T> T getFieldValue(final Object obj, final String name, final Class<T> asType) {
		final Object o = this.getFieldValue(obj, name);
		final T value = this.getConversionUtils().convert(o, asType);
		return value;
	}
	
	/**
	 * Get the values of all readable fields on an object (may not all be
	 * writeable)
	 * 
	 * @param obj any object
	 * @return a map of field name -> value
	 * @throws IllegalArgumentException if the obj is null
	 */
	public Map<String, Object> getFieldValues(final Object obj) {
		return this.getFieldValues(obj, FieldsFilter.READABLE, false);
	}
	
	// INTERNAL - specific methods which are not really for general use
	
	/**
	 * Get the values of all fields on an object but optionally filter the fields
	 * used
	 * 
	 * @param obj any object
	 * @param filter (optional) indicates the fields to return the values for, can
	 *          be null for defaults <br/>
	 *          WARNING: getting the field values from settable only fields works
	 *          as expected (i.e. you will an empty map)
	 * @param includeClassField if true then the value from the "getClass()"
	 *          method is returned as part of the
	 *          set of object values with a type of {@link Class} and a field name
	 *          of "class"
	 * @return a map of field name -> value
	 * @throws IllegalArgumentException if the obj is null
	 */
	public Map<String, Object> getFieldValues(final Object obj, final FieldsFilter filter, final boolean includeClassField) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		final Map<String, Object> values = new ArrayOrderedMap<String, Object>();
		if (includeClassField) {
			// add as the first field
			values.put(ClassFields.FIELD_CLASS, obj.getClass());
		}
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			values.putAll(this.fieldAdapterManager.getFieldAdapter().getFieldValues(obj, filter));
		}
		else {
			final Map<String, Class<?>> types = this.getFieldTypes(obj.getClass(), filter);
			if (FieldsFilter.WRITEABLE.equals(filter)) {
				types.clear();
			}
			for (final String name : types.keySet()) {
				try {
					final Object o = this.getFieldValue(obj, name);
					values.put(name, o);
				}
				catch (final RuntimeException e) {
					// failed to get the value so we will skip this one
					continue;
				}
			}
		}
		return values;
	}
	
	public boolean isSingleton() {
		return this.singleton;
	}
	
	/**
	 * Set the value of a field on an object (automatically auto converts),
	 * name can be nested, indexed, or mapped
	 * 
	 * @param obj any object
	 * @param name the name of a field on this object
	 * @param value the value to set the field to (must match target exactly)
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws FieldSetValueException if the field is not writeable or visible
	 * @throws IllegalArgumentException if there is a general failure setting the
	 *           value
	 */
	public void setFieldValue(final Object obj, final String name, final Object value) {
		this.setFieldValue(obj, name, value, true);
	}
	
	/**
	 * Set the value of a field on an object (optionally convert the value to the
	 * field type),
	 * name can be nested, indexed, or mapped
	 * 
	 * @param obj any object
	 * @param name the name of a field on this object
	 * @param value the value to set the field to
	 * @param autoConvert if true then the value will be converted to the target
	 *          value type if it is possible,
	 *          otherwise the value must match the target type exactly
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws UnsupportedOperationException if this value cannot be auto
	 *           converted to the type specified
	 * @throws FieldSetValueException if the field is not writeable or visible
	 * @throws IllegalArgumentException if there is a general failure setting the
	 *           value
	 */
	@SuppressWarnings("unchecked")
	public void setFieldValue(Object obj, String name, Object value, final boolean autoConvert) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		// Resolve nested references
		final Holder holder = this.unpackNestedName(name, obj, true);
		name = holder.getName();
		obj = holder.getObject();
		
		if (autoConvert) {
			// auto convert the value to the target type if possible
			// String targetName = getResolver().getProperty(name); // simple name of
			// target field
			// attempt to convert the value into the target type
			final Class<?> type = this.getFieldType(obj, name);
			value = this.getConversionUtils().convert(value, type);
		}
		
		// set the value
		if (Map.class.isAssignableFrom(obj.getClass())) {
			this.setValueOfMap((Map) obj, name, value);
		}
		else if (this.getResolver().isMapped(name)) {
			this.setMappedValue(obj, name, value);
		}
		else if (this.getResolver().isIndexed(name)) {
			this.setIndexedValue(obj, name, value);
		}
		else {
			this.setSimpleValue(obj, name, value);
		}
	}
	
	/**
	 * For setting an indexed value on an indexed object directly,
	 * indexed objects are lists and arrays <br/>
	 * NOTE: If the supplied index is invalid for the array then this will fail
	 * 
	 * @param indexedObject any array or list
	 * @param index the index to put the value into (will append to the end of the
	 *          list if index < 0), must be within the bounds of the array
	 * @param value any value, will be converted to the correct type for the array
	 *          automatically
	 * @throws IllegalArgumentException if there is a failure because of an
	 *           invalid index or null arguments
	 * @throws FieldSetValueException if the field is not writeable or visible
	 */
	@SuppressWarnings("unchecked")
	public void setIndexedValue(final Object indexedObject, final int index, final Object value) {
		if (indexedObject == null) {
			throw new IllegalArgumentException("Invalid indexedObject, cannot be null");
		}
		if (ConstructorUtils.isClassArray(indexedObject.getClass())) {
			// this is an array
			try {
				// set the value on the array
				// NOTE: cannot automatically expand the array
				final Class<?> componentType = ArrayUtils.type((Object[]) indexedObject);
				final Object convert = ReflectUtils.getInstance().convert(value, componentType);
				Array.set(indexedObject, index, convert);
			}
			catch (final Exception e) {
				throw new IllegalArgumentException("Failed to set index (" + index + ") for array of size ("
																						+ Array.getLength(indexedObject) + ") to value: " + value, e);
			}
		}
		else if (ConstructorUtils.isClassList(indexedObject.getClass())) {
			// this is a list
			final List l = (List) indexedObject;
			try {
				// set value on list
				if (index < 0) {
					l.add(value);
				}
				else {
					if (index >= l.size()) {
						// automatically expand the list
						final int start = l.size();
						for (int i = start; i < (index + 1); i++) {
							l.add(i, null);
						}
					}
					l.set(index, value);
				}
			}
			catch (final Exception e) {
				// catching the general exception is correct here, translate the
				// exception
				throw new IllegalArgumentException("Failed to set index (" + index + ") for list of size (" + l.size()
																						+ ") to value: " + value, e);
			}
		}
		else {
			// invalid
			throw new IllegalArgumentException("Object does not appear to be indexed (not an array or a list): "
																					+ (indexedObject == null ? "NULL" : indexedObject.getClass()));
		}
	}
	
	public void setResolver(final Resolver resolver) {
		if (resolver != null) {
			this.nameResolver = resolver;
		}
		else {
			this.getResolver();
		}
	}
	
	@Override
	public String toString() {
		return "Field::c=" + FieldUtils.timesCreated + ":s=" + this.singleton + ":resolver="
						+ this.getResolver().getClass().getName();
	}
	
	/**
	 * This will set the value on a field, types must match,
	 * for internal use only,
	 * Reduce code duplication
	 * 
	 * @param obj any object
	 * @param cp the analysis object which must match the given name and object
	 * @param value the value for the field
	 * @throws FieldSetValueException if the field is not writeable or visible
	 * @throws IllegalArgumentException if inputs are invalid (null)
	 */
	protected void assignFieldValue(final Object obj, final ClassProperty cp, final Object value) {
		if (obj == null) {
			throw new IllegalArgumentException("Object cannot be null");
		}
		if (cp == null) {
			throw new IllegalArgumentException("ClassProperty cannot be null");
		}
		if (cp.isPublicField()) {
			final Field field = cp.getField();
			try {
				field.set(obj, value);
			}
			catch (final Exception e) {
				// catching the general exception is correct here, translate the
				// exception
				throw new FieldSetValueException("Field set failure setting value (" + value + ") for name ("
																					+ cp.getFieldName() + ") on: " + obj, cp.getFieldName(), value, obj, e);
			}
		}
		else {
			// must be a property then
			final Method setter = cp.getSetter();
			try {
				setter.invoke(obj, new Object[] {
					value
				});
			}
			catch (final Exception e) {
				throw new FieldSetValueException("Setter method failure setting value (" + value + ") for name ("
																					+ cp.getFieldName() + ") on: " + obj, cp.getFieldName(), value, obj, e);
			}
		}
	}
	
	/**
	 * This will get the value from a field,
	 * for internal use only,
	 * Reduce code duplication
	 * 
	 * @param obj any object
	 * @param cp the analysis object which must match the given object (defines
	 *          the field)
	 * @return the value for the field
	 * @throws IllegalArgumentException if inputs are invalid (null)
	 * @throws FieldGetValueException if there is an internal failure getting the
	 *           field
	 */
	protected Object findFieldValue(final Object obj, final ClassProperty cp) {
		if (obj == null) {
			throw new IllegalArgumentException("Object cannot be null");
		}
		if (cp == null) {
			throw new IllegalArgumentException("ClassProperty cannot be null");
		}
		
		Object value = null;
		if (cp.isPublicField()) {
			final Field field = cp.getField();
			try {
				value = field.get(obj);
			}
			catch (final Exception e) {
				// catching the general exception is correct here, translate the
				// exception
				throw new FieldGetValueException("Field get failure getting value for name (" + cp.getFieldName() + ") from: "
																					+ obj, cp.getFieldName(), obj, e);
			}
		}
		else {
			// must be a property then
			final Method getter = cp.getGetter();
			try {
				value = getter.invoke(obj, new Object[0]);
			}
			catch (final Exception e) {
				// catching the general exception is correct here, translate the
				// exception
				throw new FieldGetValueException("Getter method failure getting value for name (" + cp.getFieldName()
																					+ ") from: " + obj, cp.getFieldName(), obj, e);
			}
		}
		return value;
	}
	
	protected ClassDataCacher getClassDataCacher() {
		return ClassDataCacher.getInstance();
	}
	
	/**
	 * Get the field if it exists for this class
	 * 
	 * @param cd the class data cache object
	 * @param name the name of the field
	 * @return the field if found OR null if not
	 */
	protected Field getFieldIfPossible(final ClassData<?> cd, final String name) {
		Field f = null;
		final List<Field> fields = cd.getFields();
		for (final Field field : fields) {
			if (field.getName().equals(name)) {
				f = field;
				break;
			}
		}
		return f;
	}
	
	/**
	 * For getting an indexed value out of an object based on field name,
	 * name must be like: fieldname[index],
	 * If the object is an array or a list then name can be the index only:
	 * "[1]" or "[0]" (brackets must be included) <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there is a failure
	 * @throws FieldGetValueException if there is an internal failure getting the
	 *           field
	 */
	@SuppressWarnings("unchecked")
	protected Object getIndexedValue(final Object obj, String name) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		Object value = null;
		final Resolver resolver = this.getResolver();
		
		// get the index from the indexed name
		int index = -1;
		try {
			index = resolver.getIndex(name);
			if (index < 0) {
				throw new IllegalArgumentException("Could not find index in name (" + name + ")");
			}
			// get the fieldname from the indexed name
			name = resolver.getProperty(name);
		}
		catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid indexed field (" + name + ") on type (" + obj.getClass() + ")", e);
		}
		
		boolean indexedProperty = false;
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			value = this.fieldAdapterManager.getFieldAdapter().getIndexedValue(obj, name, index);
		}
		else {
			boolean isArray = false;
			Object indexedObject = null;
			if (obj.getClass().isArray()) {
				indexedObject = obj;
				isArray = true;
			}
			else if (List.class.isAssignableFrom(obj.getClass())) {
				indexedObject = obj;
			}
			else {
				// normal bean
				final ClassFields cf = this.analyzeObject(obj);
				final ClassProperty cp = cf.getClassProperty(name);
				if (!cp.isIndexed()) {
					throw new IllegalArgumentException("This field (" + name + ") is not an indexed field");
				}
				isArray = cp.isArray();
				// try to get the indexed getter and use that first
				if (cp instanceof IndexedProperty) {
					indexedProperty = true;
					final IndexedProperty icp = (IndexedProperty) cp;
					try {
						final Method getter = icp.getIndexGetter();
						value = getter.invoke(obj, new Object[] {
							index
						});
					}
					catch (final Exception e) {
						// catching the general exception is correct here, translate the
						// exception
						throw new FieldGetValueException("Indexed getter method failure getting indexed (" + index
																							+ ") value for name (" + cp.getFieldName() + ") from: " + obj,
							cp.getFieldName(), obj, e);
					}
				}
				else {
					indexedObject = this.findFieldValue(obj, cp);
				}
			}
			if (!indexedProperty) {
				// now get the indexed value if possible
				if (indexedObject != null) {
					if (isArray) {
						// this is an array
						try {
							// get value from array
							value = Array.get(indexedObject, index);
						}
						catch (final ArrayIndexOutOfBoundsException e) {
							throw new IllegalArgumentException("Index (" + index + ") is out of bounds ("
																									+ Array.getLength(indexedObject) + ") for the array: " + value, e);
						}
					}
					else {
						// this better be a list
						if (!List.class.isAssignableFrom(indexedObject.getClass())) {
							throw new IllegalArgumentException("Field (" + name
																									+ ") does not appear to be indexed (not an array or a list)");
						}
						else {
							// get value from list
							try {
								value = ((List) indexedObject).get(index);
							}
							catch (final IndexOutOfBoundsException e) {
								throw new IllegalArgumentException("Index (" + index + ") is out of bounds ("
																										+ ((List) indexedObject).size() + ") for the list: " + value, e);
							}
						}
					}
				}
				else {
					throw new IllegalArgumentException("Indexed object is null, cannot retrieve index (" + index
																							+ ") value from field (" + name + ")");
				}
			}
		}
		return value;
	}
	
	/**
	 * For getting a mapped value out of an object based on field name,
	 * name must be like: fieldname[index] <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there are invalid arguments
	 * @throws FieldGetValueException if there is an internal failure getting the
	 *           field
	 */
	@SuppressWarnings("unchecked")
	protected Object getMappedValue(final Object obj, String name) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		Object value = null;
		final Resolver resolver = this.getResolver();
		
		// get the key from the mapped name
		String key = null;
		try {
			key = resolver.getKey(name);
			if (key == null) {
				throw new IllegalArgumentException("Could not find key in name (" + name + ")");
			}
			// get the fieldname from the mapped name
			name = resolver.getProperty(name);
		}
		catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid mapped field (" + name + ") on type (" + obj.getClass() + ")", e);
		}
		
		boolean mappedProperty = false;
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			value = this.fieldAdapterManager.getFieldAdapter().getMappedValue(obj, name, key);
		}
		else {
			Map map = null;
			if (Map.class.isAssignableFrom(obj.getClass())) {
				map = (Map) obj;
			}
			else {
				// normal bean
				final ClassFields cf = this.analyzeObject(obj);
				final ClassProperty cp = cf.getClassProperty(name);
				if (!cp.isMapped()) {
					throw new IllegalArgumentException("This field (" + name + ") is not an mapped field");
				}
				// try to get the mapped getter and use that first
				if (cp instanceof MappedProperty) {
					mappedProperty = true;
					final MappedProperty mcp = (MappedProperty) cp;
					try {
						final Method getter = mcp.getMapGetter();
						value = getter.invoke(obj, new Object[] {
							key
						});
					}
					catch (final Exception e) {
						// catching the general exception is correct here, translate the
						// exception
						throw new FieldGetValueException("Mapped getter method failure getting mapped (" + key
																							+ ") value for name (" + cp.getFieldName() + ") from: " + obj,
							cp.getFieldName(), obj, e);
					}
				}
				else {
					final Object o = this.findFieldValue(obj, cp);
					if (!Map.class.isAssignableFrom(o.getClass())) {
						throw new IllegalArgumentException("Field (" + name + ") does not appear to be a map (not instance of Map)");
					}
					map = (Map) o;
				}
			}
			// get the value from the map
			if (!mappedProperty) {
				if (map != null) {
					try {
						value = map.get(key);
					}
					catch (final Exception e) {
						throw new IllegalArgumentException("Key (" + key + ") is invalid (" + map.size() + ") for the map: " + map,
							e);
					}
				}
				else {
					throw new IllegalArgumentException("Mapped object is null, cannot retrieve key (" + key
																							+ ") value from field (" + name + ")");
				}
			}
		}
		return value;
	}
	
	protected Resolver getResolver() {
		if (this.nameResolver == null) {
			this.nameResolver = new DefaultResolver();
		}
		return this.nameResolver;
	}
	
	// STATIC access
	
	/**
	 * For getting a value out of a bean based on a field name <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there is failure
	 */
	protected Object getSimpleValue(final Object obj, final String name) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		Object value = null;
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			value = this.fieldAdapterManager.getFieldAdapter().getSimpleValue(obj, name);
		}
		else {
			// normal bean
			final ClassFields<?> cf = this.analyzeObject(obj);
			try {
				// use the class property
				final ClassProperty cp = cf.getClassProperty(name);
				value = this.findFieldValue(obj, cp);
			}
			catch (final FieldnameNotFoundException fnfe) {
				// could not find this as a standard field so handle as internal lookup
				final ClassData<?> cd = cf.getClassData();
				final Field field = this.getFieldIfPossible(cd, name);
				if (field == null) {
					throw new FieldnameNotFoundException("Could not find field with name (" + name + ") on object (" + obj
																								+ ") after extended look into non-visible fields", fnfe);
				}
				try {
					value = field.get(obj);
				}
				catch (final Exception e) {
					// catching the general exception is correct here, translate the
					// exception
					throw new FieldGetValueException("Field get failure getting value for field (" + name
																						+ ") from non-visible field in object: " + obj, name, obj, e);
				}
			}
		}
		return value;
	}
	
	/**
	 * For getting a value out of a map based on a field name which has a key in
	 * it,
	 * name is expected to be the key for the map only: e.g. "mykey",
	 * if it happens to be of the form: thing[mykey] then the key will be
	 * extracted <br/>
	 * <b>WARNING: Cannot handle a nested name or mapped/indexed key</b>
	 * 
	 * @return the value in the map with a key matching this name OR null if no
	 *         key found
	 */
	@SuppressWarnings("unchecked")
	protected Object getValueOfMap(final Map map, String name) {
		final Resolver resolver = this.getResolver();
		if (resolver.isMapped(name)) {
			final String propName = resolver.getProperty(name);
			if ((propName == null) || (propName.length() == 0)) {
				name = resolver.getKey(name);
			}
		}
		
		final Object value = map.get(name);
		return value;
	}
	
	/**
	 * For setting an indexed value on an object based on field name,
	 * name must be like: fieldname[index] <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there is a failure
	 * @throws FieldSetValueException if there is an internal failure setting the
	 *           field
	 */
	@SuppressWarnings("unchecked")
	protected void setIndexedValue(final Object obj, String name, final Object value) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		final Resolver resolver = this.getResolver();
		// get the index from the indexed name
		int index = -1;
		try {
			index = resolver.getIndex(name);
			if (index < 0) {
				throw new IllegalArgumentException("Could not find index in name (" + name + ")");
			}
			// get the fieldname from the indexed name
			name = resolver.getProperty(name);
		}
		catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid indexed field (" + name + ") on type (" + obj.getClass() + ")", e);
		}
		
		boolean indexedProperty = false;
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			this.fieldAdapterManager.getFieldAdapter().setIndexedValue(obj, name, index, value);
		}
		else {
			boolean isArray = false;
			Object indexedObject = null;
			if (ConstructorUtils.isClassArray(obj.getClass())) {
				indexedObject = obj;
				isArray = true;
			}
			else if (ConstructorUtils.isClassList(obj.getClass())) {
				indexedObject = obj;
			}
			else {
				// normal bean
				final ClassFields cf = this.analyzeObject(obj);
				final ClassProperty cp = cf.getClassProperty(name);
				if (!cp.isIndexed()) {
					throw new IllegalArgumentException("This field (" + name + ") is not an indexed field");
				}
				isArray = cp.isArray();
				// try to get the indexed setter and use that first
				if (cp instanceof IndexedProperty) {
					indexedProperty = true;
					final IndexedProperty icp = (IndexedProperty) cp;
					try {
						final Method setter = icp.getIndexSetter();
						setter.invoke(obj, new Object[] {
							index,
							value
						});
					}
					catch (final Exception e) {
						// catching the general exception is correct here, translate the
						// exception
						throw new FieldSetValueException("Indexed setter method failure setting indexed (" + index
																							+ ") value for name (" + cp.getFieldName() + ") on: " + obj,
							cp.getFieldName(), obj, e);
					}
				}
				else {
					// get the field value out and work with it directly
					indexedObject = this.findFieldValue(obj, cp);
					if (indexedObject == null) {
						// handle nulls by creating if possible
						try {
							if (isArray) {
								// create the array if it is null
								final Class<?> type = value.getClass();
								indexedObject = ArrayUtils.create(type, index + 1);
							}
							else { // List
								// create the list if it is null, back-fill it, and assign it
								// back to the object
								final Class<?> type = cp.getType();
								if (type.isInterface()) {
									indexedObject = new ArrayList(index + 1);
								}
								else {
									indexedObject = type.newInstance();
								}
							}
							this.setSimpleValue(obj, name, indexedObject);
						}
						catch (final Exception e) {
							throw new IllegalArgumentException(
								"Indexed object is null, attempt to create list failed, cannot set value for index (" + index
									+ ") on field (" + name + ")", e);
						}
					}
				}
			}
			if (!indexedProperty) {
				// set the indexed value
				if (isArray) {
					// this is an array
					try {
						// set the value on the array
						final int length = ArrayUtils.size((Object[]) indexedObject);
						if (index >= length) {
							// automatically expand the array
							indexedObject = ArrayUtils.resize((Object[]) indexedObject, index + 1);
							this.setSimpleValue(obj, name, indexedObject); // need to put the array
																												// back into the object
						}
						// convert this value to the type for the array
						final Class<?> componentType = ArrayUtils.type((Object[]) indexedObject);
						final Object convert = ReflectUtils.getInstance().convert(value, componentType);
						Array.set(indexedObject, index, convert);
					}
					catch (final Exception e) {
						throw new IllegalArgumentException("Failed to set index (" + index + ") for array of size ("
																								+ Array.getLength(indexedObject) + ") to value: " + value, e);
					}
				}
				else {
					// this better be a list
					if ((indexedObject == null) || !List.class.isAssignableFrom(indexedObject.getClass())) {
						throw new IllegalArgumentException("Field (" + name
																								+ ") does not appear to be indexed (not an array or a list): "
																								+ (indexedObject == null ? "NULL" : indexedObject.getClass()));
					}
					else {
						// this is a list
						final List l = (List) indexedObject;
						try {
							// set value on list
							if (index < 0) {
								l.add(value);
							}
							else {
								if (index >= l.size()) {
									// automatically expand the list
									final int start = l.size();
									for (int i = start; i < (index + 1); i++) {
										l.add(i, null);
									}
								}
								l.set(index, value);
							}
						}
						catch (final Exception e) {
							// catching the general exception is correct here, translate the
							// exception
							throw new IllegalArgumentException("Failed to set index (" + index + ") for list of size (" + l.size()
																									+ ") to value: " + value, e);
						}
					}
				}
			}
		}
	}
	
	/**
	 * For getting a mapped value out of an object based on field name,
	 * name must be like: fieldname[index] <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there is failure
	 * @throws FieldSetValueException if there is an internal failure setting the
	 *           field
	 */
	@SuppressWarnings("unchecked")
	protected void setMappedValue(final Object obj, String name, Object value) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		final Resolver resolver = this.getResolver();
		// get the key from the mapped name
		String key = null;
		try {
			key = resolver.getKey(name);
			if (key == null) {
				throw new IllegalArgumentException("Could not find key in name (" + name + ")");
			}
			// get the fieldname from the mapped name
			name = resolver.getProperty(name);
		}
		catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid mapped field (" + name + ") on type (" + obj.getClass() + ")", e);
		}
		
		boolean mappedProperty = false;
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			this.fieldAdapterManager.getFieldAdapter().setMappedValue(obj, name, key, value);
		}
		else {
			Map map = null;
			if (Map.class.isAssignableFrom(obj.getClass())) {
				map = (Map) obj;
			}
			else {
				// normal bean
				final ClassFields cf = this.analyzeObject(obj);
				final ClassProperty cp = cf.getClassProperty(name);
				if (!cp.isMapped()) {
					throw new IllegalArgumentException("This field (" + name + ") is not an mapped field");
				}
				// try to get the mapped setter and use that first
				if (cp instanceof MappedProperty) {
					mappedProperty = true;
					final MappedProperty mcp = (MappedProperty) cp;
					try {
						final Method setter = mcp.getMapSetter();
						value = setter.invoke(obj, new Object[] {
							key,
							value
						});
					}
					catch (final Exception e) {
						// catching the general exception is correct here, translate the
						// exception
						throw new FieldSetValueException("Mapped setter method failure setting mapped (" + key
																							+ ") value for name (" + cp.getFieldName() + ") on: " + obj,
							cp.getFieldName(), obj, e);
					}
				}
				else {
					final Object o = this.findFieldValue(obj, cp);
					if (o == null) {
						// create the map if it is null and assign it back to the object
						try {
							final Class<?> type = cp.getType();
							if (type.isInterface()) {
								map = new ArrayOrderedMap(5);
							}
							else {
								map = (Map) type.newInstance();
							}
							this.setSimpleValue(obj, name, map);
						}
						catch (final Exception e) {
							// catching the general exception is correct here, translate the
							// exception
							throw new IllegalArgumentException(
								"Mapped object is null, attempt to create map failed, cannot set value for key (" + key
									+ ") on field (" + name + ")", e);
						}
					}
					else {
						if (!Map.class.isAssignableFrom(o.getClass())) {
							throw new IllegalArgumentException("Field (" + name
																									+ ") does not appear to be a map (not instance of Map)");
						}
						map = (Map) o;
					}
				}
			}
			if (!mappedProperty) {
				// set value in map
				if (map == null) {
					throw new IllegalArgumentException("Mapped object is null, cannot set value for key (" + key + ") on field ("
																							+ name + ")");
				}
				// set value on map
				try {
					map.put(key, value);
				}
				catch (final Exception e) {
					throw new IllegalArgumentException("Value (" + value + ") cannot be put for key (" + key + ") for the map: "
																							+ map, e);
				}
			}
		}
	}
	
	/**
	 * Set a value on a field of an object, the types must match and the name must
	 * be identical <br/>
	 * <b>WARNING: Cannot handle a nested/mapped/indexed name</b>
	 * 
	 * @throws FieldnameNotFoundException if this field name is invalid for this
	 *           object
	 * @throws IllegalArgumentException if there is failure
	 * @throws FieldSetValueException if there is an internal failure setting the
	 *           field
	 */
	protected void setSimpleValue(final Object obj, final String name, Object value) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		if ((name == null) || "".equals(name)) {
			throw new IllegalArgumentException("field name cannot be null or blank");
		}
		
		// Handle DynaBean instances specially
		if (this.fieldAdapterManager.isAdaptableObject(obj)) {
			this.fieldAdapterManager.getFieldAdapter().setSimpleValue(obj, name, value);
		}
		else {
			// normal bean
			final ClassFields<?> cf = this.analyzeObject(obj);
			try {
				final ClassProperty cp = cf.getClassProperty(name);
				this.assignFieldValue(obj, cp, value);
			}
			catch (final FieldnameNotFoundException fnfe) {
				// could not find this as a standard field so handle as internal lookup
				final ClassData<?> cd = cf.getClassData();
				final Field field = this.getFieldIfPossible(cd, name);
				if (field == null) {
					throw new FieldnameNotFoundException("Could not find field with name (" + name + ") on object (" + obj
																								+ ") after extended look into non-visible fields", fnfe);
				}
				try {
					value = this.getConversionUtils().convert(value, field.getType());
					field.set(obj, value);
				}
				catch (final Exception e) {
					// catching the general exception is correct here, translate the
					// exception
					throw new FieldSetValueException("Field set failure setting value (" + value + ") for field (" + name
																						+ ") from non-visible field in object: " + obj, name, obj, e);
				}
			}
		}
	}
	
	/**
	 * Set a value on a map using the name as the key,
	 * name is expected to be the key for the map only: e.g. "mykey",
	 * if it happens to be of the form: thing[mykey] then the key will be
	 * extracted <br/>
	 * <b>WARNING: Cannot handle a nested name or mapped/indexed key</b>
	 */
	@SuppressWarnings("unchecked")
	protected void setValueOfMap(final Map map, String name, final Object value) {
		final Resolver resolver = this.getResolver();
		if (resolver.isMapped(name)) {
			final String propName = resolver.getProperty(name);
			if ((propName == null) || (propName.length() == 0)) {
				name = resolver.getKey(name);
			}
		}
		
		map.put(name, value);
	}
	
	/**
	 * Traverses the nested name path to get to the requested name and object
	 * 
	 * @param fullName the full path name (e.g. thing.field1.field2.stuff)
	 * @param object the object to traverse
	 * @param autoCreate if true then create the nested objects to force
	 *          successful traversal, else will throw NPE
	 * @return a holder with the nested name (e.g. stuff) and the nested object
	 * @throws NullPointerException if the path cannot be traversed
	 * @throws IllegalArgumentException if the path is invalid
	 */
	@SuppressWarnings("unchecked")
	protected Holder unpackNestedName(final String fullName, final Object object, final boolean autoCreate) {
		String name = fullName;
		Object obj = object;
		final Class<?> cls = object.getClass();
		try {
			// Resolve nested references
			while (this.getResolver().hasNested(name)) {
				final String next = this.getResolver().next(name);
				Object nestedBean = null;
				if (Map.class.isAssignableFrom(obj.getClass())) {
					nestedBean = this.getValueOfMap((Map) obj, next);
				}
				else if (this.getResolver().isMapped(next)) {
					nestedBean = this.getMappedValue(obj, next);
				}
				else if (this.getResolver().isIndexed(next)) {
					nestedBean = this.getIndexedValue(obj, next);
				}
				else {
					nestedBean = this.getSimpleValue(obj, next);
				}
				if (nestedBean == null) {
					// could not get the nested bean because it is unset
					if (autoCreate) {
						// create the nested bean
						try {
							Class<?> type = this.getFieldType(obj, next);
							if (Object.class.equals(type)) {
								// indeterminate type so we will make a map
								type = ArrayOrderedMap.class;
							}
							nestedBean = this.getConstructorUtils().constructClass(type);
							this.setFieldValue(obj, next, nestedBean, false); // need to put this
																														// new object into
																														// the parent
						}
						catch (final RuntimeException e) {
							throw new IllegalArgumentException("Nested path failure: Could not create nested object ("
																									+ cls.getName() + ") in path (" + fullName + "): " + e.getMessage(),
								e);
						}
					}
					else {
						// no auto create so we have to fail here
						throw new NullPointerException("Nested traversal failure: null field value for name (" + name
																						+ ") in nestedName (" + fullName + ") on object class (" + cls
																						+ ") for object: " + obj);
					}
				}
				obj = nestedBean;
				name = this.getResolver().remove(name);
			}
		}
		catch (final FieldnameNotFoundException e) {
			// convert field name failure into illegal argument
			throw new IllegalArgumentException("Nested path failure: Invalid path name (" + fullName
																					+ ") contains invalid field names: " + e.getMessage(), e);
		}
		return new Holder(name, obj);
	}
	
}
