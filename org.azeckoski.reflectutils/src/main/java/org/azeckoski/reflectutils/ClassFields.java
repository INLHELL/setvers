/**
 * $Id: ClassFields.java 35 2008-11-05 17:19:05Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/ClassFields.java $
 * ClassFields.java - genericdao - May 5, 2008 2:16:35 PM - azeckoski
 ************************************************************************** 
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this
 * distribution and is available at:
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.azeckoski.reflectutils.annotations.ReflectIncludeStaticFields;
import org.azeckoski.reflectutils.annotations.ReflectTransientClassFields;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.azeckoski.reflectutils.map.OrderedMap;

/**
 * @param <T> the class type
 *          This is used as a record (cache) of the information about a classes
 *          fields, properties, and
 *          annotations among other things (property here means a bean standard
 *          property so a public getter
 *          and setter which conform to the bean standard), it may or may not be
 *          a complete record of the
 *          class<br/>
 *          This is primarily meant to provide easy access to information
 *          gathered via reflection
 *          and puts all that information in one convenient package which can be
 *          cached (but must be cached
 *          in a ClassLoader safe way)<br/>
 *          NOTE: It is important that you do not hold onto this object as
 *          it is meant to be cached in a way that it can be collected and will
 *          hold open the ClassLoader
 *          that the class came from<br/>
 *          Terminology:<br/>
 *          Complete field: the value of the field can be
 *          set (is not final or missing a setter) and retrieved (not missing a
 *          getter) <br/>
 *          Partial field:
 *          the value can either be set or retrieved but not both <br/>
 *          Settable: the value can be set on this field (final fields cannot be
 *          set) <br/>
 *          Gettable: the value can be retrieved from this field <br/>
 * <br/>
 *          This object is immutable<br/>
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class ClassFields<T> {
	
	/**
	 * Mode for finding the fields on classes/objects:<br/>
	 * {@link #HYBRID} (default)
	 */
	public static enum FieldFindMode {
		/**
		 * finds all fields which are accessible (public, protected, or private),
		 * ignores getters and setters
		 */
		FIELD,
		/**
		 * (default) finds fields by matched public getters and setters first and
		 * then public fields
		 */
		HYBRID,
		/**
		 * find all matched getters and setters only, ignores fields
		 */
		PROPERTY
	};
	
	/**
	 * Indicates the fields filtering to use when retrieving fields from
	 * types/objects: <br/>
	 * {@link #COMPLETE} (default)
	 */
	public static enum FieldsFilter {
		/**
		 * all fields including ones that are partial (read or write only)
		 */
		ALL,
		/**
		 * (default) all complete (read and write) fields
		 */
		COMPLETE,
		/**
		 * all readable fields (may not be writeable), includes transient fields
		 */
		READABLE,
		/**
		 * all serializable fields (readable fields but skips transient fields)
		 */
		SERIALIZABLE,
		/**
		 * all writeable fields (may not be readable)
		 */
		WRITEABLE
	}
	
	/**
	 * Indicates that this is the getter or setter for the class type and should
	 * be ignored
	 */
	public static class GetClassMethodException extends RuntimeException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1643289043761839115L;

		public GetClassMethodException() {
			super();
		}
	};
	
	// PUBLIC access methods
	
	public static final String FIELD_CLASS = "class";
	
	public static final String METHOD_GET_CLASS = "getClass";
	
	private static final String PREFIX_GET = "get";
	
	private static final String PREFIX_IS = "is";
	
	private static final String PREFIX_SET = "set";
	
	// WARNING: all these things can hold open a ClassLoader
	private final ClassData<T> classData;
	
	private final FieldFindMode fieldFindMode;
	
	/**
	 * includes all the field names which should be ignored for reflection
	 */
	private final Set<String> ignoredFieldNames = new HashSet<String>();
	
	/**
	 * if true then include the "class" field as if it were a regular field
	 */
	private boolean includeClassField = false;
	
	/**
	 * if true then include static fields, otherwise skip them (default is to
	 * skip)
	 */
	private boolean includeStaticFields = false;
	
	private final OrderedMap<String, ClassProperty> namesToProperties; // this
																																			// contains
																																			// all
																																			// properties
																																			// data
																																			// (includes
																																			// partials)
	
	/**
	 * includes all the field names which should be marked as transient
	 */
	private final Set<String> transientFieldNames = new HashSet<String>();
	
	// ANNOTATIONS
	
	/**
	 * Constructor to use when you know nothing about the class at all, this will
	 * use very
	 * straightforward walking over the PUBLIC methods and fields on the class and
	 * will extract the field
	 * information, this uses the default field finding mode of
	 * {@link FieldFindMode#HYBRID}<br/>
	 * <b>NOTE:</b> This is fairly expensive so it is recommended that you cache
	 * the constructed object
	 * by using {@link FieldUtils}
	 * 
	 * @param fieldClass
	 *          this is the class whose fields are represented
	 */
	public ClassFields(final Class<T> fieldClass) {
		this(fieldClass, FieldFindMode.HYBRID, false, false);
	}
	
	/**
	 * Constructor to use when you know nothing about the class at all, this will
	 * use very
	 * straightforward walking over the methods and fields on the class and will
	 * extract the field
	 * information according to the field finding mode<br/>
	 * <b>NOTE:</b> This is fairly expensive so it is recommended that you cache
	 * the constructed object
	 * by using {@link FieldUtils}
	 * 
	 * @param fieldClass this is the class whose fields are represented
	 * @param findMode the search mode for fields on the classes, allows the
	 *          developer to control the way the
	 *          fields are found for later usage<br/>
	 *          HYBRID (default): finds fields by matched public getters and
	 *          setters first and then public fields <br/>
	 *          FIELD: finds all fields which are accessible (public, protected,
	 *          or private), ignores getters and setters <br/>
	 *          PROPERTY: find all matched getters and setters only, ignores
	 *          fields <br/>
	 */
	public ClassFields(final Class<T> fieldClass, final FieldFindMode findMode) {
		this(fieldClass, findMode, false, false);
	}
	
	/**
	 * Constructor to use when you know nothing about the class at all, this will
	 * use the java
	 * introspector as long as the param is true, otherwise it will simply call
	 * over to {@link ClassFields#ClassFields(Class)} and use very
	 * straightforward walking over the methods and fields on the class to extract
	 * the field
	 * information according to the field finding mode<br/>
	 * <b>NOTE:</b> This is fairly expensive so it is recommended that you cache
	 * the constructed object
	 * by using {@link FieldUtils}
	 * 
	 * @param fieldClass this is the class whose fields are represented
	 * @param findMode the search mode for fields on the classes, allows the
	 *          developer to control the way the
	 *          fields are found for later usage<br/>
	 *          HYBRID (default): finds fields by matched public getters and
	 *          setters first and then public fields <br/>
	 *          FIELD: finds all fields which are accessible (public, protected,
	 *          or private), ignores getters and setters <br/>
	 *          PROPERTY: find all matched getters and setters only, ignores
	 *          fields <br/>
	 * @param useIntrospector
	 *          if this is true then the {@link Introspector} is used, this is
	 *          generally the
	 *          slowest way to get information about a class but some people
	 *          prefer it, if false
	 *          then the internal methods are used
	 * @param includeClassFields if true then the "class" field (the result of
	 *          getClass()) is included as a read only field
	 */
	public ClassFields(final Class<T> fieldClass, final FieldFindMode findMode, boolean useIntrospector, final boolean includeClassFields) {
		// set the field class
		if (fieldClass == null) {
			throw new IllegalArgumentException("field class cannot be null");
		}
		this.classData = new ClassData<T>(fieldClass);
		this.fieldFindMode = findMode;
		this.includeClassField = includeClassFields;
		this.namesToProperties = new ArrayOrderedMap<String, ClassProperty>();
		
		// check for reflect annotations on the class
		final List<Annotation> annotations = this.classData.getAnnotations();
		for (final Annotation annotation : annotations) {
			// note that we compare the simple name to avoid issues with cross
			// classloader types
			if (ReflectIncludeStaticFields.class.getSimpleName().equals(annotation.annotationType().getSimpleName())) {
				this.includeStaticFields = true;
			}
			else if (ReflectIgnoreClassFields.class.getSimpleName().equals(annotation.annotationType().getSimpleName())) {
				// this does not work if the classloader of the annotation is not our
				// classloader
				String[] ignore = new String[0];
				if (ReflectIgnoreClassFields.class.equals(annotation.annotationType())) {
					ignore = ((ReflectIgnoreClassFields) annotation).value();
				}
				else {
					// get the value out by reflection on the annotation
					final ClassData<?> cd = ClassDataCacher.getInstance().getClassData(annotation);
					final List<Method> methods = cd.getMethods();
					for (final Method method : methods) {
						if (method.getName().equals("value")) {
							try {
								final Object o = method.invoke(annotation, new Object[] {});
								ignore = (String[]) o;
							}
							catch (final Exception e) {
								throw new RuntimeException("Annotation of the same name has invalid value() method (" + method
																						+ ") and is not of the right type: " + ReflectIgnoreClassFields.class);
							}
							break;
						}
					}
				}
				for (final String name : ignore) {
					this.ignoredFieldNames.add(name);
				}
			}
			else if (ReflectTransientClassFields.class.getSimpleName().equals(annotation.annotationType().getSimpleName())) {
				// this does not work if the classloader of the annotation is not our
				// classloader
				String[] transients = new String[0];
				if (ReflectTransientClassFields.class.equals(annotation.annotationType())) {
					transients = ((ReflectTransientClassFields) annotation).value();
				}
				else {
					// get the value out by reflection on the annotation
					final ClassData<?> cd = ClassDataCacher.getInstance().getClassData(annotation);
					final List<Method> methods = cd.getMethods();
					for (final Method method : methods) {
						if (method.getName().equals("value")) {
							try {
								final Object o = method.invoke(annotation, new Object[] {});
								transients = (String[]) o;
							}
							catch (final Exception e) {
								throw new RuntimeException("Annotation of the same name has invalid value() method (" + method
																						+ ") and is not of the right type: " + ReflectTransientClassFields.class);
							}
							break;
						}
					}
				}
				for (final String name : transients) {
					this.transientFieldNames.add(name);
				}
			}
		}
		
		// populate the internal storage with the list of properties
		if (useIntrospector) {
			// use the java Introspector
			BeanInfo bi;
			try {
				bi = Introspector.getBeanInfo(fieldClass);
				final PropertyDescriptor[] descriptors = bi.getPropertyDescriptors();
				if (descriptors != null) {
					this.populateProperties(descriptors);
					// fields
					this.populateFields(false);
				}
				else {
					useIntrospector = false;
				}
			}
			catch (final IntrospectionException e) {
				// no descriptors so fail over to using the internal methods
				useIntrospector = false;
			}
		}
		
		if (!useIntrospector) {
			// construct using pure reflection
			if (FieldFindMode.HYBRID.equals(findMode) || FieldFindMode.PROPERTY.equals(findMode)) {
				final List<ClassProperty> properties = this.findProperties(true);
				for (final ClassProperty property : properties) {
					final String fieldName = property.getFieldName();
					this.namesToProperties.put(fieldName, property);
				}
			}
			if (FieldFindMode.HYBRID.equals(findMode) || FieldFindMode.FIELD.equals(findMode)) {
				if (FieldFindMode.FIELD.equals(findMode)) {
					this.populateFields(true);
				}
				else {
					this.populateFields(false);
				}
			}
		}
		this.populateAnnotationsFields();
	}
	
	/**
	 * Constructor for when you have everything already in nice arrays, the getter
	 * and setter method
	 * arrays MUST be the same length AND the same order (i.e. getter[0] and
	 * setter[0] must refer to
	 * the same property), no arguments can be null but arrays can be empty, null
	 * values in arrays
	 * will be ignored<br/>
	 * <b>NOTE:</b> validation of the getters and setters is somewhat costly
	 * so you may want to cache the constructed object
	 * 
	 * @param fieldClass
	 *          this is the class whose fields are represented
	 * @param getterMethods
	 *          array of getter methods for properties
	 * @param setterMethods
	 *          array of setter methods for properties
	 * @param publicFields
	 *          array of public fields
	 */
	public ClassFields(final Class<T> fieldClass, final Method[] getterMethods, final Method[] setterMethods, final Field[] publicFields) {
		if ((fieldClass == null) || (getterMethods == null) || (setterMethods == null) || (publicFields == null)) {
			throw new IllegalArgumentException("None of the params can be null");
		}
		if (getterMethods.length != setterMethods.length) {
			throw new IllegalArgumentException("Getter and setter methods must be the same length");
		}
		// set the field class
		this.classData = new ClassData<T>(fieldClass);
		this.fieldFindMode = FieldFindMode.HYBRID;
		// set the properties
		this.namesToProperties = new ArrayOrderedMap<String, ClassProperty>(getterMethods.length + publicFields.length);
		for (int i = 0; i < getterMethods.length; i++) {
			if ((getterMethods[i] != null) && (setterMethods[i] != null)) {
				final String fieldName = this.checkPropertyMethods(getterMethods[i], setterMethods[i]);
				if (!this.namesToProperties.containsKey(fieldName)) {
					final ClassProperty p = new ClassProperty(fieldName, getterMethods[i], setterMethods[i]);
					this.namesToProperties.put(fieldName, p);
				}
			}
		}
		this.populateFields(publicFields);
		this.populateAnnotationsFields();
	}
	
	/**
	 * Constructor for when you have descriptors and fields in arrays (if you have
	 * descriptors it is
	 * easy to get the fields from the class using {@link Class#getFields()}), no
	 * arguments can be
	 * null but the arrays can be empty
	 * 
	 * @param fieldClass
	 *          this is the class whose fields are represented
	 * @param descriptors
	 *          an array of descriptors, you can get these from
	 *          {@link BeanInfo#getPropertyDescriptors()} and you can get the
	 *          {@link BeanInfo} from {@link Introspector#getBeanInfo(Class)}
	 * @param publicFields
	 *          array of public fields
	 */
	public ClassFields(final Class<T> fieldClass, final PropertyDescriptor[] descriptors, final Field[] publicFields) {
		if ((fieldClass == null) || (descriptors == null) || (publicFields == null)) {
			throw new IllegalArgumentException("None of the params can be null");
		}
		this.classData = new ClassData<T>(fieldClass);
		this.fieldFindMode = FieldFindMode.HYBRID;
		this.namesToProperties = new ArrayOrderedMap<String, ClassProperty>(descriptors.length + publicFields.length);
		this.populateProperties(descriptors);
		this.populateFields(publicFields);
		this.populateAnnotationsFields();
	}
	
	/**
	 * Capitalize a string
	 * 
	 * @param input
	 *          any string
	 * @return the string capitalized (e.g. myString -> MyString)
	 */
	public static String capitalize(final String input) {
		String cap = null;
		if (input.length() == 0) {
			cap = "";
		}
		else {
			final char first = Character.toUpperCase(input.charAt(0));
			if (input.length() == 1) {
				cap = first + "";
			}
			else {
				cap = first + input.substring(1);
			}
		}
		return cap;
	}
	
	// SPECIAL methods
	
	/**
	 * @param method
	 *          any method
	 * @return true if this is the getClass() method for standard objects
	 */
	public static boolean isGetClassMethod(final Method method) {
		boolean gc = false;
		if (method != null) {
			if (ClassFields.METHOD_GET_CLASS.equals(method.getName())) {
				gc = true;
			}
		}
		return gc;
	}
	
	/**
	 * @param methodName
	 *          a getter or setter style method name (e.g. getThing, setStuff,
	 *          isType)
	 * @return the fieldName equivalent (thing, stuff, type)
	 */
	public static String makeFieldNameFromMethod(final String methodName) {
		String name = null;
		if (methodName.startsWith(ClassFields.PREFIX_IS)) {
			name = ClassFields.unCapitalize(methodName.substring(2));
		}
		else if (methodName.startsWith(ClassFields.PREFIX_GET) || methodName.startsWith(ClassFields.PREFIX_SET)) {
			// set or get
			name = ClassFields.unCapitalize(methodName.substring(3));
		}
		else {
			name = methodName;
		}
		return name;
	}
	
	/**
	 * undo string capitalization
	 * 
	 * @param input
	 *          any string
	 * @return the string uncapitalized (e.g. MyString -> myString)
	 */
	public static String unCapitalize(final String input) {
		String cap = null;
		if (input.length() == 0) {
			cap = "";
		}
		else {
			final char first = Character.toLowerCase(input.charAt(0));
			if (input.length() == 1) {
				cap = first + "";
			}
			else {
				cap = first + input.substring(1);
			}
		}
		return cap;
	}
	
	// VARIABLES
	
	/**
	 * SPECIAL METHOD: accesses the internal data<br/>
	 * Gets all the cached class properties objects related to this class<br/>
	 * WARNING: Special method which allows access to the internal properties
	 * storage structure,
	 * this should really only be used for filtering results in a way that is not
	 * supported by the
	 * standard class fields methods
	 * 
	 * @return the complete set of field properties objects for this class
	 */
	public Map<String, ClassProperty> getAllClassProperties() {
		return this.namesToProperties;
	}
	
	/**
	 * Gets the annotation from the represented class if one exists of the type
	 * specified
	 * 
	 * @param annotationType
	 *          the annotation type to look for on this class
	 * @return the annotation if found OR null if none found
	 */
	@SuppressWarnings({
		"unchecked",
		"hiding"
	})
	public <T extends Annotation> T getClassAnnotation(final Class<T> annotationType) {
		if (annotationType == null) {
			throw new IllegalArgumentException("annotationType must not be null");
		}
		T annote = null;
		final List<Annotation> annotations = this.classData.getAnnotations();
		for (final Annotation annotation : annotations) {
			if (annotationType.equals(annotation.annotationType())) {
				annote = (T) annotation;
				break;
			}
		}
		return annote;
	}
	
	/**
	 * Get all annotations present on the represented class
	 * 
	 * @return the set of all annotations on the class this refers to
	 */
	public Set<Annotation> getClassAnnotations() {
		return new HashSet<Annotation>(this.classData.getAnnotations());
	}
	
	/**
	 * SPECIAL METHOD: accesses the internal data<br/>
	 * Gets the internal cache object for the class which holds all fields,
	 * methods, constructors, and annotations
	 * 
	 * @return the class data cache object
	 */
	public ClassData<T> getClassData() {
		return this.classData;
	}
	
	/**
	 * SPECIAL METHOD: accesses the internal data<br/>
	 * Returns a field property object for any field (not limited to only complete
	 * fields)
	 * 
	 * @param name the fieldName
	 * @return the Property object which holds information about a field
	 * @throws FieldnameNotFoundException if this fieldName is invalid
	 */
	public ClassProperty getClassProperty(final String name) {
		final ClassProperty cp = this.getAnyPropertyOrFail(name);
		return cp;
	}
	
	/**
	 * @param annotationType
	 *          the annotation type to look for on this field
	 * @param name the fieldName
	 * @return the annotation if found OR null if none found
	 * @throws FieldnameNotFoundException if this fieldName is invalid
	 */
	@SuppressWarnings("hiding")
	public <T extends Annotation> T getFieldAnnotation(final Class<T> annotationType, final String name) {
		if (annotationType == null) {
			throw new IllegalArgumentException("annotationType must not be null");
		}
		final ClassProperty cp = this.getAnyPropertyOrFail(name);
		final T a = cp.getAnnotation(annotationType);
		return a;
	}
	
	/**
	 * Get all annotations on the field in the represented class
	 * 
	 * @param name the fieldName
	 * @return the set of all annotations on this field
	 * @throws FieldnameNotFoundException if this fieldName is invalid
	 */
	public Set<Annotation> getFieldAnnotations(final String name) {
		final ClassProperty cp = this.getAnyPropertyOrFail(name);
		final Collection<Annotation> annotes = cp.getAnnotationsCollection();
		final Set<Annotation> s = new HashSet<Annotation>(annotes);
		return s;
	}
	
	/**
	 * @return the class that this {@link ClassFields} object represents the
	 *         fields for
	 * @throws ClassLoadingException
	 *           if the class this refers to has been garbage collected
	 */
	public Class<T> getFieldClass() {
		return this.getStoredClass();
	}
	
	/**
	 * Will find the first field which has this annotation type
	 * 
	 * @param annotationType
	 *          the annotation type to look for on this field
	 * @return the name of the field which has this annotation OR null if none
	 *         found
	 */
	public String getFieldNameByAnnotation(final Class<? extends Annotation> annotationType) {
		if (annotationType == null) {
			throw new IllegalArgumentException("annotationType must not be null");
		}
		String fieldName = null;
		final Collection<ClassProperty> cps = this.namesToProperties.values();
		for (final ClassProperty classProperty : cps) {
			if (classProperty.getAnnotation(annotationType) != null) {
				fieldName = classProperty.getFieldName();
				break;
			}
		}
		return fieldName;
	}
	
	/**
	 * @return the list of field names <br/>
	 *         Only returns the complete
	 *         fields, the partial ones (only settable or gettable) will not be
	 *         returned
	 */
	public List<String> getFieldNames() {
		return this.getFieldNames(null);
	}
	
	/**
	 * Get the field names but filter the fields to return
	 * 
	 * @param filter (optional) indicates the fields to return the names for, can
	 *          be null for defaults
	 * @return the list of field names
	 */
	public List<String> getFieldNames(final FieldsFilter filter) {
		final List<String> names = new ArrayList<String>();
		for (final Entry<String, ClassProperty> entry : this.namesToProperties.getEntries()) {
			final String name = entry.getKey();
			final ClassProperty cp = entry.getValue();
			if (this.isFieldInFilter(cp, filter)) {
				names.add(name);
			}
		}
		return names;
	}
	
	/**
	 * Finds the names of all the fields with the given annotation type
	 * 
	 * @param annotationType
	 *          the annotation type to look for on this field
	 * @return the list of all field names sorted OR empty if none found
	 */
	public List<String> getFieldNamesWithAnnotation(final Class<? extends Annotation> annotationType) {
		if (annotationType == null) {
			throw new IllegalArgumentException("annotationType must not be null");
		}
		final HashSet<String> fieldNames = new HashSet<String>();
		final Collection<ClassProperty> cps = this.namesToProperties.values();
		for (final ClassProperty classProperty : cps) {
			if (classProperty.getAnnotation(annotationType) != null) {
				fieldNames.add(classProperty.getFieldName());
			}
		}
		final ArrayList<String> l = new ArrayList<String>(fieldNames);
		Collections.sort(l);
		return l;
	}
	
	// PUBLIC constructors
	
	/**
	 * @param name the fieldName
	 * @return the type of this field
	 * @throws FieldnameNotFoundException if this fieldName is invalid
	 */
	public Class<?> getFieldType(final String name) {
		final ClassProperty cp = this.getAnyPropertyOrFail(name);
		final Class<?> type = cp.getType();
		return type;
	}
	
	/**
	 * Get the types for fields in a class
	 * 
	 * @return the map of fieldName -> field type
	 */
	public Map<String, Class<?>> getFieldTypes() {
		return this.getFieldTypes(null);
	}
	
	/**
	 * Get the types for fields in a class but filter the fields to get the types
	 * for
	 * 
	 * @param filter (optional) indicates the fields to return the types for, can
	 *          be null for defaults
	 * @return the map of fieldName -> field type
	 */
	public Map<String, Class<?>> getFieldTypes(final FieldsFilter filter) {
		final OrderedMap<String, Class<?>> fieldTypes = new ArrayOrderedMap<String, Class<?>>();
		fieldTypes.setName(this.getStoredClass().getName());
		for (final Entry<String, ClassProperty> entry : this.namesToProperties.getEntries()) {
			final String name = entry.getKey();
			final ClassProperty cp = entry.getValue();
			if (this.isFieldInFilter(cp, filter)) {
				fieldTypes.put(name, cp.getType());
			}
		}
		return fieldTypes;
	}
	
	/**
	 * @return true if this has no fields, false otherwise (only includes complete
	 *         fields)
	 */
	public boolean isEmpty() {
		return this.size() <= 0;
	}
	
	/**
	 * @param filter (optional) indicates the fields to include in the check, can
	 *          be null for defaults
	 * @return true if this has no fields, false otherwise (only includes complete
	 *         fields)
	 */
	public boolean isEmpty(final FieldsFilter filter) {
		return this.size(filter) <= 0;
	}
	
	// PRIVATE analyzer methods
	
	/**
	 * @param cp the {@link ClassProperty} object which represents a field
	 * @param filter (optional) indicates the fields to return the names for, can
	 *          be null for defaults
	 * @return true if this field is in the given filter
	 */
	public boolean isFieldInFilter(final ClassProperty cp, final FieldsFilter filter) {
		boolean in = false;
		if (FieldsFilter.ALL.equals(filter)) {
			in = true;
		}
		else if (FieldsFilter.READABLE.equals(filter)) {
			if (this.isGettable(cp)) {
				in = true;
			}
		}
		else if (FieldsFilter.WRITEABLE.equals(filter)) {
			if (this.isSettable(cp)) {
				in = true;
			}
		}
		else if (FieldsFilter.SERIALIZABLE.equals(filter)) {
			if (this.isGettable(cp)) {
				if (!cp.isTransient()) {
					in = true;
				}
			}
		}
		else {
			// return complete
			if (this.isComplete(cp)) {
				in = true;
			}
		}
		return in;
	}
	
	/**
	 * Checks is this is a complete field
	 * 
	 * @param name the fieldName
	 * @return true if this fieldName is valid, false otherwise
	 */
	public boolean isFieldNameValid(final String name) {
		return this.isFieldNameValid(name, null);
	}
	
	/**
	 * Checks if a field is valid for the given filter
	 * 
	 * @param name the fieldName
	 * @param filter (optional) indicates the fields to include in the check, can
	 *          be null for defaults
	 * @return true if this fieldName is valid, false otherwise
	 */
	public boolean isFieldNameValid(final String name, final FieldsFilter filter) {
		boolean valid = this.namesToProperties.containsKey(name);
		if (valid) {
			// the field is real so check to see if it is in this filter
			final ClassProperty cp = this.getAnyPropertyOrFail(name);
			if (this.isFieldInFilter(cp, filter)) {
				valid = true;
			}
			else {
				valid = false;
			}
		}
		return valid;
	}
	
	/**
	 * @return the number of fields (only includes complete fields)
	 */
	public int size() {
		return this.size(null);
	}
	
	/**
	 * @param filter (optional) indicates the fields to include in the count, can
	 *          be null for defaults
	 * @return the number of fields
	 */
	public int size(final FieldsFilter filter) {
		int size = 0;
		if (FieldsFilter.ALL.equals(filter)) {
			size = this.namesToProperties.size();
		}
		else {
			size = this.getFieldNames(filter).size();
		}
		return size;
	}
	
	@Override
	public String toString() {
		return "(" + this.getStoredClass().getName() + "):mode=" + this.fieldFindMode.name() + ":" + this.getFieldNames(FieldsFilter.ALL);
	}
	
	/**
	 * @param member any class member
	 * @return true if static, false otherwise
	 */
	protected boolean isStatic(final Member member) {
		if (Modifier.isStatic(member.getModifiers())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks a getter and setter pair and returns the proper property name
	 * 
	 * @param getter
	 * @param setter
	 * @return the property name for this getter and setter pair
	 */
	private String checkPropertyMethods(final Method getter, final Method setter) {
		String name = null;
		final String gName = getter.getName();
		Class<?> gType = null;
		final Class<?>[] paramTypes = getter.getParameterTypes();
		if (paramTypes.length == 0) {
			if (ClassFields.isGetClassMethod(getter)) {
				throw new GetClassMethodException();
			}
			else if (gName.startsWith(ClassFields.PREFIX_GET) || gName.startsWith(ClassFields.PREFIX_IS)) {
				gType = getter.getReturnType();
				if (gType != null) {
					name = ClassFields.makeFieldNameFromMethod(gName);
				}
			}
		}
		if (name != null) {
			boolean validSetter = false;
			final String sName = setter.getName();
			Class<?> sType = null;
			if (sName.startsWith(ClassFields.PREFIX_SET)) {
				final Class<?>[] sParamTypes = setter.getParameterTypes();
				if (sParamTypes.length == 1) {
					sType = sParamTypes[0];
					if ((sType == null) || !sType.isAssignableFrom(gType)) {
						throw new IllegalArgumentException("setter (" + sName + ") type (" + sType + ") and " + "getter (" + gName
																								+ ") type (" + gType + ") do not match, they MUST match");
					}
					else {
						validSetter = true;
					}
				}
			}
			if (!validSetter) {
				throw new IllegalArgumentException("setter (" + sName + ") does not appear to be a valid setter method "
																						+ "(single param, name starts with set)");
			}
		}
		else {
			throw new IllegalArgumentException("getter (" + gName + ") does not appear to be a valid getter method "
																					+ "(0 params, name starts with get or is)");
		}
		return name;
	}
	
	/**
	 * Simple manual method for walking over a class and finding the bean
	 * properties, generally used
	 * because the sun Introspector is so slow, note that this will also return
	 * all the non-matching
	 * getters or setters if requested
	 * 
	 * @param fieldClass
	 *          the class to reflect over
	 * @param includePartial
	 *          if true then includes getters and setters that do not match, if
	 *          false then ONLY
	 *          include the getters and setters which match exactly
	 * @return a list Property objects
	 */
	private List<ClassProperty> findProperties(final boolean includePartial) {
		// find the property methods from all public methods
		final Map<String, ClassProperty> propertyMap = new ArrayOrderedMap<String, ClassProperty>();
		for (final Method method : this.classData.getPublicMethods()) {
			final Class<?>[] paramTypes = method.getParameterTypes();
			final String name = method.getName();
			if (!this.includeStaticFields && this.isStatic(method)) {
				continue; // skip statics
			}
			else if (ClassFields.isGetClassMethod(method) && !this.includeClassField) {
				continue; // skip class field
			}
			else if (paramTypes.length == 0) {
				// no params
				if (name.startsWith(ClassFields.PREFIX_GET) || name.startsWith(ClassFields.PREFIX_IS)) {
					// getter
					final Class<?> returnType = method.getReturnType();
					if (returnType != null) {
						try {
							final String fieldName = ClassFields.makeFieldNameFromMethod(method.getName());
							ClassProperty p = null;
							if (propertyMap.containsKey(fieldName)) {
								p = propertyMap.get(fieldName);
								p.setGetter(method);
							}
							else {
								p = new ClassProperty(fieldName, method, null);
								propertyMap.put(fieldName, p);
							}
						}
						catch (final Exception e) {
							// nothing to do here but move on
						}
					}
				}
			}
			else if (paramTypes.length == 1) {
				if (name.startsWith(ClassFields.PREFIX_SET)) {
					// setter (one param)
					final String fieldName = ClassFields.makeFieldNameFromMethod(method.getName());
					ClassProperty p = null;
					if (propertyMap.containsKey(fieldName)) {
						p = propertyMap.get(fieldName);
						p.setSetter(method);
					}
					else {
						p = new ClassProperty(fieldName, null, method);
						propertyMap.put(fieldName, p);
					}
				}
			}
		}
		final List<ClassProperty> properties = new ArrayList<ClassProperty>();
		for (final Entry<String, ClassProperty> entry : propertyMap.entrySet()) {
			final ClassProperty p = entry.getValue();
			if (p != null) {
				if (!this.includeStaticFields && p.isStatic()) {
					continue; // skip any props with static fields
				}
				if (this.ignoredFieldNames.contains(p.getFieldName())) {
					continue; // skip ignored names
				}
				if (includePartial) {
					// put all of them in
					properties.add(p);
				}
				else {
					// filter down to only the one which are complete
					if (p.isGettable() && p.isSettable()) {
						properties.add(p);
					}
				}
			}
		}
		return properties;
	}
	
	/**
	 * @param name
	 * @return
	 */
	private ClassProperty getAnyPropertyOrFail(final String name) {
		final ClassProperty cp = this.namesToProperties.get(name);
		if (cp == null) {
			throw new FieldnameNotFoundException(name);
		}
		return cp;
	}
	
	private Class<T> getStoredClass() {
		return this.classData.getType();
	}
	
	/**
	 * Determines if a {@link ClassProperty} is complete based on the
	 * {@link FieldFindMode} setting
	 * 
	 * @param cp a class property setting
	 * @return true if it is complete, false otherwise
	 */
	private boolean isComplete(final ClassProperty cp) {
		boolean complete = false;
		if (FieldFindMode.FIELD.equals(this.fieldFindMode)) {
			if (cp.isField()) {
				complete = true;
			}
		}
		else if (FieldFindMode.PROPERTY.equals(this.fieldFindMode)) {
			if (cp.isProperty()) {
				complete = true;
			}
		}
		else {
			// default
			if (cp.isComplete()) {
				complete = true;
			}
		}
		return complete;
	}
	
	/**
	 * Determines if a {@link ClassProperty} is gettable based on the
	 * {@link FieldFindMode} setting
	 * 
	 * @param cp a class property setting
	 * @return true if it is gettable, false otherwise
	 */
	private boolean isGettable(final ClassProperty cp) {
		boolean gettable = false;
		if (FieldFindMode.FIELD.equals(this.fieldFindMode)) {
			if (cp.isField() && cp.isGettable()) {
				gettable = true;
			}
		}
		else if (FieldFindMode.PROPERTY.equals(this.fieldFindMode)) {
			if (cp.isProperty() && cp.isGettable()) {
				gettable = true;
			}
		}
		else {
			// default
			if (cp.isPublicGettable()) {
				gettable = true;
			}
		}
		return gettable;
	}
	
	// STATIC methods
	
	/**
	 * Determines if a {@link ClassProperty} is settable based on the
	 * {@link FieldFindMode} setting
	 * 
	 * @param cp a class property setting
	 * @return true if it is settable, false otherwise
	 */
	private boolean isSettable(final ClassProperty cp) {
		boolean settable = false;
		if (FieldFindMode.FIELD.equals(this.fieldFindMode)) {
			if (cp.isField() && cp.isSettable()) {
				settable = true;
			}
		}
		else if (FieldFindMode.PROPERTY.equals(this.fieldFindMode)) {
			if (cp.isProperty() && cp.isSettable()) {
				settable = true;
			}
		}
		else {
			// default
			if (cp.isPublicSettable()) {
				settable = true;
			}
		}
		return settable;
	}
	
	/**
	 * We need to find all the class, method, and field annotations and not just
	 * the ones on public
	 * methods so we are going through everything on this class and all the super
	 * classes<br/>
	 * WARNING: this is NOT cheap and the results need to be cached badly<br/>
	 * NOTE: This also populates all the fields in every property and checks and
	 * removes
	 * all staics or excluded fields if any slipped through the previous processes
	 * 
	 * @param fieldClass
	 *          the class to analyze
	 */
	private void populateAnnotationsFields() {
		// get the annotations from all class methods
		for (final Method method : this.classData.getMethods()) {
			final int paramCount = method.getParameterTypes().length;
			// only include annotations on methods which *might* be fields (no args, 1
			// arg, or 2 args for the special ones)
			if (paramCount <= 2) {
				try {
					final String name = ClassFields.makeFieldNameFromMethod(method.getName());
					final ClassProperty cp = this.getAnyPropertyOrFail(name);
					final Annotation[] annotations = method.getAnnotations();
					for (final Annotation annotation : annotations) {
						if (annotation != null) {
							cp.addAnnotation(annotation);
						}
					}
				}
				catch (final FieldnameNotFoundException e) {
					// nothing to do but keep going
				}
			}
		}
		// get the annotations from all class fields
		for (final Field field : this.classData.getFields()) {
			try {
				final String fieldName = field.getName();
				if (!this.includeStaticFields && this.isStatic(field)) {
					this.namesToProperties.remove(fieldName); // as a final check, take this
																								// out if it is in there
					continue; // skip statics
				}
				if (this.ignoredFieldNames.contains(fieldName)) {
					this.namesToProperties.remove(fieldName); // as a final check, take this
																								// out if it is in there
					continue; // skip ignored names
				}
				final ClassProperty cp = this.getAnyPropertyOrFail(fieldName);
				if (cp.getField() == null) {
					cp.setField(field); // ensure there is a field set for all methods
				}
				final Annotation[] annotations = field.getAnnotations();
				for (final Annotation annotation : annotations) {
					if (annotation != null) {
						cp.addAnnotation(annotation);
					}
				}
			}
			catch (final FieldnameNotFoundException e) {
				// nothing to do but keep going
			}
		}
		// now mark the transient fields as such
		for (final String fieldName : this.transientFieldNames) {
			try {
				final ClassProperty cp = this.getAnyPropertyOrFail(fieldName);
				cp.transientField = true;
			}
			catch (final FieldnameNotFoundException e) {
				// nothing to do but keep going
			}
		}
	}
	
	/**
	 * Populates the fields from the class data object
	 * 
	 * @param includeNonPublic if true then all fields will be used, if false then
	 *          only public fields
	 */
	private void populateFields(final boolean includeNonPublic) {
		// set the fields
		List<Field> fields = null;
		if (includeNonPublic) {
			fields = this.classData.getFields();
		}
		else {
			fields = this.classData.getPublicFields();
		}
		for (final Field field : fields) {
			if (!this.includeStaticFields && this.isStatic(field)) {
				continue; // skip statics
			}
			final String fieldName = field.getName();
			if (this.ignoredFieldNames.contains(fieldName)) {
				continue; // skip ignored names
			}
			if (!this.namesToProperties.containsKey(fieldName)) {
				final ClassProperty p = new ClassProperty(fieldName, field);
				this.namesToProperties.put(fieldName, p);
			}
		}
	}
	
	/**
	 * @param publicFields
	 *          takes an array of fields and populates the internal storage
	 *          mechanisms
	 */
	private void populateFields(final Field[] publicFields) {
		// set the fields
		for (int i = 0; i < publicFields.length; i++) {
			if (publicFields[i] != null) {
				final String fieldName = publicFields[i].getName();
				if (!this.namesToProperties.containsKey(fieldName)) {
					final ClassProperty p = new ClassProperty(fieldName, publicFields[i]);
					this.namesToProperties.put(fieldName, p);
				}
			}
		}
	}
	
	// INNER classes
	
	/**
	 * Populate the internal storage from an array of descriptors
	 * 
	 * @param descriptors
	 */
	private void populateProperties(final PropertyDescriptor[] descriptors) {
		for (int i = 0; i < descriptors.length; i++) {
			final PropertyDescriptor pd = descriptors[i];
			final String fieldName = pd.getName();
			if (!this.namesToProperties.containsKey(fieldName)) {
				final Method getter = pd.getReadMethod();
				final Method setter = pd.getWriteMethod();
				if ((getter != null) && (setter != null)) {
					ClassProperty p = null;
					if (pd instanceof IndexedPropertyDescriptor) {
						final IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
						// Class<?> iType = ipd.getIndexedPropertyType();
						final Method iGetter = ipd.getIndexedReadMethod();
						final Method iSetter = ipd.getIndexedWriteMethod();
						if ((iGetter != null) && (iSetter != null)) {
							p = new ClassProperty.IndexedProperty(fieldName, getter, setter, iGetter, iSetter);
						}
					}
					if (p == null) {
						p = new ClassProperty(fieldName, getter, setter);
					}
					this.namesToProperties.put(fieldName, p);
				}
			}
		}
	}
	
}
