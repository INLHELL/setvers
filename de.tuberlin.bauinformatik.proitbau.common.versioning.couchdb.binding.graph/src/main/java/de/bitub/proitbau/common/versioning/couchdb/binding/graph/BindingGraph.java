/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 24.07.2012
 * Project: Setvers
 * E-mail: vladislav.fedotov@tu-berlin.de
 * Company: TU Berlin
 * Version: 1.0
 * 
 * Copyright (c) 2013 Vladislav Fedotov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Vladislav Fedotov - initial API and implementation
 ******************************************************************************/
package de.bitub.proitbau.common.versioning.couchdb.binding.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public class BindingGraph implements iBindingGraph {
	
	private String author = "author";
	
	private String comment = "comment";
	
	private long date = System.currentTimeMillis();
	
	private String revision;
	
	private String uuid = UUID.randomUUID().toString();
	
	private Set<VersionedSetRepresentation> versionedSetRepresentations = new HashSet<VersionedSetRepresentation>(30);
	
	private VersionedSetWrapper versionedSetWrapper;
	
	private String versionedSetWrapperUuid;
	
	public BindingGraph() {
	}
	
	protected void addVersionedSet(final VersionedSet versionedSet) {
		Preconditions.checkNotNull(versionedSet, "Given versioned set is null!");
		this.versionedSetWrapper.addVersionedsSet(versionedSet);
		// @formatter:off
		final VersionedSetRepresentation versionedSetRepresentation =
			new VersionedSetRepresentation(
				versionedSet.getUuid(), 
				versionedSet.getName(), 
				versionedSet.getUuidsOfObjects().size(), 
				versionedSet.isVisible(),
				versionedSet.getType()
		);
		// @formatter:on
		final Set<String> predecessorsBinding = Sets.newHashSetWithExpectedSize(30);
		for (final VersionedSet predecessorBinding : versionedSet.getPredecessorsBinding()) {
			predecessorsBinding.add(predecessorBinding.getUuid());
		}
		versionedSetRepresentation.setPredecessorsBinding(predecessorsBinding);
		versionedSetRepresentation.setPredecessorsVersioning(versionedSet.getPredecessorsVersioning());
		this.versionedSetRepresentations.add(versionedSetRepresentation);
	}
	
	@Override
	public void specifyNewState(final Set<VersionedSet> versionedSets) {
		Preconditions.checkNotNull(versionedSets, "Given set of versioned set objects is null!");
		Preconditions.checkArgument(!versionedSets.isEmpty(), "Given set of versioned set objects is empty!");
		this.versionedSetRepresentations.clear();
		this.versionedSetWrapper = new VersionedSetWrapper();
		this.versionedSetWrapperUuid = this.versionedSetWrapper.getUuid();
		this.author = "author";
		this.comment = "comment";
		this.date = System.currentTimeMillis();
		for (final VersionedSet versionedSet : versionedSets) {
			this.addVersionedSet(versionedSet);
		}
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof VersionedSetWrapper) {
			final VersionedSetWrapper other = (VersionedSetWrapper) obj;
			return Objects.equal(this.uuid, other.getUuid());
		}
		return false;
	}
	
	@Override
	public String getAuthor() {
		return this.author;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	@Override
	public long getDate() {
		return this.date;
	}
	
	@Override
	public String getRevision() {
		return this.revision;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}
	
	@Override
	public Set<VersionedSetRepresentation> getVersionedSetRepresentations() {
		return this.versionedSetRepresentations;
	}
	
	@Override
	public VersionedSetWrapper getVersionedSetWrapper() {
		return this.versionedSetWrapper;
	}
	
	@Override
	public String getVersionedSetWrapperUuid() {
		return this.versionedSetWrapperUuid;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.uuid);
	}
	
	@Override
	public Set<VersionedSet> obtainVersionedSets() {
		return this.versionedSetWrapper.getVersionedSets();
	}
	
	@Override
	public void setAuthor(final String author) {
		this.author = author;
	}
	
	@Override
	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	@Override
	public void setDate(final long date) {
		this.date = date;
	}
	
	@Override
	public void setRevision(final String revision) {
		this.revision = revision;
	}
	
	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}
	
	protected void setVersionedSetRepresentations(final Set<VersionedSetRepresentation> versionedSetRepresentations) {
		this.versionedSetRepresentations = versionedSetRepresentations;
	}
	
	@Override
	public void setVersionedSetWrapper(final VersionedSetWrapper versionedSetWrapper) {
		this.versionedSetWrapper = versionedSetWrapper;
	}
	
	public void setVersionedSetWrapperUuid(final String versionedSetWrapperUuid) {
		this.versionedSetWrapperUuid = versionedSetWrapperUuid;
	}
	
	@Override
	public boolean isInitial() {
		if (this.revision == null) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.add("author", this.author)
			.add("comment", this.comment)
			.add("date", this.date)
			.add("revision", this.revision)
			.add("uuid", this.uuid)
			.add("versionedSets", this.versionedSetRepresentations)
			.toString();
		// @formatter:on
	}
	
}
