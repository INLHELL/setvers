/**
 * $Id: FieldSetValueException.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/exceptions/FieldSetValueException.java $
 * FieldSetValueException.java - genericdao - May 21, 2008 4:45:14 PM -
 * azeckoski
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

package org.azeckoski.reflectutils.exceptions;

/**
 * Indicates that there was a low level failure setting the value on an object
 * field,
 * this is probably caused by a type failure or security failure
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class FieldSetValueException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8662444365035197640L;
	/**
	 * the field name we were trying to set
	 */
	public String fieldName;
	/**
	 * the value we were trying to set on the field
	 */
	public Object fieldvalue;
	/**
	 * the object with the field we were trying to set
	 */
	public Object object;
	
	public FieldSetValueException(final String fieldName, final Object fieldvalue, final Object object, final Throwable cause) {
		super("Failed to set field (" + fieldName + ") to value (" + fieldvalue + "), cause=" + cause, cause);
		this.fieldName = fieldName;
		this.fieldvalue = fieldvalue;
		this.object = object;
	}
	
	public FieldSetValueException(final String message, final String fieldName, final Object fieldvalue, final Object object, final Throwable cause) {
		super(message, cause);
		this.fieldName = fieldName;
		this.fieldvalue = fieldvalue;
		this.object = object;
	}
	
}
