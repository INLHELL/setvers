package de.bitub.proitbau.common.model.bean;

import java.beans.PropertyChangeSupport;

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

import de.bitub.proitbau.common.model.datastructure.structuring.StructuringNode;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringObject;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringSet;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringTree;
import de.bitub.proitbau.core.model.domain.aModelObject;

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
		value = StructuringNode.class),
	@Type(
		value = StructuringObject.class),
	@Type(
		value = StructuringSet.class),
	@Type(
		value = StructuringTree.class),
})
public interface aObservableMixIn {
	
	@JsonIgnore
	int getModCount();
	
	@JsonIgnore
	PropertyChangeSupport getPropertyChangeSupport();
}
