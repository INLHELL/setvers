/**
 * $Id: ScalarConverter.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/converters/ScalarConverter.java $
 * ScalarConverter.java - genericdao - Sep 10, 2008 3:53:32 PM - azeckoski
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

package org.azeckoski.reflectutils.converters;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ConversionUtils;
import org.azeckoski.reflectutils.DeepUtils;
import org.azeckoski.reflectutils.converters.api.VariableConverter;

/**
 * This is a special variable converter designed to handle the special case of
 * converting
 * from a non-scalar (collection, array, list, etc.) to a scalar object
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ScalarConverter implements VariableConverter {
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.converters.api.VariableConverter#canConvert(
	 * java.lang.Object, java.lang.Class)
	 */
	@Override
	public boolean canConvert(final Object value, final Class<?> toType) {
		boolean convertible = false;
		if (value != null) {
			final Class<?> fromType = value.getClass();
			if (ConstructorUtils.isClassObjectHolder(fromType)) {
				// converting from a non-scalar
				if (!ConstructorUtils.isClassObjectHolder(toType)) {
					// to a scalar
					convertible = true;
				}
			}
		}
		return convertible;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.azeckoski.reflectutils.converters.api.VariableConverter#convert(java
	 * .lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(final Object value, final Class<T> toType) {
		// we know that fromType is a holder and toType is a scalar
		T convert = null;
		final Class<?> fromType = value.getClass();
		Object toConvert = value;
		
		if (ConstructorUtils.isClassArray(fromType)) {
			// from array
			final int length = Array.getLength(toConvert);
			if (length > 0) {
				final Class<?> componentType = fromType.getComponentType();
				if (String.class.equals(toType) && ConstructorUtils.isClassSimple(componentType)) {
					return (T) ArrayUtils.arrayToString((Object[]) value);
				}
				else {
					// get the first one
					toConvert = Array.get(toConvert, 0);
				}
			}
			else {
				// empty so use empty string
				toConvert = "";
			}
		}
		else if (ConstructorUtils.isClassCollection(fromType)) {
			// from collection
			final Collection<?> collection = (Collection) toConvert;
			final int length = collection.size();
			// to scalar
			if (length > 0) {
				// get the first one (random)
				toConvert = collection.iterator().next();
			}
			else {
				// empty so use empty string
				toConvert = "";
			}
		}
		else if (ConstructorUtils.isClassMap(fromType)) {
			// from map
			final Map map = (Map) toConvert;
			final int length = map.size();
			// to scalar
			if (length > 0) {
				// check if the keys are strings and the toType is non-simple
				boolean stringKeys = false;
				for (final Object key : map.keySet()) {
					if (String.class.equals(key.getClass())) {
						stringKeys = true;
					}
					else {
						stringKeys = false;
						break;
					}
				}
				if (stringKeys && !ConstructorUtils.isClassSimple(toType)) {
					// this is a bean so populate it with the map data
					convert = this.getConstructorUtils().constructClass(toType);
					this.getDeepUtils().populate(convert, map); // put the values from the map
																									// into the object
					return convert; // EXIT
				}
				else {
					// just get the first one (random)
					toConvert = map.values().iterator().next();
				}
			}
			else {
				// empty so use empty string
				toConvert = "";
			}
		}
		else {
			// should not happen
			throw new IllegalArgumentException(
				"Failure converting to scalar value, the given input does not seem to be an object holder (" + fromType + "): "
					+ value);
		}
		
		// now convert the object from the holder
		convert = this.getConversionUtils().convert(toConvert, toType);
		return convert;
	}
	
	protected ConstructorUtils getConstructorUtils() {
		return ConstructorUtils.getInstance();
	}
	
	protected ConversionUtils getConversionUtils() {
		return ConversionUtils.getInstance();
	}
	
	protected DeepUtils getDeepUtils() {
		return DeepUtils.getInstance();
	}
	
}
