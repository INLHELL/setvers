package de.bitub.proitbau.common.versioning.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class ModelCache {
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static ModelCache instance = new ModelCache();
	}
	
	@SuppressWarnings("synthetic-access")
	public static ModelCache getInstance() {
		return Handler.instance;
	}
	
	private final Map<Field, String> fieldNames = Maps.newHashMapWithExpectedSize(100);
	
	private final Multimap<Class<?>, Field> nonStaticFields = HashMultimap.create(30, 20);
	
	private final Multimap<Class<?>, Field> comaparableFields = HashMultimap.create(30, 20);
	
	private final Map<Field, Boolean> isFieldTransient = Maps.newHashMapWithExpectedSize(1000);
	
	private final Map<Field, Boolean> isFieldVisible = Maps.newHashMapWithExpectedSize(100);
	
	private final Map<Object, UUID> objectsUUID = Maps.newHashMapWithExpectedSize(10000);
	
	private final Map<Field, Integer> orderIndexesOfFields = Maps.newHashMapWithExpectedSize(100);
	
	private final Map<Class<?>, String> superclassNames = Maps.newHashMapWithExpectedSize(100);
	
	private final Map<Class<?>, String> versionedEntityNames = Maps.newHashMapWithExpectedSize(100);
	
	private final Cache<Object, Set<Object>> objectValues = CacheBuilder.newBuilder().maximumSize(20000)
		.expireAfterWrite(5, TimeUnit.MINUTES).build();
	
	private ModelCache() {
	}
	
	public void addComarableField(final Class<?> cls, final Field field) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		Preconditions.checkNotNull(field, "Given field is null!");
		this.comaparableFields.put(cls, field);
	}
	
	public void addComarableFields(final Class<?> cls, final Collection<Field> fields) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		Preconditions.checkNotNull(fields, "Given collection of fields is null!");
		Preconditions.checkArgument(!fields.isEmpty(), "Given collection of fields is empty!");
		this.comaparableFields.putAll(cls, fields);
	}
	
	public boolean addNonStaticField(final Class<?> cls, final Field field) {
		if ((cls != null) && (field != null)) {
			return this.nonStaticFields.put(cls, field);
		}
		return false;
	}
	
	public boolean addFieldName(final Field field, final String fieldName) {
		if ((field != null) && (fieldName != null) && !this.fieldNames.containsKey(field)) {
			this.fieldNames.put(field, fieldName);
			return true;
		}
		return false;
	}
	
	public boolean addNonStaticFields(final Class<?> cls, final Collection<Field> fields) {
		if ((cls != null) && (fields != null) && (!fields.isEmpty()) && !this.nonStaticFields.containsKey(cls)) {
			this.nonStaticFields.putAll(cls, fields);
			return true;
		}
		return false;
	}
	
	public boolean addObjectUuid(final Object object, final UUID uuid) {
		if ((object != null) && (uuid != null) && !this.objectsUUID.containsKey(object)) {
			this.objectsUUID.put(object, uuid);
			return true;
		}
		return false;
	}
	
	public void addObjectValues(final Class<?> cls, final Field field) {
		Preconditions.checkNotNull(cls, "Given class is null!");
		Preconditions.checkNotNull(field, "Given field is null!");
		this.comaparableFields.put(cls, field);
	}
	
	public boolean addOrderIndexOfField(final Field field, final int orderIndex) {
		if ((field != null) && !this.orderIndexesOfFields.containsKey(field)) {
			this.orderIndexesOfFields.put(field, Integer.valueOf(orderIndex));
			return true;
		}
		return false;
	}
	
	public boolean addSuperclassName(final Class<?> cls, final String superclassName) {
		if ((cls != null) && !this.superclassNames.containsKey(cls)) {
			this.superclassNames.put(cls, superclassName);
			return true;
		}
		return false;
	}
	
	public boolean addTransientBindingField(final Field field, final Boolean result) {
		if ((field != null)) {
			this.isFieldTransient.put(field, result);
			return true;
		}
		return false;
	}
	
	public boolean addVersionedEntityName(final Class<?> cls, final String versionedEntityName) {
		if ((cls != null) && (versionedEntityName != null) && !this.versionedEntityNames.containsKey(cls)) {
			this.versionedEntityNames.put(cls, versionedEntityName);
			return true;
		}
		return false;
	}
	
	public boolean addVisibilityOfField(final Field field, final boolean visible) {
		if ((field != null) && !this.isFieldVisible.containsKey(field)) {
			this.isFieldVisible.put(field, Boolean.valueOf(visible));
			return true;
		}
		return false;
	}
	
	public boolean containsComparableField(final Class<?> cls) {
		if ((cls != null) && (this.comaparableFields.containsKey(cls))) {
			return true;
		}
		return false;
	}
	
	public boolean containsNonStaticField(final Class<?> cls) {
		if ((cls != null) && this.nonStaticFields.containsKey(cls)) {
			return true;
		}
		return false;
	}
	
	public boolean containsStaticField(final Field field) {
		if ((field != null) && this.nonStaticFields.containsValue(field)) {
			return true;
		}
		return false;
	}
	
	public boolean containsFieldName(final Field field) {
		if ((field != null) && this.fieldNames.containsKey(field)) {
			return true;
		}
		return false;
	}
	
	public boolean containsFieldName(final String fieldName) {
		if ((fieldName != null) && this.fieldNames.containsValue(fieldName)) {
			return true;
		}
		return false;
	}
	
	public boolean containsObjectUuid(final Object object) {
		if ((this.objectsUUID != null) && this.objectsUUID.containsKey(object)) {
			return true;
		}
		return false;
	}
	
	public boolean containsObjectUUID(final UUID uuid) {
		if ((this.objectsUUID != null) && this.objectsUUID.containsValue(uuid)) {
			return true;
		}
		return false;
	}
	
	public boolean containsOrderIndexOfField(final Field field) {
		if ((field != null) && this.orderIndexesOfFields.containsKey(field)) {
			return true;
		}
		return false;
	}
	
	public boolean containsSuperclassName(final Class<?> cls) {
		if ((cls != null) && this.superclassNames.containsKey(cls)) {
			return true;
		}
		return false;
	}
	
	public boolean containsSuperclassName(final String name) {
		if ((name != null) && this.superclassNames.containsValue(name)) {
			return true;
		}
		return false;
	}
	
	public boolean containsTransientField(final Field field) {
		if ((field != null) && this.isFieldTransient.containsKey(field)) {
			return true;
		}
		return false;
	}
	
	public boolean containsVersionedEntityName(final Class<?> cls) {
		if ((cls != null) && (this.versionedEntityNames != null) && this.versionedEntityNames.containsKey(cls)) {
			return true;
		}
		return false;
	}
	
	public boolean containsVersionedEntutyName(final String name) {
		if ((name != null) && this.versionedEntityNames.containsValue(name)) {
			return true;
		}
		return false;
	}
	
	public boolean containsVisibleField(final Field field) {
		if ((field != null) && this.isFieldVisible.containsKey(field)) {
			return true;
		}
		return false;
	}
	
	public Collection<Field> getComparableFields(final Class<?> cls) {
		return this.comaparableFields.get(cls);
	}
	
	public String getFieldName(final Field field) {
		return this.fieldNames.get(field);
	}
	
	public Collection<Field> getNonStaticFields(final Class<?> cls) {
		return this.nonStaticFields.get(cls);
	}
	
	public UUID getObjectUuid(final Object object) {
		return this.objectsUUID.get(object);
	}
	
	public Cache<Object, Set<Object>> getObjectValues() {
		return this.objectValues;
	}
	
	public int getOrderIndexOfField(final Field field) {
		return this.orderIndexesOfFields.get(field).intValue();
	}
	
	public String getSuperclassName(final Class<?> cls) {
		return this.superclassNames.get(cls);
	}
	
	public String getVersionedEntityName(final Class<?> cls) {
		return this.versionedEntityNames.get(cls);
	}
	
	public void invalidateObjectValuesCache() {
		this.objectValues.invalidateAll();
	}
	
	public boolean isFieldComparable(final Field field) {
		return this.comaparableFields.containsValue(field);
	}
	
	public boolean isFieldTransient(final Field field) {
		return this.isFieldTransient.get(field).booleanValue();
	}
	
	public boolean isFieldVisible(final Field field) {
		return this.isFieldVisible.get(field).booleanValue();
	}
	
}
