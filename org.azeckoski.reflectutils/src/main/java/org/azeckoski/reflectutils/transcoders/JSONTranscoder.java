/**
 * $Id: JSONTranscoder.java 81 2011-12-19 17:03:40Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/transcoders/JSONTranscoder.java $
 * JSONTranscoder.java - entity-broker - Sep 16, 2008 3:19:29 PM - azeckoski
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ConversionUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;

/**
 * Provides methods for encoding and decoding JSON
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings({
	"unchecked",
	"rawtypes"
})
public class JSONTranscoder implements Transcoder {
	
	/**
	 * Create Java objects from JSON (note that only simple java objects, maps,
	 * and arrays will be returned) <br/>
	 * Dates will come back in as UTC timecodes or possibly strings which you will
	 * need to parse manually <br/>
	 * Numbers will come in as int if they are small, long if they are big, and
	 * BigInteger if they are huge,
	 * floating point is handled similarly: float, double, BigDecimal <br/>
	 * JSON arrays come back as a List always, similarly, any collection or array
	 * that was output will come back as a list <br/>
	 * You can use the {@link ConversionUtils} to help with conversion if needed <br/>
	 * 
	 * Derived from code at:
	 * https://svn.sourceforge.net/svnroot/stringtree/trunk/src/delivery/java/org/
	 * stringtree/json/JSONWriter.java
	 */
	public class JsonReader {
		
		private final StringBuffer buf = new StringBuffer(); // TODO crikey - thread
																										// safety
		private char c; // TODO crikey - thread safety
		private CharacterIterator it; // TODO crikey - thread safety
		private Object token; // TODO crikey - thread safety
		
		public Object read(final CharacterIterator it) {
			return this.read(it, JSONTranscoder.NEXT);
		}
		
		public Object read(final CharacterIterator ci, final int start) {
			this.it = ci;
			switch (start) {
				case FIRST:
					this.c = this.it.first();
					break;
				case CURRENT:
					this.c = this.it.current();
					break;
				case NEXT:
					this.c = this.it.next();
					break;
			}
			return this.read();
		}
		
		public Object read(final String string) {
			return this.read(new StringCharacterIterator(string), JSONTranscoder.FIRST);
		}
		
		private void add() {
			this.add(this.c);
		}
		
		private void add(final char cc) {
			this.buf.append(cc);
			this.next();
		}
		
		private int addDigits() {
			int ret;
			for (ret = 0; Character.isDigit(this.c); ++ret) {
				this.add();
			}
			return ret;
		}
		
		private Object array() {
			final List<Object> ret = new ArrayList<Object>();
			Object value = this.read();
			while (this.token != JSONTranscoder.MARK_ARRAY_END) {
				ret.add(value);
				if (this.read() == JSONTranscoder.MARK_COMMA) {
					value = this.read();
				}
			}
			return ret;
		}
		
		private char next() {
			this.c = this.it.next();
			return this.c;
		}
		
		private Object number() {
			int length = 0;
			boolean isFloatingPoint = false;
			this.buf.setLength(0);
			
			if (this.c == '-') {
				this.add();
			}
			length += this.addDigits();
			if (this.c == '.') {
				this.add();
				length += this.addDigits();
				isFloatingPoint = true;
			}
			if ((this.c == 'e') || (this.c == 'E')) {
				this.add();
				if ((this.c == '+') || (this.c == '-')) {
					this.add();
				}
				this.addDigits();
				isFloatingPoint = true;
			}
			
			final String s = this.buf.toString();
			// more friendly handling of numbers
			Object num = null;
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
		
		private Object object() {
			final Map<Object, Object> ret = new ArrayOrderedMap<Object, Object>();
			Object key = this.read();
			while (this.token != JSONTranscoder.MARK_OBJECT_END) {
				this.read(); // should be a colon
				if (this.token != JSONTranscoder.MARK_OBJECT_END) {
					ret.put(key, this.read());
					if (this.read() == JSONTranscoder.MARK_COMMA) {
						key = this.read();
					}
				}
			}
			
			return ret;
		}
		
		private Object read() {
			this.skipWhiteSpace();
			final char ch = this.c;
			this.next();
			switch (ch) {
				case '"':
					this.token = this.string();
					break;
				case '[':
					this.token = this.array();
					break;
				case ']':
					this.token = JSONTranscoder.MARK_ARRAY_END;
					break;
				case ',':
					this.token = JSONTranscoder.MARK_COMMA;
					break;
				case '{':
					this.token = this.object();
					break;
				case '}':
					this.token = JSONTranscoder.MARK_OBJECT_END;
					break;
				case ':':
					this.token = JSONTranscoder.MARK_COLON;
					break;
				case 't':
					this.next();
					this.next();
					this.next(); // assumed r-u-e
					this.token = Boolean.TRUE;
					break;
				case 'f':
					this.next();
					this.next();
					this.next();
					this.next(); // assumed a-l-s-e
					this.token = Boolean.FALSE;
					break;
				case 'n':
					this.next();
					this.next();
					this.next(); // assumed u-l-l
					this.token = null;
					break;
				default:
					this.c = this.it.previous();
					if (Character.isDigit(this.c) || (this.c == '-')) {
						this.token = this.number();
					}
			}
			// System.out.println("token: " + token); // enable this line to see the
			// token stream
			return this.token;
		}
		
		private void skipWhiteSpace() {
			while (Character.isWhitespace(this.c)) {
				this.next();
			}
		}
		
		private Object string() {
			this.buf.setLength(0);
			while (this.c != '"') {
				if (this.c == '\\') {
					this.next();
					if (this.c == 'u') {
						this.add(this.unicode());
					}
					else {
						final Object value = JSONTranscoder.escapes.get(new Character(this.c));
						if (value != null) {
							this.add(((Character) value).charValue());
						}
					}
				}
				else {
					this.add();
				}
			}
			this.next();
			
			return this.buf.toString();
		}
		
		private char unicode() {
			int value = 0;
			for (int i = 0; i < 4; ++i) {
				switch (this.next()) {
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
						value = ((value << 4) + this.c) - '0';
						break;
					case 'a':
					case 'b':
					case 'c':
					case 'd':
					case 'e':
					case 'f':
						value = (value << 4) + (this.c - 'a') + 10;
						break;
					case 'A':
					case 'B':
					case 'C':
					case 'D':
					case 'E':
					case 'F':
						value = (value << 4) + (this.c - 'A') + 10;
						break;
				}
			}
			return (char) value;
		}
		
	}
	
	public static final char AMP = '&';
	
	/**
	 * single quote (')
	 */
	public static final char APOS = '\'';
	
	public static final char ARRAY_BEG = '[';
	
	public static final char ARRAY_END = ']';
	
	public static final char BACK = '\\';
	
	public static final char BANG = '!';
	
	public static final String BOOLEAN_FALSE = "false";
	
	public static final String BOOLEAN_TRUE = "true";
	
	public static final int CURRENT = 1;
	public static final char EOL = '\n';
	public static final char EQ = '=';
	
	public static final int FIRST = 0;
	
	public static final char GT = '>';
	
	public static final char JSON_SEP = ',';
	
	// Encoder
	
	public static final char LT = '<';
	public static final int NEXT = 2;
	public static final String NULL = "null";
	public static final char OBJ_BEG = '{';
	public static final char OBJ_END = '}';
	public static final char OBJ_SEP = ':';
	
	// based on code from: http://www.json.org/java/org/json/XML.java
	
	public static final char QUEST = '?';
	public static final char QUOT = '"';
	public static final char SLASH = '/';
	public static final char SPACE = ' ';
	protected static char[] hex = "0123456789ABCDEF".toCharArray();
	protected static final String SPACES = "  ";
	private static Map<Character, Character> escapes = new HashMap<Character, Character>();
	private static final Object MARK_ARRAY_END = new Object();
	private static final Object MARK_COLON = new Object();
	private static final Object MARK_COMMA = new Object();
	private static final Object MARK_OBJECT_END = new Object();
	static {
		JSONTranscoder.escapes.put(new Character('"'), new Character('"'));
		JSONTranscoder.escapes.put(new Character('\\'), new Character('\\'));
		JSONTranscoder.escapes.put(new Character('/'), new Character('/'));
		JSONTranscoder.escapes.put(new Character('b'), new Character('\b'));
		JSONTranscoder.escapes.put(new Character('f'), new Character('\f'));
		JSONTranscoder.escapes.put(new Character('n'), new Character('\n'));
		JSONTranscoder.escapes.put(new Character('r'), new Character('\r'));
		JSONTranscoder.escapes.put(new Character('t'), new Character('\t'));
	}
	
	private List<ObjectEncoder> encoders = null;
	private boolean humanOutput = false;
	private boolean includeClassField = false;
	
	private boolean includeNulls = true;
	
	private int maxLevel = 7;
	
	/**
	 * Default constructor:
	 * See other constructors for options
	 */
	public JSONTranscoder() {
	}
	
	/**
	 * @param humanOutput if true then enable human readable output (includes
	 *          indentation and line breaks)
	 * @param includeNulls if true then create output tags for null values
	 * @param includeClassField if true then include the value from the
	 *          "getClass()" method as "class" when encoding beans and maps
	 */
	public JSONTranscoder(final boolean humanOutput, final boolean includeNulls, final boolean includeClassField) {
		this.humanOutput = humanOutput;
		this.includeNulls = includeNulls;
		this.includeClassField = includeClassField;
	}
	
	/**
	 * Escape a string for JSON encoding
	 * 
	 * @param string any string
	 * @return the escaped string
	 */
	public static String escapeForJSON(final String string) {
		final StringBuilder sb = new StringBuilder();
		if (string != null) {
			for (int i = 0, len = string.length(); i < len; i++) {
				final char c = string.charAt(i);
				switch (c) {
					case QUOT:
						sb.append("\\\"");
						break;
					case BACK:
						sb.append("\\\\");
						break;
					case SLASH:
						sb.append("\\/");
						break;
					case '\b':
						sb.append("\\b");
						break;
					case '\f':
						sb.append("\\f");
						break;
					case '\n':
						sb.append("\\n");
						break;
					case '\r':
						sb.append("\\r");
						break;
					case '\t':
						sb.append("\\t");
						break;
					default:
						if (Character.isISOControl(c)) {
							sb.append("\\u");
							int n = c;
							for (int j = 0; j < 4; ++j) {
								final int digit = (n & 0xf000) >> 12;
								sb.append(JSONTranscoder.hex[digit]);
								n <<= 4;
							}
						}
						else {
							sb.append(c);
						}
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convert an object into a well-formed, element-normal XML string.
	 * 
	 * @param object any object
	 * @return the JSON string version of the object
	 */
	public static String makeJSON(final Object object) {
		return JSONTranscoder.makeJSON(object, null, false, true, false, 10, null);
	}
	
	/**
	 * Convert an object into a well-formed, element-normal XML string.
	 * 
	 * @param object any object
	 * @param humanOutput true of human readable output
	 * @param includeNulls true to include null values when generating tags
	 * @param includeClassField if true then include the value from the
	 *          "getClass()" method as "class" when encoding beans and maps
	 * @param maxLevel maximum level to traverse the objects before stopping
	 * @param encoders the external encoders to allow to process complex objects
	 * @return the JSON string version of the object
	 */
	public static String makeJSON(final Object object, final Map<String, Object> properties, final boolean humanOutput,
		final boolean includeNulls, final boolean includeClassField, final int maxLevel, final List<ObjectEncoder> encoders) {
		return JSONTranscoder.toJSON(object, 0, maxLevel, humanOutput, includeNulls, includeClassField, properties, encoders);
	}
	
	// STATICS
	
	protected static void makeEOL(final StringBuilder sb, final boolean includeEOL) {
		if (includeEOL) {
			sb.append(JSONTranscoder.EOL);
		}
	}
	
	protected static void makeLevelSpaces(final StringBuilder sb, final int level, final boolean includeEOL) {
		if (includeEOL) {
			for (int i = 0; i < level; i++) {
				sb.append(JSONTranscoder.SPACES);
			}
		}
	}
	
	protected static String toJSON(final Object object, final int level, final int maxLevel, final boolean humanOutput, final boolean includeNulls,
		final boolean includeClassField, final Map<String, Object> properties, final List<ObjectEncoder> encoders) {
		final StringBuilder sb = new StringBuilder();
		
		if (object == null) {
			if (includeNulls) {
				// nulls use the constant
				sb.append(JSONTranscoder.NULL);
			}
		}
		else {
			final Class<?> type = ConstructorUtils.getWrapper(object.getClass());
			if (ConstructorUtils.isClassSimple(type)) {
				// Simple (String, Number, etc.)
				if (Date.class.isAssignableFrom(type) || Timestamp.class.isAssignableFrom(type)) {
					// date
					final Date d = (Date) object;
					sb.append(d.getTime());
				}
				else if (Number.class.isAssignableFrom(type)) {
					// number
					sb.append(object.toString());
				}
				else if (Boolean.class.isAssignableFrom(type)) {
					// boolean
					if (((Boolean) object).booleanValue()) {
						sb.append(JSONTranscoder.BOOLEAN_TRUE);
					}
					else {
						sb.append(JSONTranscoder.BOOLEAN_FALSE);
					}
				}
				else {
					sb.append(JSONTranscoder.QUOT);
					sb.append(JSONTranscoder.escapeForJSON(object.toString()));
					sb.append(JSONTranscoder.QUOT);
				}
			}
			else if (ConstructorUtils.isClassArray(type)) {
				// ARRAY
				final int length = ArrayUtils.size((Object[]) object);
				sb.append(JSONTranscoder.ARRAY_BEG);
				if (length > 0) {
					for (int i = 0; i < length; ++i) {
						if (i > 0) {
							sb.append(JSONTranscoder.JSON_SEP);
						}
						JSONTranscoder.makeEOL(sb, humanOutput);
						JSONTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
						sb.append(JSONTranscoder.toJSON(Array.get(object, i), level + 1, maxLevel, humanOutput, includeNulls, includeClassField,
							properties, encoders));
					}
					JSONTranscoder.makeEOL(sb, humanOutput);
					JSONTranscoder.makeLevelSpaces(sb, level, humanOutput);
				}
				sb.append(JSONTranscoder.ARRAY_END);
			}
			else if (ConstructorUtils.isClassCollection(type)) {
				// COLLECTION
				final Collection<Object> collection = (Collection) object;
				sb.append(JSONTranscoder.ARRAY_BEG);
				if (!collection.isEmpty()) {
					boolean first = true;
					for (final Object element : collection) {
						if (first) {
							first = false;
						}
						else {
							sb.append(JSONTranscoder.JSON_SEP);
						}
						JSONTranscoder.makeEOL(sb, humanOutput);
						JSONTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
						sb.append(JSONTranscoder.toJSON(element, level + 1, maxLevel, humanOutput, includeNulls, includeClassField, properties,
							encoders));
					}
					JSONTranscoder.makeEOL(sb, humanOutput);
					JSONTranscoder.makeLevelSpaces(sb, level, humanOutput);
				}
				sb.append(JSONTranscoder.ARRAY_END);
			}
			else {
				// must be a bean or map, make sure it is a map
				// special handling for certain object types
				final String special = TranscoderUtils.handleObjectEncoding(object, encoders);
				if (special != null) {
					if ("".equals(special)) {
						// skip this one entirely
						sb.append(JSONTranscoder.NULL);
					}
					else {
						// just use the value in special to represent this
						sb.append(JSONTranscoder.QUOT);
						sb.append(JSONTranscoder.escapeForJSON(special));
						sb.append(JSONTranscoder.QUOT);
					}
				}
				else {
					// normal handling
					if (maxLevel <= level) {
						// if the max level was reached then stop
						sb.append(JSONTranscoder.QUOT);
						sb.append("MAX level reached (");
						sb.append(level);
						sb.append("):");
						sb.append(JSONTranscoder.escapeForJSON(object.toString()));
						sb.append(JSONTranscoder.QUOT);
					}
					else {
						Map<String, Object> map = null;
						if (Map.class.isAssignableFrom(type)) {
							map = (Map<String, Object>) object;
						}
						else {
							// reflect over objects
							map = ReflectUtils.getInstance().getObjectValues(object, FieldsFilter.SERIALIZABLE, includeClassField);
						}
						// add in the optional properties if it makes sense to do so
						if ((level == 0) && (properties != null) && !properties.isEmpty()) {
							map.putAll(properties);
						}
						sb.append(JSONTranscoder.OBJ_BEG);
						boolean first = true;
						for (final Entry<String, Object> entry : map.entrySet()) {
							if (entry.getKey() != null) {
								final Object value = entry.getValue();
								if ((value != null) || includeNulls) {
									if (first) {
										first = false;
									}
									else {
										sb.append(JSONTranscoder.JSON_SEP);
									}
									JSONTranscoder.makeEOL(sb, humanOutput);
									JSONTranscoder.makeLevelSpaces(sb, level + 1, humanOutput);
									sb.append(JSONTranscoder.QUOT);
									sb.append(entry.getKey());
									sb.append(JSONTranscoder.QUOT);
									sb.append(JSONTranscoder.OBJ_SEP);
									if (humanOutput) {
										sb.append(JSONTranscoder.SPACE);
									}
									sb.append(JSONTranscoder.toJSON(value, level + 1, maxLevel, humanOutput, includeNulls, includeClassField,
										properties, encoders));
								}
							}
						}
						JSONTranscoder.makeEOL(sb, humanOutput);
						JSONTranscoder.makeLevelSpaces(sb, level, humanOutput);
						sb.append(JSONTranscoder.OBJ_END);
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
		Map<String, Object> decoded = null;
		final Object decode = new JsonReader().read(string);
		if (decode instanceof Map) {
			decoded = (Map<String, Object>) decode;
		}
		else {
			// for JSON if the result is not a map then simply put the result into a
			// map
			decoded = new ArrayOrderedMap<String, Object>();
			decoded.put(Transcoder.DATA_KEY, decode);
		}
		return decoded;
	}
	
	@Override
	public String encode(final Object object, final String name, final Map<String, Object> properties) {
		return this.encode(object, name, properties, this.maxLevel);
	}
	
	@Override
	public String encode(final Object object, final String name, final Map<String, Object> properties, final int maxDepth) {
		// Object data = object;
		// String encoded = "";
		// if (object != null) {
		// Map<String, Object> mapData = ReflectUtils.getInstance().map(object, 10,
		// null, false, true, Transcoder.DATA_KEY);
		// // for JSON we can get out the "data" field and convert that only
		// if (mapData.size() == 1 && mapData.containsKey(Transcoder.DATA_KEY)) {
		// data = mapData.get(Transcoder.DATA_KEY);
		// } else {
		// data = mapData;
		// }
		// }
		// allow the transcoder to deal with the data directly, no need to convert
		// it to a map first
		final String encoded =
			JSONTranscoder.makeJSON(object, properties, this.humanOutput, this.includeNulls, this.includeClassField, maxDepth, null);
		return encoded;
	}
	
	public List<ObjectEncoder> getEncoders() {
		return this.encoders;
	}
	
	@Override
	public String getHandledFormat() {
		return "json";
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
