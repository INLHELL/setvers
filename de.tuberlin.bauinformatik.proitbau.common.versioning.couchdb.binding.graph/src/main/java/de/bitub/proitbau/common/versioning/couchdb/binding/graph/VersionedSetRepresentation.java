/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.binding.graph
 * Project: SetVers
 * File: VersionedSetRepresentation.java
 * Date: 27.08.2012
 * Time: 15:29:03
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.couchdb.binding.graph;

import java.util.Set;
import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.model.VersionedSetType;

public class VersionedSetRepresentation implements iVersionedSetWrapper {
	
	private String uuid = UUID.randomUUID().toString();
	
	private String versionedSetUuid;
	
	private String versionedSetName;
	
	private int versionedSetSize;
	
	private boolean visible;
	
	private VersionedSetType versionedSetType;
	
	private Set<String> predecessorsBinding = Sets.newHashSetWithExpectedSize(30);
	
	private Set<String> predecessorsVersioning = Sets.newHashSetWithExpectedSize(30);
	
	public VersionedSetRepresentation() {
		super();
	}
	
	public VersionedSetRepresentation(final String versionedSetUuid, final String versionedSetName,
		final int versionedSetSize, final boolean visible, final VersionedSetType versionedSetType) {
		super();
		this.versionedSetUuid = versionedSetUuid;
		this.versionedSetName = versionedSetName;
		this.versionedSetSize = versionedSetSize;
		this.visible = visible;
		this.versionedSetType = versionedSetType;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof VersionedSetRepresentation) {
			final VersionedSetRepresentation other = (VersionedSetRepresentation) obj;
			return Objects.equal(this.uuid, other.getUuid());
		}
		return false;
	}
	
	public Set<String> getPredecessorsBinding() {
		return this.predecessorsBinding;
	}
	
	public Set<String> getPredecessorsVersioning() {
		return this.predecessorsVersioning;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}
	
	@Override
	public VersionedSetType getVersionedSetType() {
		return this.versionedSetType;
	}
	
	@Override
	public String getVersionedSetName() {
		return this.versionedSetName;
	}
	
	@Override
	public int getVersionedSetSize() {
		return this.versionedSetSize;
	}
	
	@Override
	public String getVersionedSetUuid() {
		return this.versionedSetUuid;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.uuid);
		// Objects.hashCode(this.getUuid(), field1, field2);
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public void setVersionedSetType(final VersionedSetType versionedSetType) {
		this.versionedSetType = versionedSetType;
	}
	
	@Override
	public void setVersionedSetName(final String versionedSetName) {
		this.versionedSetName = versionedSetName;
	}
	
	@Override
	public void setVersionedSetSize(final int versionedSetSize) {
		this.versionedSetSize = versionedSetSize;
	}
	
	@Override
	public void setVersionedSetUuid(final String versionedSetUuid) {
		this.versionedSetUuid = versionedSetUuid;
	}
	
	public void setVisible(final boolean visible) {
		this.visible = visible;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.add("uuid", this.uuid)
			.toString();
		// @formatter:on
	}
	
	protected void setPredecessorsBinding(final Set<String> predecessorsBinding) {
		this.predecessorsBinding = predecessorsBinding;
	}
	
	protected void setPredecessorsVersioning(final Set<String> predecessorsVersioning) {
		this.predecessorsVersioning = predecessorsVersioning;
	}
	
}
