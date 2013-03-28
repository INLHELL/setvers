/*
 * Package: de.bitub.proitbau.common.versioning.couchdb.binding.graph
 * Project: SetVers
 * File: VersionedSetWrapper.java
 * Date: 27.08.2012
 * Time: 15:28:41
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

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public class VersionedSetWrapper {
	
	private String uuid = UUID.randomUUID().toString();
	
	private String revision;
	
	private Set<VersionedSet> versionedsSets = Sets.newHashSet();
	
	public VersionedSetWrapper() {
		super();
	}
	
	public String getUuid() {
		return this.uuid;
	}
	
	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}
	
	public String getRevision() {
		return this.revision;
	}
	
	public void setRevision(final String revision) {
		this.revision = revision;
	}
	
	public Set<VersionedSet> getVersionedSets() {
		return this.versionedsSets;
	}
	
	public void setVersionedsSets(final Set<VersionedSet> versionedsSets) {
		this.versionedsSets = versionedsSets;
	}
	
	public void addVersionedsSet(final VersionedSet versionedSet) {
		this.versionedsSets.add(versionedSet);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getUuid());
		// Objects.hashCode(this.getUuid(), field1, field2);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof VersionedSetWrapper) {
			final VersionedSetWrapper other = (VersionedSetWrapper) obj;
			return Objects.equal(this.uuid, other.uuid);
			// && Objects.equal(this.filed, other.filed);
		}
		return false;
	}
	
	@Override
	public String toString() {
		// @formatter:off
		return Objects.toStringHelper(this)
			.add("uuid", this.uuid)
			.toString();
		// @formatter:on
	}
	
}
