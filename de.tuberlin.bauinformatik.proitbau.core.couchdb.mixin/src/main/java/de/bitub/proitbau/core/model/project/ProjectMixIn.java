package de.bitub.proitbau.core.model.project;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

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
@JsonIdentityInfo(
	property = "_id",
	scope = Project.class,
	generator = ObjectIdGenerators.PropertyGenerator.class)
public interface ProjectMixIn {
	
	@JsonProperty("_rev")
	String getRevision();
	
	@JsonProperty("_id")
	String getUuid();
}
