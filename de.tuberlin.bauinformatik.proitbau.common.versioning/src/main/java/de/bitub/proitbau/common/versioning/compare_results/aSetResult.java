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

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public abstract class aSetResult<T extends iResultable<?>, O> implements iResultable<O> {
	
	private String name = "";
	
	private O first = null;
	
	private O second = null;
	
	private int orderIndex = 0;
	
	private boolean visible = true;
	
	private boolean equal = true;
	
	protected List<T> results = Lists.newArrayListWithExpectedSize(200);
	
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
	public O getFirst() {
		return this.first;
	}
	
	@Override
	public void setFirst(final O first) {
		Preconditions.checkNotNull(first, "Given object is null");
		this.first = first;
	}
	
	public boolean isFirstNull() {
		if (this.first == null) {
			return true;
		}
		return false;
	}
	
	@Override
	public O getSecond() {
		return this.second;
	}
	
	@Override
	public void setSecond(final O second) {
		Preconditions.checkNotNull(second, "Given object is null");
		this.second = second;
	}
	
	public boolean isSecondNull() {
		if (this.second == null) {
			return true;
		}
		return false;
	}
	
	public void addResult(final T result) {
		Preconditions.checkNotNull(result, "Given result is null");
		this.results.add(result);
		// Propagate this to higher level
		if (!result.isEqual()) {
			this.setEqual(false);
		}
	}
	
	public T getResult(final Object object) {
		for (T result : this.results) {
			if (result.getFirst().equals(object)) {
				return result;
			}
		}
		return null;
	}
	
	public List<T> getResults() {
		return this.results;
	}
	
	public void setResults(final List<T> results) {
		Preconditions.checkNotNull(results, "Given results is null");
		this.results = results;
	}
	
	public ModificationType getModificationType() {
		return this.modificationType;
	}
	
	public void setModificationType(final ModificationType modificationType) {
		this.modificationType = modificationType;
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
	
	public boolean contains(final Object field) {
		Preconditions.checkNotNull(field, "Given field is null!");
		if (field.equals(this.first) || field.equals(this.second)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isEqual() {
		return this.equal;
	}
	
	@Override
	public void setEqual(final boolean equal) {
		this.equal = equal;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.addValue(this.first)
			.addValue(this.second)
			.addValue(this.equal)
			.toString();
		// @formatter:on
	}
	
}
