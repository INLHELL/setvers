package de.bitub.proitbau.core.model.domain;

import java.util.Set;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.bitub.proitbau.common.model.iProjectCalendar;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringSet;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringTree;
import de.bitub.proitbau.core.model.domain.process.iPDProcessComponent;
import de.bitub.proitbau.core.model.domain.structuring.StructuringSetPersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringTreePersistable;
import de.bitub.proitbau.core.model.domain.time.ProjectCalendar;

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
	scope = DomainModel.class,
	generator = ObjectIdGenerators.PropertyGenerator.class)
public interface DomainModelMixIn {
	
	@JsonSerialize(
		as = StructuringSetPersistable.class)
	iStructuringSet<iPDProcessComponent> getPdProcessComponents();
	
	@JsonSerialize(
		contentAs = StructuringTreePersistable.class)
	Set<iStructuringTree> getPdStructuringTrees();
	
	@JsonSerialize(
		contentAs = StructuringTreePersistable.class)
	Set<iStructuringTree> getPiStructuringTrees();
	
	@JsonProperty("_rev")
	String getRevision();
	
	@JsonProperty("_id")
	String getUuid();
	
	@JsonDeserialize(
		as = StructuringSetPersistable.class)
	void setPdProcessComponents(final iStructuringSet<iPDProcessComponent> pdProcessComponents);
	
	@JsonDeserialize(
		contentAs = StructuringTreePersistable.class)
	void setPdStructuringTrees(final Set<iStructuringTree> structuringTrees);
	
	@JsonDeserialize(
		contentAs = StructuringTreePersistable.class)
	void setPiStructuringTrees(final Set<iStructuringTree> structuringTrees);
	
	@JsonDeserialize(
		as = ProjectCalendar.class)
	void setTotalCalendar(final iProjectCalendar totalCalendar);
}
