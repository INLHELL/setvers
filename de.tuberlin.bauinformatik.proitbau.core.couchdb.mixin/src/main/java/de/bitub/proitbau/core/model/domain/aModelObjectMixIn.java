package de.bitub.proitbau.core.model.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.bitub.proitbau.core.model.domain.activitystate.PDCondition;
import de.bitub.proitbau.core.model.domain.time.DailyShifts;
import de.bitub.proitbau.core.model.domain.time.ProjectCalendar;
import de.bitub.proitbau.core.model.domain.time.Shift;
import de.bitub.proitbau.core.model.domain.time.TimeRange;
import de.bitub.proitbau.core.model.domain.time.WeeklyShift;
import de.bitub.proitbau.core.model.project.Project;

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
		value = aTemplateInstance.class),
	@Type(
		value = DailyShifts.class),
	@Type(
		value = DomainModel.class),
	@Type(
		value = PDCondition.class),
	@Type(
		value = Project.class),
	@Type(
		value = ProjectCalendar.class),
	@Type(
		value = Shift.class),
	@Type(
		value = TimeRange.class),
	@Type(
		value = Update.class),
	@Type(
		value = UpdateInformation.class),
	@Type(
		value = WeeklyShift.class)
})
public interface aModelObjectMixIn {
	
	@JsonProperty("uuid")
	String getUuid();
}
