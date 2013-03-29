/*******************************************************************************
 * Author:		"Vladislav Fedotov"
 * Written:		2013
 * Project:		Setvers
 * E-mail:		vladislav.fedotov@tu-berlin.de
 * Company:		TU Berlin
 * Version:		1.0
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladislav Fedotov - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.versioning.model;

import com.google.common.base.Objects;

public class VersionedSetType {
	
	private Class<?> mainType;
	private Class<?> subType;
	
	public VersionedSetType() {
		super();
	}
	
	public VersionedSetType(final Class<?> mainType, final Class<?> subType) {
		super();
		this.mainType = mainType;
		this.subType = subType;
	}
	
	public VersionedSetType(final Class<?> mainType) {
		super();
		this.mainType = mainType;
		this.subType = null;
	}
	
	public Class<?> getMainType() {
		return this.mainType;
	}
	
	public void setMainType(final Class<?> classType) {
		this.mainType = classType;
	}
	
	public Class<?> getSubType() {
		return this.subType;
	}
	
	public void setSubType(final Class<?> subType) {
		this.subType = subType;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof VersionedSetType) {
			final VersionedSetType other = (VersionedSetType) obj;
			return Objects.equal(this.getMainType(), other.getMainType())
							&& Objects.equal(this.getSubType(), other.getSubType());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getMainType(), this.getSubType());
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.add("mainType", this.mainType)
			.add("subType", this.subType)
			.toString();
		// @formatter:on
	}
	
}
