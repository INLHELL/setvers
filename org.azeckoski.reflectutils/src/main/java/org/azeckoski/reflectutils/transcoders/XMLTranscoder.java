/**
 * $Id: XMLTranscoder.java 81 2011-12-19 17:03:40Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/transcoders/XMLTranscoder.java $
 * XMLEncoder.java - entity-broker - Sep 15, 2008 6:36:42 PM - azeckoski
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

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.DateUtils;
import org.azeckoski.reflectutils.FieldUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides methods for encoding and decoding XML <br/>
 * Note that the XML parser always trashes the root node currently
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings({
	"unchecked",
	"rawtypes"
})
public class XMLTranscoder implements Transcoder {
	
	/**
	 * Use SAX to process the XML document
	 */
	public class XMLparser extends DefaultHandler {
		
		private final Stack<Container> containerStack = new Stack<Container>();
		private final CharArrayWriter contents = new CharArrayWriter();
		
		// this should be false when there are no contents to read
		private boolean currentContents = false;
		
		private Types currentType = null;
		
		private Map<String, Object> map = null;
		
		// handle the XML parsing
		
		private final Stack<String> tagStack = new Stack<String>();
		
		private String xml = null;
		
		public XMLparser(final String xml) {
			if ((xml == null) || "".equals(xml)) {
				throw new IllegalArgumentException("xml cannot be null or empty");
			}
			this.xml = xml;
			this.map = new ArrayOrderedMap<String, Object>();
			this.containerStack.push(new Container(this.map)); // init the holder stack
																										// (causes root node to be
																										// trashed)
			this.parseXML(xml);
		}
		
		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			// get the text out of the element
			this.contents.write(ch, start, length);
			this.currentContents = true;
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String name) throws SAXException {
			if (this.tagStack.size() > this.containerStack.size()) {
				// only add data when we are above a container
				Object val = null;
				if (this.currentContents) {
					final String content = XMLTranscoder.unescapeXML(this.contents.toString().trim());
					val = content;
					if (Types.BOOLEAN.equals(this.currentType)) {
						val = Boolean.valueOf(content);
					}
					else if (Types.NUMBER.equals(this.currentType)) {
						try {
							val = XMLTranscoder.number(content);
						}
						catch (final NumberFormatException e) {
							val = content;
						}
					}
					else if (Types.DATE.equals(this.currentType)) {
						try {
							val = new Date(Long.valueOf(content));
						}
						catch (final NumberFormatException e) {
							val = content;
						}
					}
				}
				// put the value into the current container
				this.add(this.containerStack.peek(), localName, val);
			}
			if (this.tagStack.isEmpty()) {
				throw new IllegalStateException("tag stack is out of sync, empty while still processing tags: " + localName);
			}
			else {
				this.tagStack.pop();
			}
			// now we need to remove the current container if we are done with it
			while (this.tagStack.size() < this.containerStack.size()) {
				if (this.containerStack.size() <= 1) {
					break;
				}
				this.containerStack.pop();
			}
			this.contents.reset();
		}
		
		/**
		 * @return the map which contains the data parsed out of the xml string
		 */
		public Map<String, Object> getMap() {
			return this.map;
		}
		
		// Event Handlers
		@Override
		public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
			this.contents.reset();
			this.tagStack.push(localName);
			if (this.tagStack.size() > (this.containerStack.size() + 1)) {
				// add a new container to the stack, use the types info from the parent
				final Container lastContainer = this.containerStack.peek();
				final Object newContainerObject = XMLTranscoder.makeContainerObject(this.currentType);
				final String parentName = (this.tagStack.size() > 1 ? this.tagStack.get(this.tagStack.size() - 2) : this.tagStack.peek());
				this.containerStack.push(new Container(lastContainer.getContainer(), parentName, newContainerObject));
				this.add(lastContainer, parentName, newContainerObject);
			}
			this.currentType = XMLTranscoder.getDataType(attributes);
			this.currentContents = false;
		}
		
		@Override
		public String toString() {
			return "parser: " + this.xml + " => " + this.map;
		}
		
		/**
		 * Adds the value to the container using the given key,
		 * if the key already exists in the container then the container needs to be
		 * switched
		 * over to a collection and its contents moved, then the stack needs to be
		 * updated,
		 * and finally the parent container needs to have it's value replaced
		 */
		protected void add(final Container container, final String key, final Object value) {
			// first we need to make sure this container is on the stack
			// if (containerStack.peek() != container) {
			// containerStack.push( new Container(container.getContainer(), key,
			// value) );
			// }
			// now do the add
			final Class<?> type = container.getContainer().getClass();
			if (ConstructorUtils.isClassMap(type)) {
				final Map<String, Object> m = (Map) container.getContainer();
				if (m.containsKey(key)) {
					// this should have been a collection so replace the map and move
					// elements over to collection
					final Collection collection = (Collection) XMLTranscoder.makeContainerObject(Types.COLLECTION);
					for (final Entry entry : m.entrySet()) {
						collection.add(entry.getValue());
					}
					collection.add(value);
					// now replace the container in the stack
					final int endPosition = this.containerStack.size() - 1;
					int containerPosition = endPosition;
					if ((container != this.containerStack.peek()) && (containerPosition != 0)) {
						containerPosition--;
					}
					final Container current = this.containerStack.get(containerPosition);
					current.replaceContainer(collection); // update container and replace
																								// the value in the parent
																								// object in the container
					// finally we need to get the next thing in the stack to point back at
					// the new parent
					if (containerPosition < endPosition) {
						// there is another container on the stack which needs to be
						// replaced
						this.containerStack.set(endPosition, new Container(collection, 1, value));
					}
				}
				else {
					m.put(key, value);
				}
			}
			else if (ConstructorUtils.isClassCollection(type)) {
				final Collection collection = ((Collection) container.getContainer());
				collection.add(value);
				// make sure the parent index is correct
				if (container != this.containerStack.peek()) {
					this.containerStack.peek().updateIndex(collection.size() - 1);
				}
			}
			else {
				// bean or something we hope
				try {
					ReflectUtils.getInstance().setFieldValue(container.getContainer(), key, value);
				}
				catch (final RuntimeException e) {
					throw new RuntimeException("Unknown container type (" + type + ") and could not set field on container: "
																			+ container, e);
				}
			}
		}
		
		protected void parseXML(final String xml) {
			try {
				XMLTranscoder.this.getParser().parse(new ByteArrayInputStream(xml.getBytes()), this);
			}
			catch (final SAXException se) {
				throw new IllegalArgumentException("Failed to parse xml (" + xml + "): " + se.getMessage(), se);
			}
			catch (final IOException ie) {
				throw new RuntimeException("Failed to convert XML string (" + xml + ") into inputstream", ie);
			}
		}
	}
	
	protected static class Container {
		
		public Object container;
		
		public int index;
		
		public String key;
		
		public Object parent;
		
		private boolean root = false;
		
		/**
		 * Use if parent is non-existent (i.e. this is the root)
		 */
		public Container(final Object container) {
			if (container == null) {
				throw new IllegalArgumentException("No null params allowed");
			}
			this.container = container;
			this.root = true;
		}
		
		/**
		 * Use if parent is indexed
		 */
		public Container(final Object parent, final int index, final Object container) {
			if ((parent == null) || (index < 0) || (container == null)) {
				throw new IllegalArgumentException("No null params or index < 0 allowed");
			}
			this.container = container;
			this.index = index;
			this.parent = parent;
		}
		
		/**
		 * Use if parent is keyed
		 */
		public Container(final Object parent, final String key, final Object container) {
			if ((parent == null) || (key == null) || (container == null)) {
				throw new IllegalArgumentException("No null params allowed");
			}
			this.container = container;
			this.key = key;
			this.parent = parent;
		}
		
		public Object getContainer() {
			return this.container;
		}
		
		public int getIndex() {
			return this.index;
		}
		
		public String getKey() {
			return this.key;
		}
		
		public Object getParent() {
			return this.parent;
		}
		
		public boolean isRoot() {
			return this.root;
		}
		
		/**
		 * Replace the container with a new one based on the parent and settings in
		 * this Container
		 */
		public void replaceContainer(final Object container) {
			if (container == null) {
				throw new IllegalArgumentException("No null params allowed");
			}
			if (this.key != null) {
				FieldUtils.getInstance().setFieldValue(this.parent, this.key, container);
			}
			else if (this.index >= 0) {
				FieldUtils.getInstance().setIndexedValue(this.parent, this.index, container);
			}
			// if not key or index then do nothing except replacing the value
			this.container = container;
		}
		
		public void setRoot(final boolean root) {
			this.root = root;
		}
		
		@Override
		public String toString() {
			return "C:root=" + this.root + ":parent=" + (this.parent == null ? this.parent : this.parent.getClass().getSimpleName()) + ":key="
							+ this.key + ":index=" + this.index + ":container="
							+ (this.container == null ? this.container : this.container.getClass().getSimpleName());
		}
		
		public void updateIndex(final int index) {
			if (index < 0) {
				throw new IllegalArgumentException("invalid index: " + index);
			}
			this.index = index;
			this.key = null;
		}
	}
	
	private static enum Types {
		ARRAY,
		BEAN,
		BOOLEAN,
		COLLECTION,
		DATE,
		MAP,
		NUMBER,
		STRING
	}
	
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
	
	private static final String ELEMENT = "element";
	
	protected SAXParser parser = null;
	
	protected SAXParserFactory parserFactory = null;
	
	private List<ObjectEncoder> encoders = null;
	
	private boolean fixTags = true;
	
	private boolean humanOutput = false;
	
	private boolean includeClass = false;
	
	private boolean includeClassField = false;
	
	private boolean includeNulls = true;
	
	private int maxLevel = 7;
	
	/**
	 * Default constructor:
	 * See other constructors for options
	 */
	public XMLTranscoder() {
	}
	
	/**
	 * @param humanOutput if true then enable human readable output (includes
	 *          indentation and line breaks)
	 * @param includeNulls if true then create output tags for null values
	 * @param includeClassField if true then include the value from the
	 *          "getClass()" method as "class" when encoding beans and maps
	 */
	public XMLTranscoder(final boolean humanOutput, final boolean includeNulls, final boolean includeClassField) {
		this.humanOutput = humanOutput;
		this.includeNulls = includeNulls;
		this.includeClassField = includeClassField;
	}
	
	/**
	 * @param humanOutput if true then enable human readable output (includes
	 *          indentation and line breaks)
	 * @param includeNulls if true then create output tags for null values
	 * @param includeClassField if true then include the value from the
	 *          "getClass()" method as "class" when encoding beans and maps
	 * @param includeClass if true then add in class tips to the XML output
	 */
	public XMLTranscoder(final boolean humanOutput, final boolean includeNulls, final boolean includeClassField, final boolean includeClass) {
		this.humanOutput = humanOutput;
		this.includeNulls = includeNulls;
		this.includeClassField = includeClassField;
		this.includeClass = includeClass;
	}
	
	/**
	 * This will force a tag or attribute to be valid in XML by replacing the
	 * invalid chars with "_",
	 * invalid chars are ' ' (space), =, ', ", >, <, &
	 * 
	 * @param string any string
	 * @return a valid string
	 */
	public static String convertInvalidChars(String string) {
		if ((string != null) && (string.length() > 0)) {
			string =
				string.replace(' ', '_').replace('=', '_').replace('"', '_').replace('\'', '_').replace('<', '_')
					.replace('>', '_').replace('&', '_');
		}
		return string;
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
	
	// STATICS
	
	/**
	 * Convert an object into a well-formed, element-normal XML string.
	 * 
	 * @param object any object
	 * @return the XML string version of the object
	 */
	public static String makeXML(final Object object) {
		return XMLTranscoder.makeXML(object, null, null, false, true, false, false, 7, true, null);
	}
	
	/**
	 * Convert an object into a well-formed, element-normal XML string.
	 * 
	 * @param object any object
	 * @param tagName (optional) enclosing root tag
	 * @param properties (optional) optional properties to add into the encoded
	 *          data
	 * @param humanOutput true of human readable output
	 * @param includeNulls true to include null values when generating tags
	 * @param maxLevel the maximum number of levels of objects to encode before
	 *          stopping
	 * @param fixTags fix up tag names (instead of throwing an exception)
	 * @return the XML string version of the object
	 */
	public static String makeXML(final Object object, final String tagName, final Map<String, Object> properties, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClass, final boolean includeClassField, final int maxLevel, final boolean fixTags,
		final List<ObjectEncoder> encoders) {
		return XMLTranscoder.toXML(object, tagName, 0, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, fixTags,
			properties, encoders);
	}
	
	/**
	 * Convert an object into a well-formed, element-normal XML string.
	 * 
	 * @param object any object
	 * @param tagName (optional) enclosing root tag
	 * @param properties (optional) optional properties to add into the encoded
	 *          data
	 * @param humanOutput true of human readable output
	 * @param includeNulls true to include null values when generating tags
	 * @param maxLevel the maximum number of levels of objects to encode before
	 *          stopping
	 * @return the XML string version of the object
	 */
	public static String makeXML(final Object object, final String tagName, final Map<String, Object> properties, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClass, final boolean includeClassField, final int maxLevel, final List<ObjectEncoder> encoders) {
		return XMLTranscoder.toXML(object, tagName, 0, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, true,
			properties, encoders);
	}
	
	public static String unescapeXML(String string) {
		if ((string != null) && (string.length() > 0)) {
			string =
				string.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&")
					.replace("&apos;", "'");
		}
		return string;
	}
	
	/**
	 * Validates that a string is a valid tag or attribute name
	 * i.e. it contains no spaces and is non-null/non-empty
	 * Whitespace is not allowed in tagNames and attributes.
	 * 
	 * @param string any string
	 * @param correct if true then correct any errors found (if possible)
	 * @return the valid string
	 * @throws IllegalArgumentException if the string is invalid (and cannot be
	 *           corrected)
	 */
	public static String validate(final String string, final boolean correct) {
		if (string == null) {
			throw new IllegalArgumentException("string is NULL");
		}
		int i;
		final int length = string.length();
		if (length == 0) {
			throw new IllegalArgumentException("Empty string.");
		}
		final StringBuilder sb = new StringBuilder();
		for (i = 0; i < length; i += 1) {
			final char c = string.charAt(i);
			if (Character.isWhitespace(c)) {
				if (correct) {
					sb.append('_');
				}
				else {
					throw new IllegalArgumentException("'" + string + "' contains a whitespace character.");
				}
			}
			else if (('=' == c) || ('\'' == c) || ('\"' == c) || ('>' == c) || ('<' == c) || ('&' == c)) {
				if (correct) {
					sb.append('_');
				}
				else {
					throw new IllegalArgumentException("'" + string + "' contains an illegal xml character (=,',\",>,<,&).");
				}
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	protected static Class<?> getDataClass(final Attributes attributes) {
		final Class<?> type = String.class;
		String value = attributes.getValue("", "type");
		if (value != null) {
			if (value.startsWith("class ")) {
				value = value.substring(6);
			}
			// TODO handle the classes?
		}
		return type;
	}
	
	protected static Types getDataType(final Attributes attributes) {
		Types elementType = Types.STRING;
		final String value = attributes.getValue("", "type");
		if (value != null) {
			if ("boolean".equals(value)) {
				elementType = Types.BOOLEAN;
			}
			else if ("number".equals(value)) {
				elementType = Types.NUMBER;
			}
			else if ("date".equals(value)) {
				elementType = Types.DATE;
			}
			else if ("array".equals(value)) {
				elementType = Types.ARRAY;
			}
			else if ("collection".equals(value)) {
				elementType = Types.COLLECTION;
			}
			else if ("map".equals(value)) {
				elementType = Types.MAP;
			}
			else if ("bean".equals(value)) {
				elementType = Types.BEAN;
			}
		}
		return elementType;
	}
	
	protected static void makeClassName(final StringBuilder sb, final Class<?> type) {
		if (type != null) {
			sb.append(" class='");
			sb.append(type.getName());
			sb.append(XMLTranscoder.APOS);
		}
	}
	
	protected static Object makeContainerObject(final Types type) {
		Object newContainer = null;
		if (Types.ARRAY.equals(type) || Types.COLLECTION.equals(type)) {
			newContainer = new Vector<Object>();
		}
		else {
			// bean, map, unknown
			newContainer = new ArrayOrderedMap<String, Object>();
		}
		return newContainer;
	}
	
	protected static String makeElementName(final Class<?> type) {
		String name = XMLTranscoder.ELEMENT;
		if (type != null) {
			if (Map.class.isAssignableFrom(type)) {
				// use the default "element"
			}
			else {
				final String simpleName = type.getSimpleName().toLowerCase();
				// strip off the [] for arrays
				final int index = simpleName.indexOf('[');
				if (index == 0) {
					// weird to have [] at the beginning so just use default
				}
				else if (index > 0) {
					name = simpleName.substring(0, index);
				}
				else {
					// not array so just use the class name
					// TODO maybe handle this prettier with by adding in "-" and stuff?
					name = simpleName;
				}
			}
		}
		return name;
	}
	
	protected static void makeEOL(final StringBuilder sb, final boolean includeEOL) {
		if (includeEOL) {
			sb.append(XMLTranscoder.EOL);
		}
	}
	
	protected static void makeLevelSpaces(final StringBuilder sb, final int level, final boolean includeEOL) {
		if (includeEOL) {
			for (int i = 0; i < level; i++) {
				sb.append(XMLTranscoder.SPACES);
			}
		}
	}
	
	/**
	 * Converts a string into a number
	 * 
	 * @param s the string
	 * @return the number
	 * @throws NumberFormatException if the string is not a number
	 */
	@SuppressWarnings("fallthrough")
	protected static Number number(final String s) {
		int length = s.length();
		boolean isFloatingPoint = false;
		
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			switch (c) {
				case '.':
				case 'e':
				case 'E':
					isFloatingPoint = true;
				case '-':
				case '+':
					length--;
			}
		}
		
		// more friendly handling of numbers
		Number num = null;
		if (isFloatingPoint) {
			if (length < 10) {
				num = Float.valueOf(s);
			}
			else if (length < 17) {
				num = Double.valueOf(s);
			}
			else {
				num = new BigDecimal(s);
			}
		}
		else {
			if (length < 10) {
				num = Integer.valueOf(s);
			}
			else if (length < 19) {
				num = Long.valueOf(s);
			}
			else {
				num = new BigInteger(s);
			}
		}
		return num;
	}
	
	protected static String toXML(final Object object, String tagName, final int level, final int maxLevel, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClass, final boolean includeClassField, final boolean fixTags,
		final Map<String, Object> properties, final List<ObjectEncoder> encoders) {
		final StringBuilder sb = new StringBuilder();
		
		if (object == null) {
			if (includeNulls) {
				// nulls are empty tags always
				tagName = XMLTranscoder.validate(tagName == null ? "null" : tagName, fixTags);
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(tagName);
				sb.append(XMLTranscoder.SLASH);
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
			}
		}
		else {
			final Class<?> type = ConstructorUtils.getWrapper(object.getClass());
			if (ConstructorUtils.isClassSimple(type)) {
				// Simple (String, Number, etc.)
				tagName = XMLTranscoder.validate(tagName == null ? XMLTranscoder.makeElementName(type) : tagName, fixTags);
				String value = "";
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(tagName);
				if (Date.class.isAssignableFrom(type) || Timestamp.class.isAssignableFrom(type)) {
					// date
					final Date d = (Date) object;
					value = d.getTime() + "";
					sb.append(" type='date' date='");
					sb.append(DateUtils.makeDateISO8601(d));
					sb.append(XMLTranscoder.APOS);
				}
				else if (Number.class.isAssignableFrom(type)) {
					// number
					sb.append(" type='number'");
					if (includeClass) {
						XMLTranscoder.makeClassName(sb, type);
					}
					value = object.toString();
				}
				else if (Boolean.class.isAssignableFrom(type)) {
					// boolean
					value = object.toString();
					sb.append(" type='boolean'");
				}
				else {
					value = XMLTranscoder.escapeForXML(object.toString());
				}
				sb.append(XMLTranscoder.GT);
				sb.append(value);
				sb.append(XMLTranscoder.LT);
				sb.append(XMLTranscoder.SLASH);
				sb.append(tagName);
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
			}
			else if (ConstructorUtils.isClassArray(type)) {
				// ARRAY
				tagName = XMLTranscoder.validate(tagName == null ? "array" : tagName, fixTags);
				final int length = ArrayUtils.size((Object[]) object);
				final Class<?> elementType = ArrayUtils.type((Object[]) object);
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(tagName);
				sb.append(" type='array' length='");
				sb.append(length);
				sb.append(XMLTranscoder.APOS);
				if (includeClass) {
					sb.append(" component='");
					sb.append(ConstructorUtils.getTypeFromInnerCollection(elementType).getName());
					sb.append(XMLTranscoder.APOS);
				}
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
				for (int i = 0; i < length; ++i) {
					sb.append(XMLTranscoder.toXML(Array.get(object, i), XMLTranscoder.makeElementName(elementType), level + 1, maxLevel, humanOutput,
						includeNulls, includeClass, includeClassField, fixTags, properties, encoders));
				}
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(XMLTranscoder.SLASH);
				sb.append(tagName);
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
			}
			else if (ConstructorUtils.isClassCollection(type)) {
				// COLLECTION
				tagName = XMLTranscoder.validate(tagName == null ? "collection" : tagName, fixTags);
				final Collection<Object> collection = (Collection) object;
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(tagName);
				sb.append(" type='collection' size='");
				sb.append(collection.size());
				sb.append(XMLTranscoder.APOS);
				if (includeClass) {
					XMLTranscoder.makeClassName(sb, ConstructorUtils.getTypeFromInnerCollection(type));
				}
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
				for (final Object element : collection) {
					Class<?> elementType = null;
					if (element != null) {
						elementType = element.getClass();
					}
					sb.append(XMLTranscoder.toXML(element, XMLTranscoder.makeElementName(elementType), level + 1, maxLevel, humanOutput, includeNulls,
						includeClass, includeClassField, fixTags, properties, encoders));
				}
				XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
				sb.append(XMLTranscoder.LT);
				sb.append(XMLTranscoder.SLASH);
				sb.append(tagName);
				sb.append(XMLTranscoder.GT);
				XMLTranscoder.makeEOL(sb, humanOutput);
			}
			else {
				// must be a bean or map, make sure it is a map
				tagName = XMLTranscoder.validate(tagName == null ? XMLTranscoder.makeElementName(type) : tagName, fixTags);
				// special handling for certain object types
				final String special = TranscoderUtils.handleObjectEncoding(object, encoders);
				if (special != null) {
					if ("".equals(special)) {
						// skip this one entirely
					}
					else {
						// just use the value in special to represent this
						XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append(XMLTranscoder.LT);
						sb.append(tagName);
						sb.append(XMLTranscoder.GT);
						sb.append(XMLTranscoder.escapeForXML(special));
						sb.append(XMLTranscoder.LT);
						sb.append(XMLTranscoder.SLASH);
						sb.append(tagName);
						sb.append(XMLTranscoder.GT);
						XMLTranscoder.makeEOL(sb, humanOutput);
					}
				}
				else {
					// normal handling
					if (maxLevel <= level) {
						// if the max level was reached then stop
						sb.append(XMLTranscoder.LT);
						sb.append(tagName);
						sb.append(XMLTranscoder.GT);
						sb.append("MAX level reached (");
						sb.append(level);
						sb.append("):");
						sb.append(XMLTranscoder.escapeForXML(object.toString()));
						sb.append(XMLTranscoder.LT);
						sb.append(XMLTranscoder.SLASH);
						sb.append(tagName);
						sb.append(XMLTranscoder.GT);
						XMLTranscoder.makeEOL(sb, humanOutput);
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
							map = ReflectUtils.getInstance().getObjectValues(object, FieldsFilter.SERIALIZABLE, false);
						}
						// add in the optional properties if it makes sense to do so
						if ((level == 0) && (properties != null) && !properties.isEmpty()) {
							map.putAll(properties);
						}
						XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append(XMLTranscoder.LT);
						sb.append(tagName);
						sb.append(" type='");
						sb.append(xmlType);
						sb.append(XMLTranscoder.APOS);
						sb.append(" size='");
						sb.append(map.size());
						sb.append(XMLTranscoder.APOS);
						if (includeClass) {
							XMLTranscoder.makeClassName(sb, ConstructorUtils.getTypeFromInnerCollection(type));
						}
						sb.append(XMLTranscoder.GT);
						XMLTranscoder.makeEOL(sb, humanOutput);
						for (final Entry<String, Object> entry : map.entrySet()) {
							if (entry.getKey() != null) {
								sb.append(XMLTranscoder.toXML(entry.getValue(), entry.getKey().toString(), level + 1, maxLevel, humanOutput,
									includeNulls, includeClass, includeClassField, fixTags, properties, encoders));
							}
						}
						XMLTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append(XMLTranscoder.LT);
						sb.append(XMLTranscoder.SLASH);
						sb.append(tagName);
						sb.append(XMLTranscoder.GT);
						XMLTranscoder.makeEOL(sb, humanOutput);
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
		// Object decode = new XMLparser(string).getObject();
		// if (decode instanceof Map) {
		// decoded = (Map<String, Object>) decode;
		// } else {
		// // if the result is not a map then simply put the result into a map
		// decoded = new ArrayOrderedMap<String, Object>();
		// decoded.put(Transcoder.DATA_KEY, decode);
		// }
		return new XMLparser(string).getMap();
	}
	
	@Override
	public String encode(final Object object, final String name, final Map<String, Object> properties) {
		return this.encode(object, name, properties, this.maxLevel);
	}
	
	@Override
	public String encode(final Object object, String name, final Map<String, Object> properties, final int maxDepth) {
		String encoded = "";
		if (object != null) {
			// only set the name if this is not null to preserve the "null" tag
			if ((name == null) || "".equals(name)) {
				name = Transcoder.DATA_KEY;
			}
		}
		encoded =
			XMLTranscoder.makeXML(object, name, properties, this.humanOutput, this.includeNulls, this.includeClass,
				this.includeClassField, maxDepth, this.fixTags, this.encoders);
		return encoded;
	}
	
	public List<ObjectEncoder> getEncoders() {
		return this.encoders;
	}
	
	@Override
	public String getHandledFormat() {
		return "xml";
	}
	
	public int getMaxLevel() {
		return this.maxLevel;
	}
	
	public boolean isFixTags() {
		return this.fixTags;
	}
	
	// DECODER
	
	public boolean isHumanOutput() {
		return this.humanOutput;
	}
	
	public boolean isIncludeClass() {
		return this.includeClass;
	}
	
	public boolean isIncludeClassField() {
		return this.includeClassField;
	}
	
	public boolean isIncludeNulls() {
		return this.includeNulls;
	}
	
	public void setEncoders(final List<ObjectEncoder> encoders) {
		this.encoders = encoders;
	}
	
	/**
	 * @param fixTags if true then fix up any invalid xml tag names, else just
	 *          throw exception
	 */
	public void setFixTags(final boolean fixTags) {
		this.fixTags = fixTags;
	}
	
	public void setHumanOutput(final boolean humanOutput) {
		this.humanOutput = humanOutput;
	};
	
	public void setIncludeClass(final boolean includeClass) {
		this.includeClass = includeClass;
	}
	
	public void setIncludeClassField(final boolean includeClassField) {
		this.includeClassField = includeClassField;
	}
	
	public void setIncludeNulls(final boolean includeNulls) {
		this.includeNulls = includeNulls;
	}
	
	/**
	 * @param maxLevel the number of objects to follow when traveling through the
	 *          object,
	 *          0 means only the fields in the initial object, default is 7
	 */
	public void setMaxLevel(final int maxLevel) {
		this.maxLevel = maxLevel;
	}
	
	protected SAXParser getParser() {
		if (this.parserFactory == null) {
			this.parserFactory = SAXParserFactory.newInstance();
			this.parserFactory.setValidating(true);
			this.parserFactory.setNamespaceAware(true);
		}
		if (this.parser != null) {
			try {
				this.parser.reset();
			}
			catch (final UnsupportedOperationException e) {
				// could not reset it so we have to make a new one
				this.parser = null;
			}
		}
		if (this.parser == null) {
			try {
				this.parser = this.parserFactory.newSAXParser();
			}
			catch (final ParserConfigurationException e) {
				throw new RuntimeException("Failed to get XML parser from factory: " + this.parserFactory, e);
			}
			catch (final SAXException e) {
				throw new RuntimeException("Failed to get XML parser from factory: " + this.parserFactory, e);
			}
		}
		return this.parser;
	}
	
}
