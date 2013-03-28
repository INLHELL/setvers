package de.bitub.proitbau.core.model.domain.process.edge;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.bitub.proitbau.core.model.domain.process.node.aProcessNode;
import de.bitub.proitbau.core.model.domain.process.node.iNodeObject;

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
public interface SemanticEdgeMixIn {
	
	@JsonSetter
	void setPredecessor(final aProcessNode<? extends iNodeObject> predecessor);
	
	@JsonSetter
	void setSemantics(final enSemantics semantics);
	
	@JsonSetter
	void setSuccessor(final aProcessNode<? extends iNodeObject> successor);
}
