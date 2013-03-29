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
package de.bitub.proitbau.common.versioning.compare_results;

import java.lang.reflect.Field;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class FieldResult implements iResultable<Object> {
	
	private String name = "";
	
	private Object firstValue = null;
	
	private Object secondValue = null;
	
	private Field field = null;
	
	private int orderIndex = 0;
	
	private boolean visible = true;
	
	private boolean equal = true;
	
	private ModificationType modificationType = ModificationType.INVARIABLE;
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(final String name) {
		Preconditions.checkNotNull(name, "Given name is null");
		this.name = name;
	}
	
	@Override
	public Object getFirst() {
		return this.firstValue;
	}
	
	@Override
	public void setFirst(final Object firstValue) {
		this.firstValue = firstValue;
	}
	
	@Override
	public Object getSecond() {
		return this.secondValue;
	}
	
	@Override
	public void setSecond(final Object secondValue) {
		this.secondValue = secondValue;
	}
	
	public Field getField() {
		return this.field;
	}
	
	public void setField(final Field field) {
		this.field = field;
	}
	
	public int getOrderIndex() {
		return this.orderIndex;
	}
	
	public void setOrderIndex(final int orderIndex) {
		this.orderIndex = orderIndex;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void setVisible(final boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public boolean isEqual() {
		return this.equal;
	}
	
	@Override
	public void setEqual(final boolean equal) {
		this.equal = equal;
	}
	
	public ModificationType getModificationType() {
		return this.modificationType;
	}
	
	public void setModificationType(final ModificationType modificationType) {
		this.modificationType = modificationType;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		 return Objects.toStringHelper(this)
			 .addValue(this.name)
       .addValue(this.firstValue)
       .addValue(this.secondValue)
       .addValue(this.equal)
       .toString();
		// @formatter:on
	}
	
}
