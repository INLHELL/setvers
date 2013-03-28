package de.bitub.proitbau.core.couchdb.mapper.factory;

import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.impl.jackson.EktorpJacksonModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DomainModelObjectMapperFactory extends StdObjectMapperFactory implements iDomainModelObjectMapperFactory {
	
	private ObjectMapper instance;
	private final boolean writeDatesAsTimestamps = false;
	
	@Override
	public synchronized ObjectMapper createObjectMapper() {
		if (this.instance == null) {
			this.instance = new ObjectMapper();
			this.applyDefaultConfiguration(this.instance);
		}
		return this.instance;
	}
	
	@Override
	public ObjectMapper createObjectMapper(final CouchDbConnector connector) {
		final ObjectMapper objectMapper = this.createObjectMapper();
		objectMapper.registerModule(new EktorpJacksonModule(connector, objectMapper));
		return objectMapper;
	}
	
	@Override
	public ObjectMapper getObjectMapper() {
		return this.instance;
	}
	
	private void applyDefaultConfiguration(final ObjectMapper om) {
		om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, this.writeDatesAsTimestamps);
		om.getSerializationConfig().withSerializationInclusion(JsonInclude.Include.NON_NULL);
	}
}
