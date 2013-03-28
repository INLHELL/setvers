/**
 * Copyright (C) 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.azeckoski.reflectutils.refmap;

import static org.azeckoski.reflectutils.refmap.ReferenceType.STRONG;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Concurrent hash map that wraps keys and/or values in soft or weak
 * references. Does not support null keys or values. Uses identity equality
 * for weak and soft keys.
 * 
 * <p>
 * The concurrent semantics of {@link ConcurrentHashMap} combined with the fact
 * that the garbage collector can asynchronously reclaim and clean up after keys
 * and values at any time can lead to some racy semantics. For example,
 * {@link #size()} returns an upper bound on the size, i.e. the actual size may
 * be smaller in cases where the key or value has been reclaimed but the map
 * entry has not been cleaned up yet.
 * 
 * <p>
 * Another example: If {@link #get(Object)} cannot find an existing entry for a
 * key, it will try to create one. This operation is not atomic. One thread
 * could {@link #put(Object, Object)} a value between the time another thread
 * running {@code get()} checks for an entry and decides to create one. In this
 * case, the newly created value will replace the put value in the map. Also,
 * two threads running {@code get()} concurrently can potentially create
 * duplicate values for a given key.
 * 
 * <p>
 * In other words, this class is great for caching but not atomicity.
 * 
 * <p>
 * To determine equality to a key, this implementation uses
 * {@link Object#equals} for strong references, and identity-based equality for
 * soft and weak references. In other words, for a map with weak or soft key
 * references, {@link #get} returns {@code null} when passed an object that
 * equals a map key, but isn't the same instance. This behavior is similar to
 * the way {@link IdentityHashMap} handles key lookups. However, to determine
 * value equality, as occurs when {@link #containsValue} is called, the
 * {@code ReferenceMap} always uses {@code equals}, regardless of the value
 * reference type.
 * 
 * <p>
 * <b>Note:</b> {@code new ReferenceMap(WEAK, STRONG)} is very nearly a drop-in
 * replacement for {@link WeakHashMap}, but improves upon this by using only
 * identity-based equality for keys. When possible, {@code ReferenceMap} should
 * be preferred over the JDK collection, for its concurrency and greater
 * flexibility.
 * 
 * @author crazybob@google.com (Bob Lee)
 */
@SuppressWarnings("unchecked")
public class ReferenceMap<K, V> implements Map<K, V>, Serializable {
	
	protected enum PutStrategy implements Strategy {
		PUT {
			
			@Override
			public Object execute(final ReferenceMap map, final Object keyReference, final Object valueReference) {
				return map.delegate.put(keyReference, valueReference);
			}
		},
		
		PUT_IF_ABSENT {
			
			@Override
			public Object execute(final ReferenceMap map, final Object keyReference, final Object valueReference) {
				return map.delegate.putIfAbsent(keyReference, valueReference);
			}
		},
		
		REPLACE {
			
			@Override
			public Object execute(final ReferenceMap map, final Object keyReference, final Object valueReference) {
				return map.delegate.replace(keyReference, valueReference);
			}
		};
	}
	
	protected interface Strategy {
		
		public Object execute(ReferenceMap map, Object keyReference, Object valueReference);
	}
	
	class Entry implements Map.Entry<K, V> {
		
		final K key;
		final V value;
		
		public Entry(final K key, final V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof ReferenceMap.Entry)) {
				return false;
			}
			
			final Entry entry = (Entry) o;
			return this.key.equals(entry.key) && this.value.equals(entry.value);
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
			return (this.key.hashCode() * 31) + this.value.hashCode();
		}
		
		@Override
		public V setValue(final V value) {
			return ReferenceMap.this.put(this.key, value);
		}
		
		@Override
		public String toString() {
			return this.key + "=" + this.value;
		}
	}
	/**
	 * Used for keys. Overrides hash code to use identity hash code.
	 */
	static class KeyReferenceAwareWrapper extends ReferenceAwareWrapper {
		
		public KeyReferenceAwareWrapper(final Object wrapped) {
			super(wrapped);
		}
		
		@Override
		public boolean equals(final Object arg0) {
			return super.equals(arg0);
		}
		
		@Override
		public int hashCode() {
			return System.identityHashCode(this.wrapped);
		}
	}
	
	/**
	 * Big hack. Used to compare keys and values to referenced keys and values
	 * without creating more references.
	 */
	static class ReferenceAwareWrapper {
		
		final Object wrapped;
		
		ReferenceAwareWrapper(final Object wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public boolean equals(final Object obj) {
			// defer to reference's equals() logic.
			return obj == null ? false : obj.equals(this);
		}
		
		@Override
		public int hashCode() {
			return this.wrapped.hashCode();
		}
		
		Object unwrap() {
			return this.wrapped;
		}
	}
	
	/*
	 * Marker interface to differentiate external and internal references. Also
	 * duplicates finalizeReferent() and Reference.get() for internal use.
	 */
	private interface InternalReference {
		
		void finalizeReferent();
		
		Object get();
	}
	
	/**
	 * Lazy initialization holder for finalizable reference queue.
	 */
	private static class ReferenceQueue {
		
		private static final FinalizableReferenceQueue instance = new FinalizableReferenceQueue();
	}
	
	private class SoftKeyReference extends FinalizableSoftReference<Object> implements InternalReference {
		
		final int hashCode;
		
		SoftKeyReference(final Object key) {
			super(key, ReferenceQueue.instance);
			this.hashCode = System.identityHashCode(key);
		}
		
		@Override
		public boolean equals(final Object object) {
			return ReferenceMap.referenceEquals(this, object);
		}
		
		@Override
		public void finalizeReferent() {
			ReferenceMap.this.delegate.remove(this);
		}
		
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}
	
	private class SoftValueReference extends FinalizableSoftReference<Object> implements InternalReference {
		
		final Object keyReference;
		
		SoftValueReference(final Object keyReference, final Object value) {
			super(value, ReferenceQueue.instance);
			this.keyReference = keyReference;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return ReferenceMap.referenceEquals(this, obj);
		}
		
		@Override
		public void finalizeReferent() {
			ReferenceMap.this.delegate.remove(this.keyReference, this);
		}
		
		@Override
		public int hashCode() {
			// It's hard to define a useful hash code, so we're careful not to use it.
			throw new AssertionError("don't hash me");
		}
	}
	
	private class WeakKeyReference extends FinalizableWeakReference<Object> implements InternalReference {
		
		final int hashCode;
		
		WeakKeyReference(final Object key) {
			super(key, ReferenceQueue.instance);
			this.hashCode = System.identityHashCode(key);
		}
		
		@Override
		public boolean equals(final Object object) {
			return ReferenceMap.referenceEquals(this, object);
		}
		
		@Override
		public void finalizeReferent() {
			ReferenceMap.this.delegate.remove(this);
		}
		
		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}
	
	private class WeakValueReference extends FinalizableWeakReference<Object> implements InternalReference {
		
		final Object keyReference;
		
		WeakValueReference(final Object keyReference, final Object value) {
			super(value, ReferenceQueue.instance);
			this.keyReference = keyReference;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return ReferenceMap.referenceEquals(this, obj);
		}
		
		@Override
		public void finalizeReferent() {
			ReferenceMap.this.delegate.remove(this.keyReference, this);
		}
		
		@Override
		public int hashCode() {
			// It's hard to define a useful hash code, so we're careful not to use it.
			throw new AssertionError("don't hash me");
		}
	}
	
	private static PutStrategy defaultPutStrategy;
	
	private static final long serialVersionUID = 0;
	
	transient ConcurrentMap<Object, Object> delegate;
	
	final ReferenceType keyReferenceType;
	
	final ReferenceType valueReferenceType;
	
	/**
	 * Concurrent hash map that wraps keys and/or values based on specified
	 * reference types.
	 * 
	 * @param keyReferenceType key reference type
	 * @param valueReferenceType value reference type
	 */
	public ReferenceMap(final ReferenceType keyReferenceType, final ReferenceType valueReferenceType) {
		ReferenceMap.ensureNotNull(keyReferenceType, valueReferenceType);
		
		if ((keyReferenceType == ReferenceType.PHANTOM) || (valueReferenceType == ReferenceType.PHANTOM)) {
			throw new IllegalArgumentException("Phantom references not supported.");
		}
		
		this.delegate = new ConcurrentHashMap<Object, Object>();
		this.keyReferenceType = keyReferenceType;
		this.valueReferenceType = valueReferenceType;
	}
	
	public static void setDefaultPutStrategy(final PutStrategy defaultPutStrategy) {
		ReferenceMap.defaultPutStrategy = defaultPutStrategy;
	}
	
	static void ensureNotNull(final Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}
	
	static void ensureNotNull(final Object... array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				throw new NullPointerException("Argument #" + i + " is null.");
			}
		}
	}
	
	static int keyHashCode(final Object key) {
		return System.identityHashCode(key);
	}
	
	/**
	 * Tests weak and soft references for identity equality. Compares references
	 * to other references and wrappers. If o is a reference, this returns true
	 * if r == o or if r and o reference the same non null object. If o is a
	 * wrapper, this returns true if r's referent is identical to the wrapped
	 * object.
	 */
	static boolean referenceEquals(final Reference r, final Object o) {
		// compare reference to reference.
		if (o instanceof InternalReference) {
			// are they the same reference? used in cleanup.
			if (o == r) {
				return true;
			}
			
			// do they reference identical values? used in conditional puts.
			final Object referent = ((Reference) o).get();
			return (referent != null) && (referent == r.get());
		}
		
		// is the wrapped object identical to the referent? used in lookups.
		return ((ReferenceAwareWrapper) o).unwrap() == r.get();
	}
	
	@Override
	public void clear() {
		this.delegate.clear();
	}
	
	@Override
	public boolean containsKey(final Object key) {
		ReferenceMap.ensureNotNull(key);
		final Object referenceAwareKey = this.makeKeyReferenceAware(key);
		return this.delegate.containsKey(referenceAwareKey);
	}
	
	@Override
	public boolean containsValue(final Object value) {
		ReferenceMap.ensureNotNull(value);
		for (final Object valueReference : this.delegate.values()) {
			if (value.equals(this.dereferenceValue(valueReference))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an unmodifiable set view of the entries in this map. As this
	 * method creates a defensive copy, the performance is O(n).
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		final Set<Map.Entry<K, V>> entrySet = new HashSet<Map.Entry<K, V>>();
		for (final Map.Entry<Object, Object> entry : this.delegate.entrySet()) {
			final Map.Entry<K, V> dereferenced = this.dereferenceEntry(entry);
			if (dereferenced != null) {
				entrySet.add(dereferenced);
			}
		}
		return Collections.unmodifiableSet(entrySet);
	}
	
	@Override
	public V get(final Object key) {
		ReferenceMap.ensureNotNull(key);
		return this.internalGet((K) key);
	}
	
	@Override
	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}
	
	/**
	 * Returns an unmodifiable set view of the keys in this map. As this method
	 * creates a defensive copy, the performance is O(n).
	 */
	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(this.dereferenceKeySet(this.delegate.keySet()));
	}
	
	@Override
	public V put(final K key, final V value) {
		return this.execute(this.putStrategy(), key, value);
	}
	
	@Override
	public void putAll(final Map<? extends K, ? extends V> t) {
		for (final Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	public V putIfAbsent(final K key, final V value) {
		// TODO (crazybob) if the value has been gc'ed but the entry hasn't been
		// cleaned up yet, this put will fail.
		return this.execute(this.putIfAbsentStrategy(), key, value);
	}
	
	@Override
	public V remove(final Object key) {
		ReferenceMap.ensureNotNull(key);
		final Object referenceAwareKey = this.makeKeyReferenceAware(key);
		final Object valueReference = this.delegate.remove(referenceAwareKey);
		return valueReference == null ? null : (V) this.dereferenceValue(valueReference);
	}
	
	public boolean remove(final Object key, final Object value) {
		ReferenceMap.ensureNotNull(key, value);
		final Object referenceAwareKey = this.makeKeyReferenceAware(key);
		final Object referenceAwareValue = this.makeValueReferenceAware(value);
		return this.delegate.remove(referenceAwareKey, referenceAwareValue);
	}
	
	public V replace(final K key, final V value) {
		// TODO (crazybob) if the value has been gc'ed but the entry hasn't been
		// cleaned up yet, this will succeed when it probably shouldn't.
		return this.execute(this.replaceStrategy(), key, value);
	}
	
	public boolean replace(final K key, final V oldValue, final V newValue) {
		ReferenceMap.ensureNotNull(key, oldValue, newValue);
		final Object keyReference = this.referenceKey(key);
		
		final Object referenceAwareOldValue = this.makeValueReferenceAware(oldValue);
		return this.delegate.replace(keyReference, referenceAwareOldValue, this.referenceValue(keyReference, newValue));
	}
	
	@Override
	public int size() {
		return this.delegate.size();
	}
	
	/**
	 * Returns an unmodifiable set view of the values in this map. As this
	 * method creates a defensive copy, the performance is O(n).
	 */
	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(this.dereferenceValues(this.delegate.values()));
	}
	
	protected PutStrategy getPutStrategy() {
		return ReferenceMap.defaultPutStrategy;
	}
	
	protected Strategy putIfAbsentStrategy() {
		return PutStrategy.PUT_IF_ABSENT;
	}
	
	protected Strategy putStrategy() {
		return PutStrategy.PUT;
	}
	
	protected Strategy replaceStrategy() {
		return PutStrategy.REPLACE;
	}
	
	/*
	 * WeakKeyReference/WeakValueReference are absolutely identical to
	 * SoftKeyReference/SoftValueReference except for which classes they extend.
	 */
	
	/**
	 * Returns the refererent for reference given its reference type.
	 */
	Object dereference(final ReferenceType referenceType, final Object reference) {
		return referenceType == STRONG ? reference : ((Reference) reference).get();
	}
	
	/**
	 * Dereferences elements in {@code in} using {@code referenceType} and puts
	 * them in {@code out}. Returns {@code out}.
	 */
	<T extends Collection<Object>> T dereferenceCollection(final ReferenceType referenceType, final T in, final T out) {
		for (final Object reference : in) {
			out.add(this.dereference(referenceType, reference));
		}
		return out;
	}
	
	/**
	 * Dereferences an entry. Returns null if the key or value has been gc'ed.
	 */
	Entry dereferenceEntry(final Map.Entry<Object, Object> entry) {
		final K key = this.dereferenceKey(entry.getKey());
		final V value = this.dereferenceValue(entry.getValue());
		return ((key == null) || (value == null)) ? null : new Entry(key, value);
	}
	
	/**
	 * Converts a reference to a key.
	 */
	K dereferenceKey(final Object o) {
		return (K) this.dereference(this.keyReferenceType, o);
	}
	
	/**
	 * Dereferences a set of key references.
	 */
	Set<K> dereferenceKeySet(final Set keyReferences) {
		return this.keyReferenceType == STRONG ? keyReferences : this.dereferenceCollection(this.keyReferenceType, keyReferences,
			new HashSet());
	}
	
	/**
	 * Converts a reference to a value.
	 */
	V dereferenceValue(final Object o) {
		return (V) this.dereference(this.valueReferenceType, o);
	}
	
	/**
	 * Dereferences a collection of value references.
	 */
	Collection<V> dereferenceValues(final Collection valueReferences) {
		return this.valueReferenceType == STRONG ? valueReferences : this.dereferenceCollection(this.valueReferenceType, valueReferences,
			new ArrayList(valueReferences.size()));
	};
	
	V execute(final Strategy strategy, final K key, final V value) {
		ReferenceMap.ensureNotNull(key, value);
		final Object keyReference = this.referenceKey(key);
		final Object valueReference = strategy.execute(this, keyReference, this.referenceValue(keyReference, value));
		return valueReference == null ? null : (V) this.dereferenceValue(valueReference);
	}
	
	V internalGet(final K key) {
		final Object valueReference = this.delegate.get(this.makeKeyReferenceAware(key));
		return valueReference == null ? null : (V) this.dereferenceValue(valueReference);
	}
	
	/**
	 * Wraps key so it can be compared to a referenced key for equality.
	 */
	Object makeKeyReferenceAware(final Object o) {
		return this.keyReferenceType == STRONG ? o : new KeyReferenceAwareWrapper(o);
	}
	
	/**
	 * Wraps value so it can be compared to a referenced value for equality.
	 */
	Object makeValueReferenceAware(final Object o) {
		return this.valueReferenceType == STRONG ? o : new ReferenceAwareWrapper(o);
	}
	
	/**
	 * Creates a reference for a key.
	 */
	Object referenceKey(final K key) {
		switch (this.keyReferenceType) {
			case STRONG:
				return key;
			case SOFT:
				return new SoftKeyReference(key);
			case WEAK:
				return new WeakKeyReference(key);
			default:
				throw new AssertionError();
		}
	}
	
	/**
	 * Creates a reference for a value.
	 */
	Object referenceValue(final Object keyReference, final Object value) {
		switch (this.valueReferenceType) {
			case STRONG:
				return value;
			case SOFT:
				return new SoftValueReference(keyReference, value);
			case WEAK:
				return new WeakValueReference(keyReference, value);
			default:
				throw new AssertionError();
		}
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		final int size = in.readInt();
		this.delegate = new ConcurrentHashMap<Object, Object>(size);
		while (true) {
			final K key = (K) in.readObject();
			if (key == null) {
				break;
			}
			final V value = (V) in.readObject();
			this.put(key, value);
		}
	}
	
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(this.size());
		for (final Map.Entry<Object, Object> entry : this.delegate.entrySet()) {
			final Object key = this.dereferenceKey(entry.getKey());
			final Object value = this.dereferenceValue(entry.getValue());
			
			// don't persist gc'ed entries.
			if ((key != null) && (value != null)) {
				out.writeObject(key);
				out.writeObject(value);
			}
		}
		out.writeObject(null);
	}
	
}
