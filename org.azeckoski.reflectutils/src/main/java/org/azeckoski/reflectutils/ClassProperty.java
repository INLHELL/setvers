/**
 * $Id: ClassProperty.java 35 2008-11-05 17:19:05Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/ClassProperty.java $
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.annotations.ReflectTransient;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.azeckoski.reflectutils.map.OrderedMap;

/**
 * Simple class for holding the values we care about for a single field,
 * if it uses getters and setters then they must be like this:<br/>
 * If the fieldName is "thing" then: <br/>
 * <b>getter:</b> Object getThing() and
 * <b>setter:</b> void setThing(Object value) <br/>
 * Warning: Note that the values in this object may be garbage collected at any
 * time,
 * do not store this object, it is meant for immediate short term use only
 */
public class ClassProperty {
	
	/**
	 * Slightly extended version with the extra indexed properties,
	 * indexed getters and setters must be setup like so:<br/>
	 * If the fieldName is "thing" then: <br/>
	 * <b>getter:</b> Object getThing(int index) and
	 * <b>setter:</b> void setThing(int index, Object value)
	 */
	public static class IndexedProperty extends ClassProperty {
		
		private Method indexGetter;
		private Method indexSetter;
		
		public IndexedProperty(final String fieldName) {
			super(fieldName);
			this.indexed = true;
		}
		
		public IndexedProperty(final String fieldName, final Method getter, final Method setter) {
			super(fieldName, getter, setter);
			this.indexed = true;
		}
		
		public IndexedProperty(final String fieldName, final Method getter, final Method setter, final Method indexGetter, final Method indexSetter) {
			super(fieldName, getter, setter);
			this.setIndexGetter(indexGetter);
			this.setIndexSetter(indexSetter);
			this.indexed = true;
		}
		
		public Method getIndexGetter() {
			return this.indexGetter;
		}
		
		public Method getIndexSetter() {
			return this.indexSetter;
		}
		
		@Override
		public boolean isGettable() {
			boolean gettable = false;
			if ((this.getIndexGetter() != null) || super.isGettable()) {
				gettable = true;
			}
			return gettable;
		}
		
		@Override
		public boolean isSettable() {
			boolean settable = false;
			if ((this.getIndexSetter() != null) || super.isSettable()) {
				settable = true;
			}
			return settable;
		}
		
		protected void setIndexGetter(final Method indexGetter) {
			this.indexGetter = indexGetter;
		}
		
		protected void setIndexSetter(final Method indexSetter) {
			this.indexSetter = indexSetter;
		}
	}
	/**
	 * Slightly extended version with the extra mapped properties,
	 * mapped getters and setters must be setup like so:<br/>
	 * If the fieldName is "thing" then: <br/>
	 * <b>getter:</b> Object getThing(String key) and
	 * <b>setter:</b> void setThing(String key, Object value) <br/>
	 * The keys MUST be Strings
	 */
	public static class MappedProperty extends ClassProperty {
		
		private Method mapGetter;
		private Method mapSetter;
		
		public MappedProperty(final String fieldName) {
			super(fieldName);
			this.mapped = true;
		}
		
		public MappedProperty(final String fieldName, final Method getter, final Method setter) {
			super(fieldName, getter, setter);
			this.mapped = true;
		}
		
		public MappedProperty(final String fieldName, final Method getter, final Method setter, final Method mapGetter, final Method mapSetter) {
			super(fieldName, getter, setter);
			this.setMapGetter(mapGetter);
			this.setMapSetter(mapSetter);
			this.mapped = true;
		}
		
		public Method getMapGetter() {
			return this.mapGetter;
		}
		
		public Method getMapSetter() {
			return this.mapSetter;
		}
		
		@Override
		public boolean isGettable() {
			boolean gettable = false;
			if ((this.getMapGetter() != null) || super.isGettable()) {
				gettable = true;
			}
			return gettable;
		}
		
		@Override
		public boolean isSettable() {
			boolean settable = false;
			if ((this.getMapSetter() != null) || super.isSettable()) {
				settable = true;
			}
			return settable;
		}
		
		protected void setMapGetter(final Method mapGetter) {
			this.mapGetter = mapGetter;
		}
		
		protected void setMapSetter(final Method mapSetter) {
			this.mapSetter = mapSetter;
		}
	}
	
	protected boolean arrayed = false;
	protected int fieldModifiers = -1; // un-set value is -1
	protected boolean finalField = false;
	
	protected boolean indexed = false;
	protected boolean mapped = false;
	protected boolean publicField = false;
	protected boolean staticField = false;
	protected boolean transientField = false;
	
	private Field field;
	private final String fieldName;
	private Method getter;
	
	private OrderedMap<Class<? extends Annotation>, Annotation> propertyAnnotations; // contains
																																										// all
																																										// annotations
																																										// on
																																										// this
																																										// property
	
	private Method setter;
	
	private Class<?> type;
	
	public ClassProperty(final String fieldName) {
		this.fieldName = fieldName;
	}
	
	public ClassProperty(final String fieldName, final Field field) {
		this.fieldName = fieldName;
		this.setField(field);
	}
	
	public ClassProperty(final String fieldName, final Method getter, final Method setter) {
		this.fieldName = fieldName;
		if (getter != null) {
			this.setGetter(getter);
		}
		if (setter != null) {
			this.setSetter(setter);
		}
	}
	
	/**
	 * @param annotationType the annotation type to look for on this field
	 * @return the annotation of this type for this field OR null if none found
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
		T a = null;
		if (this.propertyAnnotations != null) {
			a = (T) this.propertyAnnotations.get(annotationType);
		}
		return a;
	}
	
	/**
	 * @return the annotations present on the property
	 */
	public Annotation[] getAnnotations() {
		final Collection<Annotation> c = this.getAnnotationsCollection();
		return c.toArray(new Annotation[c.size()]);
	}
	
	public Field getField() {
		return this.field;
	}
	
	/**
	 * @return the name of this field
	 */
	public String getFieldName() {
		return this.fieldName;
	}
	
	public Method getGetter() {
		return this.getter;
	}
	
	/**
	 * @return the fieldModifiers code for this field (see {@link Modifier} to
	 *         decode this)
	 */
	public int getModifiers() {
		return this.fieldModifiers;
	}
	
	public Method getSetter() {
		return this.setter;
	}
	
	/**
	 * @return the type of this field
	 */
	public Class<?> getType() {
		return this.type;
	}
	
	/**
	 * @return true if this field is an array
	 */
	public boolean isArray() {
		return this.arrayed;
	}
	
	/**
	 * @return true if this field is complete (value can be set and retrieved via
	 *         public methods/fields)
	 */
	public boolean isComplete() {
		return (this.isPublicGettable() && this.isPublicSettable());
	}
	
	/**
	 * @return true if the associated field object is set
	 */
	public boolean isField() {
		return (this.getField() != null);
	}
	
	/**
	 * @return true if this field is final
	 */
	public boolean isFinal() {
		return this.finalField;
	}
	
	/**
	 * @return true if this field value can be retrieved
	 */
	public boolean isGettable() {
		return (this.isField() || (this.getGetter() != null));
	}
	
	/**
	 * @return true if this field is indexed (typically a list)
	 */
	public boolean isIndexed() {
		return this.indexed;
	}
	
	/**
	 * @return true if this field is a map of some kind
	 */
	public boolean isMapped() {
		return this.mapped;
	}
	
	/**
	 * @return true if this field is partial (value can be set OR retrieved but
	 *         not both)
	 */
	public boolean isPartial() {
		return (!this.isComplete());
	}
	
	/**
	 * @return true if there is a getter and setter method available for this
	 *         field (may not be public)
	 */
	public boolean isProperty() {
		return ((this.getGetter() != null) && (this.getSetter() != null));
	}
	
	/**
	 * @return true if the associated field object is set and public
	 */
	public boolean isPublicField() {
		return (this.publicField && this.isField());
	}
	
	/**
	 * @return true if this field value can be retrieved and is public (either the
	 *         getter or the field)
	 */
	public boolean isPublicGettable() {
		return (this.isPublicField() || (this.getGetter() != null));
	}
	
	/**
	 * @return true if this field value can be set and is public (either the
	 *         setter or the field)
	 */
	public boolean isPublicSettable() {
		boolean settable = false;
		if (!this.finalField) { // no setting finals
			if (this.isPublicField() || (this.getSetter() != null)) {
				settable = true;
			}
		}
		return settable;
	}
	
	/**
	 * @return true if this field value can be set
	 */
	public boolean isSettable() {
		boolean settable = false;
		if (!this.finalField) { // no setting finals
			if (this.isField() || (this.getSetter() != null)) {
				settable = true;
			}
		}
		return settable;
	}
	
	/**
	 * @return true if the associated field object is static
	 */
	public boolean isStatic() {
		return this.staticField;
	}
	
	/**
	 * @return true if this field is transient
	 */
	public boolean isTransient() {
		return this.transientField;
	}
	
	@Override
	public String toString() {
		return this.fieldName + "(" + (this.type == null ? "" : this.type.getSimpleName()) + ")[" + (this.field == null ? "-" : "F")
						+ (this.getter == null ? "-" : "G") + (this.setter == null ? "-" : "S") + ":" + (this.mapped ? "M" : "-")
						+ (this.indexed ? "I" : "-") + (this.arrayed ? "A" : "-") + ":" + (this.finalField ? "F" : "-")
						+ (this.transientField ? "T" : "-") + (this.publicField ? "P" : "-") + (this.staticField ? "S" : "-") + "]";
	}
	
	protected void addAnnotation(final Annotation annotation) {
		if (annotation != null) {
			if (this.propertyAnnotations == null) {
				this.propertyAnnotations = new ArrayOrderedMap<Class<? extends Annotation>, Annotation>();
			}
			final Class<? extends Annotation> c = annotation.annotationType();
			if (!this.propertyAnnotations.containsKey(c)) {
				// only add an annotation in the first time it is encountered
				this.propertyAnnotations.put(c, annotation);
			}
			// note that we compare the simple name to avoid issues with cross
			// classloader types
			if (ReflectTransient.class.getSimpleName().equals(c.getSimpleName())) {
				this.transientField = true; // pretend to be transient
			}
		}
	}
	
	protected Collection<Annotation> getAnnotationsCollection() {
		Collection<Annotation> ans = null;
		if ((this.propertyAnnotations == null) || this.propertyAnnotations.isEmpty()) {
			ans = new ArrayList<Annotation>();
		}
		else {
			ans = this.propertyAnnotations.values();
		}
		return ans;
	}
	
	protected void setField(final Field field) {
		this.field = field;
		// only the field knows the true fieldModifiers
		this.setModifiers(field);
		if (this.type == null) {
			this.makeType();
		}
	}
	
	protected void setGetter(final Method getter) {
		this.getter = getter;
		this.makeType(); // getter type always wins
	}
	
	/**
	 * Sets the fieldModifiers based on this field,
	 * will not reset them once they are set except to clear them if a null field
	 * is set
	 */
	protected void setModifiers(final Field field) {
		if (field == null) {
			this.fieldModifiers = -1;
			this.transientField = false;
			this.finalField = false;
			this.publicField = false;
			this.staticField = false;
		}
		else {
			if (this.fieldModifiers < 0) {
				this.fieldModifiers = field.getModifiers();
				this.transientField = Modifier.isTransient(this.fieldModifiers);
				this.finalField = Modifier.isFinal(this.fieldModifiers);
				this.publicField = Modifier.isPublic(this.fieldModifiers);
				this.staticField = Modifier.isStatic(this.fieldModifiers);
			}
		}
	}
	
	protected void setSetter(final Method setter) {
		this.setter = setter;
		if (this.type == null) {
			this.makeType();
		}
	}
	
	protected void setType(final Class<?> type) {
		this.type = type;
	}
	
	private void makeType() {
		if (this.getGetter() != null) {
			final Method m = this.getGetter();
			this.setType(m.getReturnType());
		}
		else if (this.getSetter() != null) {
			final Method m = this.getSetter();
			final Class<?>[] params = m.getParameterTypes();
			if (params != null) {
				if (params.length == 1) {
					this.setType(params[0]); // normal setter
				}
				else if (params.length == 2) {
					this.setType(params[1]); // indexed/mapped setter
				}
			}
		}
		else {
			final Field f = this.getField();
			this.setType(f.getType());
		}
		this.indexed = false;
		this.arrayed = false;
		this.mapped = false;
		final Class<?> type = this.getType();
		if (type != null) {
			if (type.isArray()) {
				this.indexed = true;
				this.arrayed = true;
			}
			if (Map.class.isAssignableFrom(type)) {
				this.mapped = true;
			}
			if (List.class.isAssignableFrom(type)) {
				this.indexed = true;
			}
		}
	}
	
}
