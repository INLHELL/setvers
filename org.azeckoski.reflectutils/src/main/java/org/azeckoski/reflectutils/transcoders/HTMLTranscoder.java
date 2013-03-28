/**
 * $Id: HTMLTranscoder.java 81 2011-12-19 17:03:40Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/transcoders/HTMLTranscoder.java $
 * HTMLTranscoder.java - entity-broker - Sep 15, 2008 6:36:42 PM - azeckoski
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

package org.azeckoski.reflectutils.transcoders;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ReflectUtils;

/**
 * Provides methods for encoding and decoding HTML <br/>
 * Note that the HTML parser is not supported
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings({
	"unchecked",
	"rawtypes"
})
public class HTMLTranscoder implements Transcoder {
	
	public static final char AMP = '&';
	
	/**
	 * single quote (')
	 */
	public static final char APOS = '\'';
	
	public static final char BANG = '!';
	
	public static final char EOL = '\n';
	
	public static final char EQ = '=';
	
	public static final char GT = '>';
	
	public static final char LT = '<';
	
	public static final char QUEST = '?';
	
	public static final char QUOT = '"';
	
	public static final char SLASH = '/';
	public static final char SPACE = ' ';
	protected static final String SPACES = "  ";
	
	private List<ObjectEncoder> encoders = null;
	
	private boolean humanOutput = true;
	
	private boolean includeClassField = false;
	
	private boolean includeNulls = true;
	private int maxLevel = 7;
	
	/**
	 * Default constructor:
	 * See other constructors for options
	 */
	public HTMLTranscoder() {
	}
	
	/**
	 * @param humanOutput if true then enable human readable output (includes
	 *          indentation and line breaks)
	 * @param includeNulls if true then create output tags for null values
	 * @param includeClassField if true then include the value from the
	 *          "getClass()" method as "class" when encoding beans and maps
	 */
	public HTMLTranscoder(final boolean humanOutput, final boolean includeNulls, final boolean includeClassField) {
		this.humanOutput = humanOutput;
		this.includeNulls = includeNulls;
		this.includeClassField = includeClassField;
	}
	
	/**
	 * Escape a string for XML encoding: replace special characters with XML
	 * escapes:
	 * 
	 * <pre>
	 * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
	 * &lt; <small>(less than)</small> is replaced by &amp;lt;
	 * &gt; <small>(greater than)</small> is replaced by &amp;gt;
	 * &quot; <small>(double quote)</small> is replaced by &amp;quot;
	 * </pre>
	 * 
	 * @param string The string to be escaped.
	 * @return The escaped string.
	 */
	public static String escapeForXML(final String string) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, len = string.length(); i < len; i++) {
			final char c = string.charAt(i);
			switch (c) {
				case AMP:
					sb.append("&amp;");
					break;
				case LT:
					sb.append("&lt;");
					break;
				case GT:
					sb.append("&gt;");
					break;
				case QUOT:
					sb.append("&quot;");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convert an object into a well-formed, element-normal HTML string.
	 * 
	 * @param object any object
	 * @return the HTML string version of the object
	 */
	public static String makeHTML(final Object object) {
		return HTMLTranscoder.makeHTML(object, null, null, false, true, false, 7, null);
	}
	
	/**
	 * Convert an object into a well-formed, element-normal HTML string.
	 * 
	 * @param object any object
	 * @param tagName (optional) enclosing root tag
	 * @param humanOutput true of human readable output
	 * @param includeNulls true to include null values when generating tags
	 * @param maxLevel TODO
	 * @return the HTML string version of the object
	 */
	public static String makeHTML(final Object object, final String tagName, final Map<String, Object> properties, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClassField, final int maxLevel, final List<ObjectEncoder> encoders) {
		return "<table border='1'>\n"
						+ HTMLTranscoder.toHTML(object, tagName, 0, maxLevel, humanOutput, includeNulls, includeClassField, properties, encoders)
						+ "</table>\n";
	}
	
	/**
	 * Validates that a string contains no spaces and is non-null/non-empty
	 * Throw an exception if the string contains whitespace.
	 * Whitespace is not allowed in tagNames and attributes.
	 * 
	 * @param string any string
	 * @throws IllegalArgumentException
	 */
	public static String validate(final String string) {
		if (string == null) {
			throw new IllegalArgumentException("string is NULL");
		}
		int i;
		final int length = string.length();
		if (length == 0) {
			throw new IllegalArgumentException("Empty string.");
		}
		for (i = 0; i < length; i += 1) {
			if (Character.isWhitespace(string.charAt(i))) {
				throw new IllegalArgumentException("'" + string + "' contains a space character.");
			}
		}
		return string;
	}
	
	protected static String makeElementName(final Class<?> type) {
		String name = "element";
		if (type != null) {
			if (!Map.class.isAssignableFrom(type)) {
				name = type.getSimpleName();
			}
		}
		return name;
	}
	
	protected static void makeEOL(final StringBuilder sb, final boolean includeEOL) {
		if (includeEOL) {
			sb.append(HTMLTranscoder.EOL);
		}
	}
	
	protected static void makeLevelSpaces(final StringBuilder sb, int level, final boolean includeEOL) {
		level++;
		if (includeEOL) {
			for (int i = 0; i < level; i++) {
				sb.append(HTMLTranscoder.SPACES);
			}
		}
	}
	
	protected static String toHTML(final Object object, String tagName, final int level, final int maxLevel, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClassField, final Map<String, Object> properties, final List<ObjectEncoder> encoders) {
		final StringBuilder sb = new StringBuilder();
		
		if (object == null) {
			if (includeNulls) {
				// nulls are empty tags always
				tagName = HTMLTranscoder.validate(tagName == null ? "null" : tagName);
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append("<tr><td>");
				sb.append(tagName);
				sb.append("</td><td>");
				sb.append("<i>NULL</i>");
				sb.append("</td></tr>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
			}
		}
		else {
			final Class<?> type = ConstructorUtils.getWrapper(object.getClass());
			if (ConstructorUtils.isClassSimple(type)) {
				// Simple (String, Number, etc.)
				tagName = HTMLTranscoder.validate(tagName == null ? HTMLTranscoder.makeElementName(type) : tagName);
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				final String value = HTMLTranscoder.escapeForXML(object.toString());
				sb.append("<tr><td>");
				sb.append(tagName);
				sb.append("</td><td>");
				sb.append(value);
				sb.append("</td></tr>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
			}
			else if (ConstructorUtils.isClassArray(type)) {
				// ARRAY
				tagName = HTMLTranscoder.validate(tagName == null ? "array" : tagName);
				final int length = ArrayUtils.size((Object[]) object);
				final Class<?> elementType = ArrayUtils.type((Object[]) object);
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append("<tr><td width='3%'>");
				sb.append(tagName);
				sb.append(" type=array");
				sb.append(" length=" + length);
				sb.append("</td><td>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
				sb.append("<table border='1'>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				for (int i = 0; i < length; ++i) {
					sb.append(HTMLTranscoder.toHTML(Array.get(object, i), HTMLTranscoder.makeElementName(elementType), level + 2, maxLevel, humanOutput,
						includeNulls, includeClassField, properties, encoders));
				}
				HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
				sb.append("</table>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append("</td></tr>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
			}
			else if (ConstructorUtils.isClassCollection(type)) {
				// COLLECTION
				tagName = HTMLTranscoder.validate(tagName == null ? "collection" : tagName);
				final Collection<Object> collection = (Collection) object;
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append("<tr><td width='3%'>");
				sb.append(tagName);
				sb.append(" type=collection");
				sb.append(" size=" + collection.size());
				sb.append("</td><td>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
				sb.append("<table border='1'>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				for (final Object element : collection) {
					Class<?> elementType = null;
					if (element != null) {
						elementType = element.getClass();
					}
					sb.append(HTMLTranscoder.toHTML(element, HTMLTranscoder.makeElementName(elementType), level + 2, maxLevel, humanOutput, includeNulls,
						includeClassField, properties, encoders));
				}
				HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
				sb.append("</table>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
				HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append("</td></tr>");
				HTMLTranscoder.makeEOL(sb, humanOutput);
			}
			else {
				// must be a bean or map, make sure it is a map
				tagName = HTMLTranscoder.validate(tagName == null ? HTMLTranscoder.makeElementName(type) : tagName);
				// special handling for certain object types
				final String special = TranscoderUtils.checkObjectSpecial(object);
				if (special != null) {
					if ("".equals(special)) {
						// skip this one entirely
					}
					else {
						// just use the value in special to represent this
						HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						final String value = HTMLTranscoder.escapeForXML(special);
						sb.append("<tr><td>");
						sb.append(tagName);
						sb.append("</td><td>");
						sb.append(value);
						sb.append("</td></tr>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
					}
				}
				else {
					// normal handling
					if ((maxLevel * 2) <= level) {
						// if the max level was reached then stop
						sb.append("<tr><td width='3%'>");
						sb.append(tagName);
						sb.append("</td><td>");
						sb.append("MAX level reached (");
						sb.append(level);
						sb.append("):");
						sb.append(HTMLTranscoder.escapeForXML(object.toString()));
						sb.append("</td></tr>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
					}
					else {
						String xmlType = "bean";
						Map<String, Object> map = null;
						if (Map.class.isAssignableFrom(type)) {
							xmlType = "map";
							map = (Map<String, Object>) object;
						}
						else {
							// reflect over objects
							map = ReflectUtils.getInstance().getObjectValues(object, FieldsFilter.SERIALIZABLE, includeClassField);
						}
						HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append("<tr><td width='3%'>");
						sb.append(tagName);
						sb.append(" type=" + xmlType);
						sb.append(" size=" + map.size());
						sb.append("</td><td>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
						HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
						sb.append("<table border='1'>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
						for (final Entry<String, Object> entry : map.entrySet()) {
							if (entry.getKey() != null) {
								sb.append(HTMLTranscoder.toHTML(entry.getValue(), entry.getKey().toString(), level + 2, maxLevel, humanOutput,
									includeNulls, includeClassField, properties, encoders));
							}
						}
						HTMLTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
						sb.append("</table>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
						HTMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append("</td></tr>");
						HTMLTranscoder.makeEOL(sb, humanOutput);
					}
				}
			}
		}
		return sb.toString();
	}
	
	public void addEncoder(final ObjectEncoder objectEncoder) {
		if (this.encoders == null) {
			this.encoders = new ArrayList<ObjectEncoder>();
		}
		this.encoders.add(objectEncoder);
	}
	
	@Override
	public Map<String, Object> decode(final String string) {
		throw new UnsupportedOperationException("Decoding from HTML is not supported");
	}
	
	@Override
	public String encode(final Object object, final String name, final Map<String, Object> properties) {
		return this.encode(object, name, properties, this.maxLevel);
	}
	
	@Override
	public String encode(final Object object, String name, final Map<String, Object> properties, final int maxDepth) {
		String encoded = "";
		if (object != null) {
			if ((name == null) || "".equals(name)) {
				name = Transcoder.DATA_KEY;
			}
		}
		encoded =
			HTMLTranscoder.makeHTML(object, name, properties, this.humanOutput, this.includeNulls, this.includeClassField,
				maxDepth, this.encoders);
		return encoded;
	}
	
	public List<ObjectEncoder> getEncoders() {
		return this.encoders;
	}
	
	@Override
	public String getHandledFormat() {
		return "html";
	}
	
	public void setEncoders(final List<ObjectEncoder> encoders) {
		this.encoders = encoders;
	}
	
	/**
	 * @param maxLevel the number of objects to follow when traveling through the
	 *          object,
	 *          0 means only the fields in the initial object, default is 7
	 */
	public void setMaxLevel(final int maxLevel) {
		this.maxLevel = maxLevel;
	}
}
