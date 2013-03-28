/**
 * $Id: FieldAdapterManager.java 54 2009-05-18 15:29:27Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/beanutils/FieldAdapterManager.java $
 * FieldAdapterManager.java - reflectutils - Oct 2, 2008 10:31:44 PM - azeckoski
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

import org.azeckoski.reflectutils.ClassLoaderUtils;

/**
 * This manager ensures that the field adapter keeps working even if the class
 * it is adapting goes away
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class FieldAdapterManager {
	
	public static final String DYNABEAN_ADAPTER = "DynaBeanAdapter";
	public static final String DYNABEAN_CLASSNAME = "org.apache.commons.beanutils.DynaBean";
	
	protected FieldAdapter fieldAdapter;
	
	public FieldAdapterManager() {
		/*
		 * loads up the field adapter if the right classes exist,
		 * this allows us to support DynaBeans even though they may not be available
		 */
		final Class<?> dynaBean = ClassLoaderUtils.getClassFromString(FieldAdapterManager.DYNABEAN_CLASSNAME);
		if (dynaBean != null) {
			// assumes the adapter impl is in the same path as the interface
			String path = FieldAdapter.class.getName();
			path = path.replace(FieldAdapter.class.getSimpleName(), FieldAdapterManager.DYNABEAN_ADAPTER);
			final Class<?> adapterClass = ClassLoaderUtils.getClassFromString(path);
			if (adapterClass == null) {
				System.err.println("WARN: Could not find adapter class: " + path
														+ ", will continue without the dynabean adapter");
				// throw new IllegalStateException("Could not find adapter class: " +
				// DYNABEAN_ADAPTER);
			}
			else {
				// found the adapter class
				try {
					this.fieldAdapter = (FieldAdapter) adapterClass.newInstance();
				}
				catch (final Exception e) {
					System.err.println("WARN: Failed to instantiate field adapter (" + adapterClass
															+ "), will continue without the dynabean adapter: " + e);
					// throw new
					// RuntimeException("Failed to instantiate field adapter ("+adapterClass+"): "+e,
					// e);
				}
			}
		}
		else {
			// no dynabean so use default
			this.fieldAdapter = new DefaultFieldAdapter();
		}
		if (this.fieldAdapter == null) {
			// failure with dynabean so use default
			this.fieldAdapter = new DefaultFieldAdapter();
		}
	}
	
	public FieldAdapter getFieldAdapter() {
		return this.fieldAdapter;
	}
	
	public boolean isAdaptableClass(final Class<?> beanClass) {
		boolean adaptable = false;
		try {
			adaptable = this.fieldAdapter.isAdaptableClass(beanClass);
		}
		catch (final NoClassDefFoundError e) {
			// invalid field adapter, back to the default one
			this.fieldAdapter = new DefaultFieldAdapter();
			adaptable = false;
		}
		return adaptable;
	}
	
	public boolean isAdaptableObject(final Object obj) {
		boolean adaptable = false;
		try {
			adaptable = this.fieldAdapter.isAdaptableObject(obj);
		}
		catch (final NoClassDefFoundError e) {
			// invalid field adapter, back to the default one
			this.fieldAdapter = new DefaultFieldAdapter();
			adaptable = false;
		}
		return adaptable;
	}
	
}
