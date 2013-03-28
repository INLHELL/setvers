/**
 * $Id: BaseDateFormatHolder.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/converters/BaseDateFormatHolder.java $
 * BaseDateFormatHolder.java - genericdao - Sep 8, 2008 2:15:02 PM - azeckoski
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Simple class to add in support for controlling the formats to
 * use when parsing the various dates
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class BaseDateFormatHolder {
	
	protected DateFormat[] formats;
	
	protected String[] patterns;
	
	public BaseDateFormatHolder() {
	}
	
	/**
	 * @param formats the formats to use when parsing strings into dates
	 */
	public BaseDateFormatHolder(final DateFormat[] formats) {
		this.setFormats(formats);
	}
	
	/**
	 * @param patterns set the patterns to use when parsing strings to dates
	 */
	public BaseDateFormatHolder(final String[] patterns) {
		this.setPatterns(patterns);
	}
	
	// = new String[] {
	// "yyyy-MM-dd HH:mm:ss.fffffffff",
	// "yyyy-MM-dd",
	// "HH:mm:ss"
	// };
	/**
	 * @return the array of date formats currently used by this converter
	 */
	public DateFormat[] getDateFormats() {
		if (this.formats == null) {
			final ArrayList<DateFormat> dateFormats = new ArrayList<DateFormat>();
			// add the standard short ones
			// dateFormats.add( DateFormat.getDateInstance(DateFormat.SHORT) );
			// dateFormats.add( DateFormat.getTimeInstance(DateFormat.SHORT) );
			// dateFormats.add( DateFormat.getDateTimeInstance(DateFormat.SHORT,
			// DateFormat.SHORT) );
			if (this.patterns != null) {
				for (int i = 0; i < this.patterns.length; i++) {
					dateFormats.add(new SimpleDateFormat(this.patterns[i]));
				}
			}
			this.formats = dateFormats.toArray(new DateFormat[dateFormats.size()]);
		}
		return this.formats;
	}
	
	/**
	 * @param formats sets the format objects to use for parsing
	 */
	public void setFormats(final DateFormat[] formats) {
		this.formats = formats;
	}
	
	/**
	 * @param patterns set the patterns to use for parsing
	 */
	public void setPatterns(final String[] patterns) {
		this.patterns = patterns;
		this.getDateFormats();
	}
	
}
