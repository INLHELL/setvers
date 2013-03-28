package de.bitub.proitbau.core.model.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.bitub.proitbau.core.model.domain.activitystate.aActivity;
import de.bitub.proitbau.core.model.domain.activitystate.aState;
import de.bitub.proitbau.core.model.domain.subject.aSubject;

@JsonAutoDetect(
	getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE,
	setterVisibility = Visibility.NONE,
	fieldVisibility = Visibility.ANY,
	creatorVisibility = Visibility.NONE)
@JsonInclude(Include.NON_EMPTY)
@JsonSubTypes({
	@Type(
		value = aActivity.class),
	@Type(
		value = aChecklistedModelObject.class),
	@Type(
		value = aState.class),
	@Type(
		value = aSubject.class),
	@Type(
		value = ChecklistItem.class),
})
@JsonTypeInfo(
	use = Id.NAME,
	include = As.PROPERTY,
	property = "T")
public interface aNamedModelObjectMixIn {
	
}
