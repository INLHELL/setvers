/**
 * $Id: DefaultFieldAdapter.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/beanutils/DefaultFieldAdapter.java $
 * DefaultFieldAdapter.java - genericdao - Sep 20, 2008 10:38:59 AM - azeckoski
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

package org.azeckoski.reflectutils.beanutils;

import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.ClassFields.FieldsFilter;

/**
 * Does nothing but implement with the defaults, used when the normal adapter is
 * not available
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DefaultFieldAdapter implements FieldAdapter {
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getFieldType(java.lang
	 * .Object, java.lang.String)
	 */
	@Override
	public Class<?> getFieldType(final Object obj, final String name) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getFieldValues(java.lang
	 * .Object, org.azeckoski.reflectutils.ClassFields.FieldsFilter)
	 */
	@Override
	public Map<String, Object> getFieldValues(final Object obj, final FieldsFilter filter) {
		return null;
	}
	
	// NOTE: nothing below here should ever get called
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getIndexedValue(java.
	 * lang.Object, java.lang.String, int)
	 */
	@Override
	public Object getIndexedValue(final Object obj, final String name, final int index) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getMappedValue(java.lang
	 * .Object, java.lang.String, java.lang.String)
	 */
	@Override
	public Object getMappedValue(final Object obj, final String name, final String key) {
		return null;
	}
	
	@Override
	public List<String> getPropertyNames(final Object bean) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getSimpleValue(java.lang
	 * .Object, java.lang.String)
	 */
	@Override
	public Object getSimpleValue(final Object obj, final String name) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#isAdaptableClass(java
	 * .lang.Class)
	 */
	@Override
	public boolean isAdaptableClass(final Class<?> beanClass) {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#isAdaptableObject(java
	 * .lang.Object)
	 */
	@Override
	public boolean isAdaptableObject(final Object obj) {
		return false;
	}
	
	@Override
	public Object newInstance(final Object bean) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setIndexedValue(java.
	 * lang.Object, java.lang.String, int, java.lang.Object)
	 */
	@Override
	public void setIndexedValue(final Object obj, final String name, final int index, final Object value) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setMappedValue(java.lang
	 * .Object, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setMappedValue(final Object obj, final String name, final String key, final Object value) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setSimpleValue(java.lang
	 * .Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setSimpleValue(final Object obj, final String name, final Object value) {
	}
	
}
