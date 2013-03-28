/**
 * $Id: DynaBeanAdapter.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/beanutils/DynaBeanAdapter.java $
 * DynaBeanAdapter.java - genericdao - Sep 20, 2008 10:08:01 AM - azeckoski
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;

/**
 * This allows dynabeans to work with the field utils,
 * should only be loaded by reflection if the DynaBean class can be found
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DynaBeanAdapter implements FieldAdapter {
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getFieldType(java.lang
	 * .Object, java.lang.String)
	 */
	@Override
	public Class<?> getFieldType(final Object obj, final String name) {
		final DynaClass dynaClass = ((DynaBean) obj).getDynaClass();
		final DynaProperty dynaProperty = dynaClass.getDynaProperty(name);
		if (dynaProperty == null) {
			throw new FieldnameNotFoundException("DynaBean: Could not find this fieldName (" + name
																						+ ") on the target object: " + obj, name, null);
		}
		return dynaProperty.getType();
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getFieldValues(java.lang
	 * .Object, org.azeckoski.reflectutils.ClassFields.FieldsFilter)
	 */
	@Override
	public Map<String, Object> getFieldValues(final Object obj, final FieldsFilter filter) {
		final Map<String, Object> values = new HashMap<String, Object>();
		final DynaProperty[] descriptors = ((DynaBean) obj).getDynaClass().getDynaProperties();
		for (int i = 0; i < descriptors.length; i++) {
			final String name = descriptors[i].getName();
			// cannot filter the values for dynabeans -AZ
			final Object o = this.getSimpleValue(obj, name);
			values.put(name, o);
		}
		return values;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getIndexedValue(java.
	 * lang.Object, java.lang.String, int)
	 */
	@Override
	public Object getIndexedValue(final Object obj, final String name, final int index) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		final Object value = ((DynaBean) obj).get(name, index);
		return value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getMappedValue(java.lang
	 * .Object, java.lang.String, java.lang.String)
	 */
	@Override
	public Object getMappedValue(final Object obj, final String name, final String key) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		final Object value = ((DynaBean) obj).get(name, key);
		return value;
	}
	
	@Override
	public List<String> getPropertyNames(final Object bean) {
		final List<String> names = new ArrayList<String>();
		final DynaProperty origDescriptors[] = ((DynaBean) bean).getDynaClass().getDynaProperties();
		for (final DynaProperty dynaProperty : origDescriptors) {
			final String name = dynaProperty.getName();
			names.add(name);
		}
		return names;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#getSimpleValue(java.lang
	 * .Object, java.lang.String)
	 */
	@Override
	public Object getSimpleValue(final Object obj, final String name) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		final Object value = (((DynaBean) obj).get(name));
		return value;
	}
	
	@Override
	public boolean isAdaptableClass(final Class<?> beanClass) {
		boolean adaptable = false;
		if (DynaBean.class.isAssignableFrom(beanClass)) {
			adaptable = true;
		}
		return adaptable;
	}
	
	@Override
	public boolean isAdaptableObject(final Object obj) {
		boolean adaptable = false;
		if (obj instanceof DynaBean) {
			adaptable = true;
		}
		return adaptable;
	}
	
	@Override
	public Object newInstance(final Object bean) {
		try {
			return ((DynaBean) bean).getDynaClass().newInstance();
		}
		catch (final Exception e) {
			throw new RuntimeException("Could not instantiate DynaBean: " + bean, e);
		} // make new dynabean
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setIndexedValue(java.
	 * lang.Object, java.lang.String, int, java.lang.Object)
	 */
	@Override
	public void setIndexedValue(final Object obj, final String name, final int index, final Object value) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		((DynaBean) obj).set(name, index, value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setMappedValue(java.lang
	 * .Object, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setMappedValue(final Object obj, final String name, final String key, final Object value) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		((DynaBean) obj).set(name, key, value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.beanutils.FieldAdapter#setSimpleValue(java.lang
	 * .Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setSimpleValue(final Object obj, final String name, final Object value) {
		final DynaProperty descriptor = ((DynaBean) obj).getDynaClass().getDynaProperty(name);
		if (descriptor == null) {
			throw new FieldnameNotFoundException(name);
		}
		((DynaBean) obj).set(name, value);
	}
	
}
