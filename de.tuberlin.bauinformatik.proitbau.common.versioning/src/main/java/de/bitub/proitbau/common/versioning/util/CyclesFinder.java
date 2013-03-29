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
package de.bitub.proitbau.common.versioning.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public class CyclesFinder {
	
	private final Set<List<VersionedSet>> cycles = new HashSet<List<VersionedSet>>();
	
	private final Set<VersionedSet> grayNodes = new HashSet<VersionedSet>();
	
	private final List<VersionedSet> path = new ArrayList<VersionedSet>();
	
	private CyclesFinder() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static CyclesFinder instance = new CyclesFinder();
	}
	
	@SuppressWarnings("synthetic-access")
	public static CyclesFinder getInstance() {
		return Handler.instance;
	}
	
	public Set<List<VersionedSet>> findElementaryCycles(final Set<VersionedSet> VersionedSets) {
		if ((VersionedSets != null) && (VersionedSets.size() != 0)) {
			for (final VersionedSet arbitaryVersionedSet : VersionedSets) {
				this.dfs(arbitaryVersionedSet);
			}
		}
		return this.cycles;
	}
	
	private void dfs(final VersionedSet versionedSet) {
		this.grayNodes.add(versionedSet);
		this.path.add(versionedSet);
		for (final VersionedSet predecessorVersionedSet : versionedSet.getPredecessorsBinding()) {
			if (!this.grayNodes.contains(predecessorVersionedSet)) {
				this.dfs(predecessorVersionedSet);
			}
			else {
				final int index = this.path.indexOf(predecessorVersionedSet);
				final List<VersionedSet> cycle = new ArrayList<VersionedSet>();
				for (int i = index; i < this.path.size(); i++) {
					cycle.add(this.path.get(i));
				}
				cycle.add(predecessorVersionedSet);
				
				this.cycles.add(cycle);
			}
		}
		this.path.remove(versionedSet);
		this.grayNodes.remove(versionedSet);
	}
	
}
