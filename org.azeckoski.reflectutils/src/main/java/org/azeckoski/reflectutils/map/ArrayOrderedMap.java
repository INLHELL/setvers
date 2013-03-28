/**
 * $Id: ArrayOrderedMap.java 2 2008-10-01 10:04:26Z azeckoski $
 * $URL:
 * https://reflectutils.googlecode.com/svn/trunk/src/main/java/org/azeckoski
 * /reflectutils/map/ArrayOrderedMap.java $
 * ArrayOrderedMap.java - genericdao - May 5, 2008 2:16:35 PM - azeckoski
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

package org.azeckoski.reflectutils.map;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map which keeps track of the order the entries are added
 * and allows retrieval of entries in the order they were entered as well,
 * this is NOT safe for multi-threaded access,
 * this is backed by a {@link HashMap} and {@link ArrayList}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ArrayOrderedMap<K, V> extends HashMap<K, V> implements OrderedMap<K, V> {
	
	abstract class CoreIterator {
		
		int currentPos = -1;
		Entry<K, V> lastReturned = null;
		private final List<Entry<K, V>> entries = ArrayOrderedMap.this.getEntries();
		
		public Entry<K, V> getNext() {
			this.currentPos++;
			try {
				this.lastReturned = this.entries.get(this.currentPos);
			}
			catch (final RuntimeException e) {
				throw new NoSuchElementException("There are no more items available to get, the last one was reached");
			}
			return this.lastReturned;
		}
		
		public boolean hasMore() {
			if ((this.currentPos + 1) < this.entries.size()) {
				return true;
			}
			return false;
		}
		
		public boolean hasMoreElements() {
			return this.hasMore();
		}
		
		// shared methods
		public boolean hasNext() {
			return this.hasMore();
		}
		
		public void remove() {
			this.removeCurrent();
		}
		
		public void removeCurrent() {
			if (this.currentPos < 0) {
				throw new IllegalArgumentException("Have not called next yet, cannot remove from this iterator");
			}
			this.entries.remove(this.currentPos);
			ArrayOrderedMap.this.remove(this.lastReturned.getKey());
		}
		
	}
	
	final class EntryIterator extends CoreIterator implements Iterator<Entry<K, V>>, Enumeration<Entry<K, V>> {
		
		@Override
		public Entry<K, V> next() {
			return super.getNext();
		}
		
		@Override
		public Entry<K, V> nextElement() {
			return this.next();
		}
	}
	
	@SuppressWarnings("unchecked")
	final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		
		@Override
		public void clear() {
			ArrayOrderedMap.this.clear();
		}
		
		@Override
		public boolean contains(final Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			final V v = ArrayOrderedMap.this.get(e.getKey());
			return (v != null) && v.equals(e.getValue());
		}
		
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}
		
		@Override
		public boolean remove(final Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			return ArrayOrderedMap.this.remove(e.getKey()) != null;
		}
		
		@Override
		public int size() {
			return ArrayOrderedMap.this.size();
		}
		
		@Override
		public Object[] toArray() {
			// Since we don't ordinarily have distinct Entry objects, we
			// must pack elements using exportable SimpleEntry
			final Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(this.size());
			for (final Iterator<Map.Entry<K, V>> i = this.iterator(); i.hasNext();) {
				c.add(new SimpleEntry<K, V>(i.next()));
			}
			return c.toArray();
		}
		
		@Override
		public <T> T[] toArray(final T[] a) {
			final Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(this.size());
			for (final Iterator<Map.Entry<K, V>> i = this.iterator(); i.hasNext();) {
				c.add(new SimpleEntry<K, V>(i.next()));
			}
			return c.toArray(a);
		}
		
	}
	
	final class KeyIterator extends CoreIterator implements Iterator<K>, Enumeration<K> {
		
		@Override
		public K next() {
			return super.getNext().getKey();
		}
		
		@Override
		public K nextElement() {
			return this.next();
		}
	}
	
	final class KeySet extends AbstractSet<K> {
		
		@Override
		public void clear() {
			ArrayOrderedMap.this.clear();
		}
		
		@Override
		public boolean contains(final Object o) {
			return ArrayOrderedMap.this.containsKey(o);
		}
		
		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}
		
		@Override
		public boolean remove(final Object o) {
			return ArrayOrderedMap.this.remove(o) != null;
		}
		
		@Override
		public int size() {
			return ArrayOrderedMap.this.size();
		}
		
		@Override
		public Object[] toArray() {
			final Collection<K> c = new ArrayList<K>();
			for (final Iterator<K> i = this.iterator(); i.hasNext();) {
				c.add(i.next());
			}
			return c.toArray();
		}
		
		@Override
		public <T> T[] toArray(final T[] a) {
			final Collection<K> c = new ArrayList<K>();
			for (final Iterator<K> i = this.iterator(); i.hasNext();) {
				c.add(i.next());
			}
			return c.toArray(a);
		}
	}
	
	/**
	 * This duplicates java.util.AbstractMap.SimpleEntry until this class
	 * is made accessible.
	 */
	static final class SimpleEntry<K, V> implements Entry<K, V> {
		
		K key;
		V value;
		
		public SimpleEntry(final Entry<K, V> e) {
			this.key = e.getKey();
			this.value = e.getValue();
		}
		
		public SimpleEntry(final K key, final V value) {
			this.key = key;
			this.value = value;
		}
		
		static boolean eq(final Object o1, final Object o2) {
			return (o1 == null ? o2 == null : o1.equals(o2));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(final Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry e = (Map.Entry) o;
			return SimpleEntry.eq(this.key, e.getKey()) && SimpleEntry.eq(this.value, e.getValue());
		}
		
		@Override
		public K getKey() {
			return this.key;
		}
		
		@Override
		public V getValue() {
			return this.value;
		}
		
		@Override
		public int hashCode() {
			return ((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode());
		}
		
		@Override
		public V setValue(final V value) {
			final V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
		
		@Override
		public String toString() {
			return this.key + "=" + this.value;
		}
	}
	
	final class ValueIterator extends CoreIterator implements Iterator<V>, Enumeration<V> {
		
		@Override
		public V next() {
			return super.getNext().getValue();
		}
		
		@Override
		public V nextElement() {
			return this.next();
		}
	}
	
	final class Values extends AbstractCollection<V> {
		
		@Override
		public void clear() {
			ArrayOrderedMap.this.clear();
		}
		
		@Override
		public boolean contains(final Object o) {
			return ArrayOrderedMap.this.containsValue(o);
		}
		
		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}
		
		@Override
		public int size() {
			return ArrayOrderedMap.this.size();
		}
		
		@Override
		public Object[] toArray() {
			final Collection<V> c = new ArrayList<V>();
			for (final Iterator<V> i = this.iterator(); i.hasNext();) {
				c.add(i.next());
			}
			return c.toArray();
		}
		
		@Override
		public <T> T[] toArray(final T[] a) {
			final Collection<V> c = new ArrayList<V>();
			for (final Iterator<V> i = this.iterator(); i.hasNext();) {
				c.add(i.next());
			}
			return c.toArray(a);
		}
	}
	
	public static final long serialVersionUID = 1l;
	
	transient Set<Map.Entry<K, V>> entrySet;
	
	transient Set<K> keySet;
	
	transient Collection<V> values;
	
	private ArrayList<K> list;
	
	private String name = "entity";
	
	public ArrayOrderedMap() {
		this(10);
	}
	
	public ArrayOrderedMap(final int initialCapacity) {
		super(initialCapacity);
		this.list = new ArrayList<K>(initialCapacity);
	}
	
	public ArrayOrderedMap(final Map<K, V> map) {
		this(map.size());
		for (final Entry<K, V> entry : map.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		this.list.clear();
	}
	
	public Enumeration<V> elements() {
		return new ValueIterator();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<Map.Entry<K, V>> entrySet() {
		final Set<Map.Entry<K, V>> es = this.entrySet;
		return (es != null) ? es : (this.entrySet = (Set) new EntrySet());
	}
	
	/**
	 * @return a list of all the entries in this map in the order they were
	 *         created
	 */
	@Override
	public List<Entry<K, V>> getEntries() {
		final ArrayList<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
		for (final K key : this.list) {
			final Entry<K, V> entry = new SimpleEntry<K, V>(key, this.get(key));
			entries.add(entry);
		}
		return entries;
	}
	
	/**
	 * Get an entry based on the position it is in the map (based on the order
	 * entries were created)
	 * 
	 * @param position the position in the map (must be less that the size)
	 * @return the entry at that position
	 * @throws IllegalArgumentException if the position is greater than the map
	 *           size
	 */
	@Override
	public Entry<K, V> getEntry(final int position) {
		if (position >= this.list.size()) {
			throw new IllegalArgumentException("Value is too large for the map size: " + this.list.size());
		}
		final K key = this.list.get(position);
		final Entry<K, V> entry = new SimpleEntry<K, V>(key, this.get(key));
		return entry;
	}
	
	/**
	 * @return a list of all the keys in this map in the order they were entered
	 */
	@Override
	public List<K> getKeys() {
		return new ArrayList<K>(this.list);
	}
	
	// Iterator support
	
	@Override
	public String getName() {
		return this.name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.azeckoski.reflectutils.map.OrderedMap#getValues()
	 */
	@Override
	public List<V> getValues() {
		return new ArrayList<V>(this.values());
	}
	
	public Enumeration<K> keys() {
		return new KeyIterator();
	}
	
	@Override
	public Set<K> keySet() {
		final Set<K> ks = this.keySet;
		return (ks != null) ? ks : (this.keySet = new KeySet());
	}
	
	// All below copied from CHM
	
	@Override
	public V put(final K key, final V value) {
		final V v = super.put(key, value);
		if (v != null) {
			// displaced
			this.list.remove(key);
		}
		this.list.add(key);
		return v;
	}
	
	@Override
	public V remove(final Object key) {
		final V v = super.remove(key);
		if (v != null) {
			this.list.remove(key);
		}
		return v;
	}
	
	/**
	 * @param name the name to use when encoding this map of entities
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
	}
	
	@Override
	public Collection<V> values() {
		final Collection<V> vs = this.values;
		return (vs != null) ? vs : (this.values = new Values());
	}
	
}
