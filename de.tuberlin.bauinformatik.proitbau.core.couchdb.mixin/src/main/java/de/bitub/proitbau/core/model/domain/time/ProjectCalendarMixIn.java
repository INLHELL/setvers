package de.bitub.proitbau.core.model.domain.time;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.cache.Cache;

import de.bitub.proitbau.core.model.domain.time.ProjectCalendar.MyKey;

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
public interface ProjectCalendarMixIn {
	
	@JsonIgnore
	Cache<Long, TimeRange> getBackwardRangeCache();
	
	@JsonIgnore
	Cache<MyKey, Long> getDurationCache();
	
	@JsonIgnore
	Cache<MyKey, Long> getEndCache();
	
	@JsonIgnore
	Cache<Long, TimeRange> getForwardRangeCache();
	
	@JsonIgnore
	Cache<MyKey, Long> getStartCache();
}
