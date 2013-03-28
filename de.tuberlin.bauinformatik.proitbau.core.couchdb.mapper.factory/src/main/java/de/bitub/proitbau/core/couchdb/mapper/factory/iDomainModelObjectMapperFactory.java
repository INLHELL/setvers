package de.bitub.proitbau.core.couchdb.mapper.factory;

import org.ektorp.impl.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface iDomainModelObjectMapperFactory extends ObjectMapperFactory {
	
	public ObjectMapper getObjectMapper();
	
	@Override
	ObjectMapper createObjectMapper();
}
