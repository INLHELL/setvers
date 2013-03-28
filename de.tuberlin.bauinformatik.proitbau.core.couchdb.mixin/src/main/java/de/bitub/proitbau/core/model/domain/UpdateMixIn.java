package de.bitub.proitbau.core.model.domain;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.cache.Cache;

import de.bitub.proitbau.common.model.iUpdateInformation;
import de.bitub.proitbau.core.model.domain.activitystate.PDActivity;

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
public interface UpdateMixIn {
	
	@JsonIgnore
	Cache<PDActivity, Double> getUpdateInformationCache();
	
	@JsonDeserialize(
		contentAs = UpdateInformation.class)
	void setUpdateInformations(final Set<iUpdateInformation> updateInformations);
	
}
