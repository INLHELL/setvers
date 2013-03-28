/*
 * Package: de.bitub.proitbau.common.versioning
 * Project: SetVers
 * File: iVersionedSetMixIn.java
 * Date: 24.07.2012
 * Time: 17:02:11
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
	property = "uuid",
	scope = VersionedSet.class,
	generator = ObjectIdGenerators.PropertyGenerator.class)
public interface VersionedSetMixIn {
	
	@JsonProperty("uuid")
	String getUuid();
	
	@JsonSetter
	@JsonDeserialize(
		contentAs = Versionable.class)
	void setVersionedObjects(final Set<Object> versionedObjects);
	
	@JsonGetter
	Set<Object> getVersionedObjects();
}
