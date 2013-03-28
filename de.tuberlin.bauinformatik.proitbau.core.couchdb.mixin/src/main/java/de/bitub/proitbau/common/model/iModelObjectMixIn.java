package de.bitub.proitbau.common.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.bitub.proitbau.common.model.templateinstance.iTemplateInstance;
import de.bitub.proitbau.core.model.domain.aModelObject;
import de.bitub.proitbau.core.model.domain.structuring.StructuringNodePersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringObjectPersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringSetPersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringTreePersistable;

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
		value = aModelObject.class),
	@Type(
		value = StructuringNodePersistable.class),
	@Type(
		value = StructuringObjectPersistable.class),
	@Type(
		value = StructuringSetPersistable.class),
	@Type(
		value = StructuringTreePersistable.class),
	@Type(
		value = iTemplateInstance.class),
	@Type(
		value = iUpdateInformation.class),
})
public interface iModelObjectMixIn {
	
}
