/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 11.03.2013
 * Project: de.tuberlin.bauinformatik.proitbau.core.couchdb.mixin
 * E-mail: vladislav.fedotov@tu-berlin.de
 * Company: TU Berlin
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * "Vladislav Fedotov" - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.model.templateinstance;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.bitub.proitbau.common.model.iNamedModelObject;
import de.bitub.proitbau.core.model.domain.aTemplateInstance;
import de.bitub.proitbau.core.model.domain.iPDTemplateInstance;
import de.bitub.proitbau.core.model.domain.iPITemplateInstance;
import de.bitub.proitbau.core.model.domain.process.edge.iSemanticEdge;
import de.bitub.proitbau.core.model.domain.process.node.iProcessNode;

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
		value = iNamedModelObject.class),
	@Type(
		value = iPDTemplateInstance.class),
	@Type(
		value = iPITemplateInstance.class),
	@Type(
		value = iProcessNode.class),
	@Type(
		value = iSemanticEdge.class)
})
public interface iTemplateInstanceMixIn {
	
}
