/**
 * $Id: ConversionUtils.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/ConversionUtils.java $
 * ConversionUtils.java - genericdao - May 19, 2008 10:10:15 PM - azeckoski
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

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.azeckoski.reflectutils.converters.ArrayConverter;
import org.azeckoski.reflectutils.converters.BigDecimalConverter;
import org.azeckoski.reflectutils.converters.BigIntegerConverter;
import org.azeckoski.reflectutils.converters.BooleanConverter;
import org.azeckoski.reflectutils.converters.ByteConverter;
import org.azeckoski.reflectutils.converters.CalendarConverter;
import org.azeckoski.reflectutils.converters.CharacterConverter;
import org.azeckoski.reflectutils.converters.ClassConverter;
import org.azeckoski.reflectutils.converters.CollectionConverter;
import org.azeckoski.reflectutils.converters.DateConverter;
import org.azeckoski.reflectutils.converters.DoubleConverter;
import org.azeckoski.reflectutils.converters.EnumConverter;
import org.azeckoski.reflectutils.converters.FileConverter;
import org.azeckoski.reflectutils.converters.FloatConverter;
import org.azeckoski.reflectutils.converters.IntegerConverter;
import org.azeckoski.reflectutils.converters.LongConverter;
import org.azeckoski.reflectutils.converters.MapConverter;
import org.azeckoski.reflectutils.converters.NumberConverter;
import org.azeckoski.reflectutils.converters.SQLDateConverter;
import org.azeckoski.reflectutils.converters.SQLTimeConverter;
import org.azeckoski.reflectutils.converters.ScalarConverter;
import org.azeckoski.reflectutils.converters.ShortConverter;
import org.azeckoski.reflectutils.converters.StringConverter;
import org.azeckoski.reflectutils.converters.TimestampConverter;
import org.azeckoski.reflectutils.converters.URLConverter;
import org.azeckoski.reflectutils.converters.api.Converter;
import org.azeckoski.reflectutils.converters.api.InterfaceConverter;
import org.azeckoski.reflectutils.converters.api.VariableConverter;
import org.azeckoski.reflectutils.refmap.ReferenceMap;
import org.azeckoski.reflectutils.refmap.ReferenceType;

/**
 * Class which provides methods for converting between object types,
 * can be extended with various converters
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ConversionUtils {
	
	protected static SoftReference<ConversionUtils> instanceStorage;
	
	private static int timesCreated = 0;
	
	protected Map<Class<?>, Converter<?>> converters = null;
	
	protected List<VariableConverter> variableConverters = null;
	
	private boolean singleton = false;
	
	/**
	 * Constructor which allows adding to the initial set of converters <br/>
	 * <b>WARNING:</b> if you don't need this control then just use the
	 * {@link #getInstance()} method to get this
	 * 
	 * @param converters a map of converters to add to the default set
	 */
	public ConversionUtils(final Map<Class<?>, Converter<?>> converters) {
		// populate the converters
		this.setConverters(converters);
		this.setVariableConverters(null);
		
		ConversionUtils.setInstance(this);
	}
	
	/**
	 * Empty constructor
	 */
	protected ConversionUtils() {
		this(null);
	}
	
	/**
	 * Get a singleton instance of this class to work with (stored statically) <br/>
	 * <b>WARNING</b>: do not hold onto this object or cache it yourself, call
	 * this method again if you need it again
	 * 
	 * @return a singleton instance of this class
	 */
	public static ConversionUtils getInstance() {
		ConversionUtils instance = (ConversionUtils.instanceStorage == null ? null : ConversionUtils.instanceStorage.get());
		if (instance == null) {
			instance = ConversionUtils.setInstance(null);
		}
		return instance;
	}
	
	public static int getTimesCreated() {
		return ConversionUtils.timesCreated;
	}
	
	/**
	 * Set the singleton instance of the class which will be stored statically
	 * 
	 * @param instance the instance to use as the singleton instance
	 */
	public static ConversionUtils setInstance(final ConversionUtils newInstance) {
		ConversionUtils instance = newInstance;
		if (instance == null) {
			instance = new ConversionUtils();
			instance.singleton = true;
		}
		ConversionUtils.timesCreated++;
		ConversionUtils.instanceStorage = new SoftReference<ConversionUtils>(instance);
		return instance;
	}
	
	/**
	 * Add a converter to the default set which will convert objects to the
	 * supplied type
	 * 
	 * @param type the type this converter will convert objects to
	 * @param converter the converter
	 */
	public void addConverter(final Class<?> type, final Converter<?> converter) {
		if ((type == null) || (converter == null)) {
			throw new IllegalArgumentException(
				"You must specify a type and a converter in order to add a converter (no nulls)");
		}
		this.getConverters().put(type, converter);
	}
	
	/**
	 * Adds a variable converter to the end of the list of default variable
	 * converters
	 * 
	 * @param variableConverter
	 */
	public void addVariableConverter(final VariableConverter variableConverter) {
		if (variableConverter == null) {
			throw new IllegalArgumentException("You must specify a variableConverter in order to add it (no nulls)");
		}
		this.getVariableConverters().add(variableConverter);
	}
	
	/**
	 * Resets and removes all variable converters including the defaults,
	 * use this when you want to override the existing variable converters
	 */
	public void clearVariableConverters() {
		if (this.variableConverters != null) {
			this.variableConverters.clear();
		}
	}
	
	/**
	 * Converts an object to any other object if possible using the current set of
	 * converters,
	 * will allow nulls to pass through unless there is a converter which will
	 * handle them <br/>
	 * Includes special handling for primitives, arrays, and collections
	 * (will take the first value when converting to scalar)
	 * 
	 * @param <T>
	 * @param value any object
	 * @param type any class type that you want to try to convert the object to
	 * @return the converted value (allows null to pass through except in the case
	 *         of primitives which become the primitive default)
	 * @throws UnsupportedOperationException if the conversion cannot be completed
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(final Object value, final Class<T> type) {
		T convert = null;
		final Object toConvert = value;
		if (toConvert != null) {
			final Class<?> fromType = toConvert.getClass();
			// first we check to see if we even need to do the conversion
			if (ConstructorUtils.classAssignable(fromType, type)) {
				// this is already equivalent so no reason to convert
				convert = (T) value;
			}
			else {
				// needs to be converted
				try {
					convert = this.convertWithConverter(toConvert, type);
				}
				catch (final RuntimeException e) {
					throw new UnsupportedOperationException("Could not convert object (" + toConvert + ") from type (" + fromType
																									+ ") to type (" + type + "): " + e.getMessage(), e);
				}
			}
		}
		else {
			// object is null but type requested may be primitive
			if (ConstructorUtils.isClassPrimitive(type)) {
				// for primitives we return the default value
				if (ConstructorUtils.getPrimitiveDefaults().containsKey(type)) {
					convert = (T) ConstructorUtils.getPrimitiveDefaults().get(type);
				}
			}
		}
		return convert;
	}
	
	/**
	 * Added for apache commons beanutils compatibility,
	 * you should probably use {@link #convert(Object, Class)}<br/>
	 * Convert the string value to an object of the specified class (if
	 * possible). Otherwise, return a String representation of the value.
	 * 
	 * @param value the string value to be converted
	 * @param type any class type that you want to try to convert the object to
	 * @return the converted value
	 * @throws UnsupportedOperationException if the conversion cannot be completed
	 */
	public Object convertString(final String value, final Class<?> type) {
		Object convert = null;
		try {
			convert = this.convert(value, type);
		}
		catch (final UnsupportedOperationException e) {
			convert = value;
		}
		return convert;
	}
	
	/**
	 * Added for apache commons beanutils compatibility,
	 * you should probably use {@link #convert(Object, Class)}<br/>
	 * Convert the specified value into a String. If the specified value
	 * is an array, the first element (converted to a String) will be
	 * returned. The registered {@link Converter} for the
	 * <code>java.lang.String</code> class will be used, which allows
	 * applications to customize Object->String conversions (the default
	 * implementation simply uses toString()).
	 * 
	 * @param object any object
	 * @return the string OR null if one cannot be found
	 */
	public String convertToString(final Object object) {
		// code here is basically from ConvertUtilsBeans 1.8.0
		String convert = null;
		if (object == null) {
			convert = null;
		}
		else if (object.getClass().isArray()) {
			if (Array.getLength(object) < 1) {
				convert = null;
			}
			else {
				final Object value = Array.get(object, 0);
				if (value == null) {
					convert = null;
				}
				else {
					final Converter<String> converter = this.getConverter(String.class);
					return converter.convert(value);
				}
			}
		}
		else {
			final Converter<String> converter = this.getConverter(String.class);
			return converter.convert(object);
		}
		return convert;
	}
	
	/**
	 * @return true if this object is the singleton
	 */
	public boolean isSingleton() {
		return this.singleton;
	}
	
	/**
	 * Set the object converters to add to the default converters
	 * 
	 * @param converters a map of converters to add to the default set
	 */
	public void setConverters(final Map<Class<?>, Converter<?>> converters) {
		this.loadDefaultConverters();
		if (converters != null) {
			for (final Entry<Class<?>, Converter<?>> entry : converters.entrySet()) {
				if ((entry.getKey() != null) && (entry.getValue() != null)) {
					this.converters.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	/**
	 * Replace the current or default variable converters with a new set,
	 * this will remove the default variable converters and will not add them back
	 * in
	 * 
	 * @param variableConverters the variable object converters
	 */
	public void setVariableConverters(final List<VariableConverter> variableConverters) {
		this.loadDefaultVariableConverters();
		if (variableConverters != null) {
			for (final VariableConverter variableConverter : variableConverters) {
				if (variableConverter != null) {
					this.variableConverters.add(variableConverter);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("converters=");
		sb.append(this.getConverters().size());
		sb.append(":");
		for (final Entry<Class<?>, Converter<?>> entry : this.getConverters().entrySet()) {
			sb.append("[");
			sb.append(entry.getKey().getName());
			sb.append("=>");
			sb.append(entry.getValue().getClass().getName());
			sb.append("]");
		}
		sb.append(":variable=");
		sb.append(this.getVariableConverters().size());
		sb.append(":");
		for (final VariableConverter variableConverter : this.getVariableConverters()) {
			sb.append("(");
			sb.append(variableConverter.getClass().getName());
			sb.append(")");
		}
		return "Convert::c=" + ConversionUtils.timesCreated + ":s=" + this.singleton + ":" + sb.toString();
	}
	
	/**
	 * Use the converters to convert the value to the provided type,
	 * this simply finds the converter and does the conversion,
	 * will convert interface types automatically <br/>
	 * WARNING: you should use {@link #convert(Object, Class)} unless you have a
	 * special need
	 * for this, it is primarily for reducing code complexity
	 * 
	 * @param <T>
	 * @param value any object to be converted
	 * @param type the type to convert to
	 * @return the converted object (may be null)
	 * @throws UnsupportedOperationException is the conversion could not be
	 *           completed
	 */
	protected <T> T convertWithConverter(final Object value, final Class<T> type) {
		T convert = null;
		// check for a variable converter that says it will handle the conversion
		// first
		final VariableConverter variableConverter = this.getVariableConverter(value, type);
		if (variableConverter != null) {
			// use the variable converter
			convert = variableConverter.convert(value, type);
		}
		else {
			// use a converter
			final Converter<T> converter = this.getConverterOrFail(type);
			if (InterfaceConverter.class.isAssignableFrom(converter.getClass())) {
				convert = ((InterfaceConverter<T>) converter).convertInterface(value, type);
			}
			else {
				// standard converter
				convert = converter.convert(value);
			}
		}
		return convert;
	}
	
	// STATIC access
	
	protected ConstructorUtils getConstructorUtils() {
		return ConstructorUtils.getInstance();
	}
	
	/**
	 * Get the converter for the given type if there is one
	 * 
	 * @param <T>
	 * @param type the type to convert to
	 * @return the converter for this type OR null if there is not one
	 */
	@SuppressWarnings("unchecked")
	protected <T> Converter<T> getConverter(final Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("Cannot get a converter for nulls");
		}
		// first make sure we are using an actual wrapper class and not the
		// primitive class (int.class)
		Class<?> toType = ConstructorUtils.getWrapper(type);
		Converter<T> converter = (Converter<T>) this.getConverters().get(toType);
		if (converter == null) {
			// none found so try not using the interface
			toType = ConstructorUtils.getClassFromInterface(toType);
			converter = (Converter<T>) this.getConverters().get(toType);
			if (converter == null) {
				// still no converter found so try the interfaces
				for (final Class<?> iface : ConstructorUtils.getExtendAndInterfacesForClass(toType)) {
					converter = (Converter<T>) this.getConverters().get(iface);
					if (converter != null) {
						// found a converter
						break;
					}
				}
			}
		}
		return converter;
	}
	
	/**
	 * Get the converter or throw exception
	 * 
	 * @param <T>
	 * @param type type to convert to
	 * @return the converter or die
	 * @throws UnsupportedOperationException if the converter cannot be found
	 */
	protected <T> Converter<T> getConverterOrFail(final Class<T> type) {
		final Converter<T> converter = this.getConverter(type);
		if (converter == null) {
			throw new UnsupportedOperationException(
				"Conversion failure: No converter available to handle conversions to type (" + type + ")");
		}
		return converter;
	}
	
	protected Map<Class<?>, Converter<?>> getConverters() {
		if (this.converters == null) {
			this.loadDefaultConverters();
		}
		return this.converters;
	}
	
	protected FieldUtils getFieldUtils() {
		return FieldUtils.getInstance();
	}
	
	/**
	 * Get the variable converter for this value and type if there is one,
	 * returns null if no converter is available
	 * 
	 * @param value the value to convert
	 * @param type the type to convert to
	 * @return the variable converter if there is one OR null if none exists
	 */
	protected VariableConverter getVariableConverter(final Object value, final Class<?> type) {
		VariableConverter converter = null;
		final Class<?> toType = ConstructorUtils.getWrapper(type);
		for (final VariableConverter variableConverter : this.getVariableConverters()) {
			if (variableConverter.canConvert(value, toType)) {
				converter = variableConverter;
				break;
			}
		}
		return converter;
	}
	
	protected List<VariableConverter> getVariableConverters() {
		if (this.variableConverters == null) {
			this.loadDefaultVariableConverters();
		}
		return this.variableConverters;
	}
	
	/**
	 * this loads up all the default converters
	 */
	protected void loadDefaultConverters() {
		if (this.converters == null) {
			this.converters = new ReferenceMap<Class<?>, Converter<?>>(ReferenceType.WEAK, ReferenceType.STRONG);
		}
		else {
			this.converters.clear();
		}
		// order is not important here but maintain alpha order for readability
		this.converters.put(BigDecimal.class, new BigDecimalConverter());
		this.converters.put(BigInteger.class, new BigIntegerConverter());
		this.converters.put(Boolean.class, new BooleanConverter());
		this.converters.put(Byte.class, new ByteConverter());
		this.converters.put(Calendar.class, new CalendarConverter());
		this.converters.put(Character.class, new CharacterConverter());
		this.converters.put(Class.class, new ClassConverter());
		this.converters.put(Collection.class, new CollectionConverter());
		this.converters.put(Date.class, new DateConverter());
		this.converters.put(Double.class, new DoubleConverter());
		this.converters.put(Enum.class, new EnumConverter());
		this.converters.put(File.class, new FileConverter());
		this.converters.put(Float.class, new FloatConverter());
		this.converters.put(Integer.class, new IntegerConverter());
		this.converters.put(Long.class, new LongConverter());
		this.converters.put(Map.class, new MapConverter());
		this.converters.put(Number.class, new NumberConverter());
		this.converters.put(Short.class, new ShortConverter());
		this.converters.put(String.class, new StringConverter());
		this.converters.put(java.sql.Date.class, new SQLDateConverter());
		this.converters.put(java.sql.Time.class, new SQLTimeConverter());
		this.converters.put(java.sql.Timestamp.class, new TimestampConverter());
		this.converters.put(URL.class, new URLConverter());
	}
	
	protected void loadDefaultVariableConverters() {
		if (this.variableConverters == null) {
			this.variableConverters = new Vector<VariableConverter>();
		}
		else {
			this.clearVariableConverters();
		}
		this.variableConverters.add(new ArrayConverter());
		this.variableConverters.add(new ScalarConverter());
	}
	
}
