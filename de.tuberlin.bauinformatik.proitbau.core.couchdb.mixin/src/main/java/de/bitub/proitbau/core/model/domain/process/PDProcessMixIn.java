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
package de.bitub.proitbau.core.model.domain.process;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

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
public interface PDProcessMixIn {
	
}
