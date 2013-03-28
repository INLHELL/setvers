package de.bitub.proitbau.core.model.domain;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.bitub.proitbau.common.model.templateinstance.iTemplateInstance;
import de.bitub.proitbau.core.model.domain.process.edge.SemanticEdge;

@JsonAutoDetect(
	getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE,
	setterVisibility = Visibility.NONE,
	fieldVisibility = Visibility.ANY,
	creatorVisibility = Visibility.NONE)
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(
	use = Id.NAME,
	include = As.PROPERTY,
	property = "T")
@JsonSubTypes({
	@Type(
		value = aNamedModelObject.class),
	@Type(
		value = SemanticEdge.class),
})
public interface aTemplateInstanceMixIn {
	
	@JsonIgnore
	Set<iTemplateInstance> getInstances();
	
	@JsonDeserialize(
		as = aTemplateInstance.class)
	void setTemplate(final iTemplateInstance template);
}
