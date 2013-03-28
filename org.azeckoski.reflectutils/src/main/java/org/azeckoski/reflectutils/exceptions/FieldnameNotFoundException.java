/**
 * $Id: FieldnameNotFoundException.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/exceptions/FieldnameNotFoundException.java $
 * FieldnameNotFoundException.java - genericdao - Apr 27, 2008 2:47:36 PM -
 * azeckoski
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

package org.azeckoski.reflectutils.exceptions;

/**
 * Indicates that the fieldname could not be found
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class FieldnameNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3046709197826557997L;
	public String fieldName;
	
	public FieldnameNotFoundException(final String fieldName) {
		this(fieldName, null);
	}
	
	public FieldnameNotFoundException(final String message, final String fieldName, final Throwable cause) {
		super(message, cause);
		this.fieldName = fieldName;
	}
	
	public FieldnameNotFoundException(final String fieldName, final Throwable cause) {
		this("Could not find fieldName (" + fieldName + ") on object", fieldName, cause);
	}
	
}
