package de.bitub.proitbau.core.couchdb.mapper.factory.mapper;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

public class DomainModelObjectMapper {
	
	private ObjectMapper objectMapper;
	
	private DomainModelObjectMapper() {
		this.objectMapper = new ObjectMapper();
	}
	
	private static class Handler {
		
		private static DomainModelObjectMapper instance = new DomainModelObjectMapper();
	}
	
	public static DomainModelObjectMapper getInstance() {
		return Handler.instance;
	}
	
	public void addMixInAnnotation(final Class<?> domainModelClass, final Class<?> mixInClass) {
		Preconditions.checkNotNull(domainModelClass, "Domain model class is null!");
		Preconditions.checkNotNull(mixInClass, "MixIn class is null!");
		DomainModelObjectMapper.getInstance().objectMapper.addMixInAnnotations(domainModelClass, mixInClass);
	}
	
	public void addMixInAnnotations(final Map<Class<?>, Class<?>> classesAndMixIns) {
		Preconditions.checkNotNull(classesAndMixIns, "Domain model class is null!");
		Preconditions.checkArgument(classesAndMixIns.size() != 0, "The map of classes and mixins is empty!");
		for (final Map.Entry<Class<?>, Class<?>> pair : classesAndMixIns.entrySet()) {
			final Class<?> domainModelClass = pair.getKey();
			final Class<?> mixInClass = pair.getValue();
			DomainModelObjectMapper.getInstance().objectMapper.addMixInAnnotations(domainModelClass, mixInClass);
		}
	}
	
	public ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
	
	public void setObjectMapper(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
