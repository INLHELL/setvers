/**
 * $Id: ClassDataCacher.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/ClassDataCacher.java $
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
import java.util.Map;

import org.azeckoski.reflectutils.ClassFields.FieldFindMode;
import org.azeckoski.reflectutils.refmap.ReferenceMap;
import org.azeckoski.reflectutils.refmap.ReferenceType;

/**
 * Class which provides access to the analysis objects and the cached reflection
 * data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ClassDataCacher {
	
	// CONSTRUCTORS
	
	protected static SoftReference<ClassDataCacher> instanceStorage;
	
	private static int timesCreated = 0;
	
	public int cacheHits = 0;
	
	// class fields
	
	public int cacheMisses = 0;
	
	public int lookups = 0;
	
	protected FieldFindMode fieldFindMode = FieldFindMode.HYBRID;
	
	protected boolean includeClassField = false;
	
	@SuppressWarnings("unchecked")
	protected Map<Class<?>, ClassFields> reflectionCache = null;
	
	private boolean singleton = false;
	
	/**
	 * Construct and specify a mode for looking up fields which does not match the
	 * default: {@link FieldFindMode#HYBRID}
	 * 
	 * @param fieldFindMode the mode when looking up fields in classes <br/>
	 *          <b>WARNING:</b> if you don't need this control then just use the
	 *          {@link #getInstance()} method to get this
	 */
	public ClassDataCacher(final FieldFindMode fieldFindMode) {
		this(fieldFindMode, null);
	}
	
	/**
	 * Construct and specify the field finding mode and your own cache when
	 * caching class data, must implement the standard map interface but
	 * only the following methods are required:<br/>
	 * {@link Map#clear()}, {@link Map#size()}, {@link Map#put(Object, Object)},
	 * {@link Map#get(Object)} <br/>
	 * <br/>
	 * <b>WARNING:</b> if you don't need this control then just use the
	 * {@link #getInstance()} method to get this
	 * 
	 * @param fieldFindMode the mode when looking up fields in classes (null for
	 *          default of {@link FieldFindMode#HYBRID})
	 * @param reflectionCache a map implementation to use as the cache mechanism
	 *          (null to use internal)
	 */
	@SuppressWarnings("unchecked")
	public ClassDataCacher(final FieldFindMode fieldFindMode, final Map<Class<?>, ClassFields> reflectionCache) {
		this.setFieldFindMode(fieldFindMode);
		this.setReflectionCache(reflectionCache);
		
		ClassDataCacher.setInstance(this);
	}
	
	/**
	 * default constructor - protected
	 */
	protected ClassDataCacher() {
		this(null, null);
	}
	
	/**
	 * Get a singleton instance of this class to work with (stored statically) <br/>
	 * <b>WARNING</b>: do not hold onto this object or cache it yourself, call
	 * this method again if you need it again
	 * 
	 * @return a singleton instance of this class
	 */
	public static ClassDataCacher getInstance() {
		ClassDataCacher instance = (ClassDataCacher.instanceStorage == null ? null : ClassDataCacher.instanceStorage.get());
		if (instance == null) {
			instance = ClassDataCacher.setInstance(null);
		}
		return instance;
	}
	
	public static int getTimesCreated() {
		return ClassDataCacher.timesCreated;
	}
	
	/**
	 * Set the singleton instance of the class which will be stored statically
	 * 
	 * @param instance the instance to use as the singleton instance
	 */
	public static ClassDataCacher setInstance(final ClassDataCacher newInstance) {
		ClassDataCacher instance = newInstance;
		if (instance == null) {
			instance = new ClassDataCacher();
			instance.singleton = true;
		}
		ClassDataCacher.timesCreated++;
		ClassDataCacher.instanceStorage = new SoftReference<ClassDataCacher>(instance);
		return instance;
	}
	
	/**
	 * Clears all cached objects
	 */
	public void clear() {
		this.getReflectionCache().clear();
	}
	
	/**
	 * Convenience Method: <br/>
	 * Gets the class data object which contains information about this class,
	 * will retrieve this from the class data cache if available or generate it if
	 * not<br/>
	 * This is also available from the {@link ClassFields} object
	 * 
	 * @param <T>
	 * @param cls any {@link Class}
	 * @return the class data cache object (contains reflected data from this
	 *         class)
	 */
	public <T> ClassData<T> getClassData(final Class<T> cls) {
		final ClassFields<T> cf = this.getClassFields(cls);
		return cf.getClassData();
	}
	
	/**
	 * Convenience Method: <br/>
	 * Gets the class data object which contains information about this objects
	 * class,
	 * will retrieve this from the class data cache if available or generate it if
	 * not<br/>
	 * This is also available from the {@link ClassFields} object
	 * 
	 * @param <T>
	 * @param obj any {@link Object}
	 * @return the raw ClassData cache object which contains reflection data about
	 *         this objects class
	 * @throws IllegalArgumentException if obj is null
	 */
	public <T> ClassData<T> getClassData(final Object obj) {
		final ClassFields<T> cf = this.getClassFieldsFromObject(obj);
		return cf.getClassData();
	}
	
	/**
	 * Get the class fields analysis of a class which contains information about
	 * that class and its fields,
	 * includes annotations, fields/properties, etc. packaged in a way which makes
	 * the data easy to get to,
	 * use the {@link ClassData} object to get to the more raw data
	 * 
	 * @param <T>
	 * @param cls any {@link Class}
	 * @return the ClassFields analysis object which contains the information
	 *         about this class
	 */
	@SuppressWarnings("unchecked")
	public <T> ClassFields<T> getClassFields(final Class<T> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("cls (type) cannot be null");
		}
		this.lookups++;
		ClassFields<T> cf = this.getReflectionCache().get(cls);
		if (cf == null) {
			// make new and put in cache
			cf = new ClassFields<T>(cls, FieldFindMode.HYBRID, false, this.includeClassField);
			this.getReflectionCache().put(cls, cf);
			this.cacheMisses++;
		}
		else {
			this.cacheHits++;
		}
		return cf;
	}
	
	/**
	 * Convenience Method: <br/>
	 * Analyze an object and produce an object which contains information about it
	 * and its fields,
	 * see {@link ClassDataCacher#getClassData(Class)}
	 * 
	 * @param <T>
	 * @param obj any {@link Object}
	 * @return the ClassFields analysis object which contains the information
	 *         about this objects class
	 * @throws IllegalArgumentException if obj is null
	 */
	@SuppressWarnings("unchecked")
	public <T> ClassFields<T> getClassFieldsFromObject(final Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null");
		}
		final Class<T> cls = (Class<T>) obj.getClass();
		return this.getClassFields(cls);
	}
	
	public FieldFindMode getFieldFindMode() {
		return this.fieldFindMode;
	}
	
	public boolean isIncludeClassField() {
		return this.includeClassField;
	}
	
	// STATIC access
	
	public final boolean isSingleton() {
		return this.singleton;
	}
	
	/**
	 * Set the mode used to find fields on classes (default
	 * {@link FieldFindMode#HYBRID}) <br/>
	 * <b>WARNING</b>: changing modes will clear the existing cache
	 * 
	 * @param fieldFindMode see FieldFindMode enum for details
	 * @see FieldFindMode
	 */
	public void setFieldFindMode(FieldFindMode fieldFindMode) {
		if (fieldFindMode == null) {
			fieldFindMode = FieldFindMode.HYBRID;
		}
		if (!this.fieldFindMode.equals(fieldFindMode)) {
			// need to clear the cache if we change the mode
			this.getReflectionCache().clear();
		}
		this.fieldFindMode = fieldFindMode;
	}
	
	/**
	 * Setting to determine if the result of "getClass()" should be included in
	 * the reflection data <br/>
	 * <b>WARNING</b>: changing this will clear the existing cache
	 * 
	 * @param includeClassField if true then getClass() will be treated as a
	 *          readable field called "class", default is false
	 */
	public void setIncludeClassField(final boolean includeClassField) {
		if (this.includeClassField != includeClassField) {
			// need to clear the cache if we change this
			this.getReflectionCache().clear();
		}
		this.includeClassField = includeClassField;
	}
	
	/**
	 * Set the cache to be used for holding the reflection data,
	 * this allows control over where the reflection caches are stored,
	 * this should store the data in a way that it will not hold open the
	 * classloader the class comes from <br/>
	 * Note that you can set this to a map implementation which does not store
	 * anything to disable caching if you like
	 * 
	 * @param reflectionCache a cache for holding class cache data (implements
	 *          map), null to use the default internal cache
	 */
	@SuppressWarnings("unchecked")
	public void setReflectionCache(final Map<Class<?>, ClassFields> reflectionCache) {
		if (reflectionCache != null) {
			this.reflectionCache.clear();
			this.reflectionCache = reflectionCache;
		}
		else {
			this.getReflectionCache();
		}
	}
	
	/**
	 * @return the size of the cache (number of cached {@link ClassFields}
	 *         entries)
	 */
	public int size() {
		return this.getReflectionCache().size();
	}
	
	@Override
	public String toString() {
		return "Cache::c=" + ClassDataCacher.timesCreated + ":s=" + this.singleton + ":fieldMode=" + this.fieldFindMode + ":lookups="
						+ this.lookups + "::cache:hits=" + this.cacheHits + ":misses=" + this.cacheMisses + ":size=" + this.size() + ":singleton="
						+ this.singleton;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<Class<?>, ClassFields> getReflectionCache() {
		if (this.reflectionCache == null) {
			// internally we are using the ReferenceMap (from the Guice codebase)
			// modeled after the groovy reflection caching (weak -> soft)
			this.reflectionCache = new ReferenceMap<Class<?>, ClassFields>(ReferenceType.WEAK, ReferenceType.SOFT);
		}
		return this.reflectionCache;
	}
	
}
