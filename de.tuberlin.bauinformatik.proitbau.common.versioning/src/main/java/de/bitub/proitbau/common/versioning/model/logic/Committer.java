/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 04.08.2010
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
package de.bitub.proitbau.common.versioning.model.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.bitub.proitbau.common.versioning.compare_results.ModificationType;
import de.bitub.proitbau.common.versioning.compare_results.StateResult;
import de.bitub.proitbau.common.versioning.compare_results.VersionedSetResult;
import de.bitub.proitbau.common.versioning.model.ModelCache;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.model.VersionedSetType;
import de.bitub.proitbau.common.versioning.util.StringUtil;

public class Committer {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(Committer.class);
	{
		Committer.logger.setLevel(Level.INFO);
	}
	
	/*
	 * -> - reference / predecessor binding relation
	 * ~> - binding
	 * * - modification
	 * 1<-2<-3->4
	 * 1~>2~>3<~4
	 * 1<-2*<-3->4->5
	 * 1~>2*~>3<~4<~5
	 * modified - 2*
	 * newly created - 5
	 * bound - 1 (by 2)
	 * unmodified - 1,3,4
	 * irrelevant - 1,2,5
	 * committed - 1,2,5
	 */
	
	private final Set<VersionedSet> boundVersionedSetsInNewState = Sets.newHashSetWithExpectedSize(30);
	private final Map<VersionedSetType, VersionedSet> remainedVersionedSetsInOldState = Maps
		.newHashMapWithExpectedSize(30);
	private final Set<VersionedSet> committedVersionedSets = Sets.newHashSetWithExpectedSize(30);
	private final Set<VersionedSet> modifiedVersionedSets = Sets.newHashSetWithExpectedSize(30);
	private final Set<VersionedSet> createdVersionedSets = Sets.newHashSetWithExpectedSize(30);
	private final Set<VersionedSet> deletedVersionedSets = Sets.newHashSetWithExpectedSize(30);
	private final Set<VersionedSet> invariableVersionedSets = Sets.newHashSetWithExpectedSize(30);
	private final Set<VersionedSet> actualState = Sets.newHashSetWithExpectedSize(30);
	
	private Committer() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static Committer instance = new Committer();
	}
	
	@SuppressWarnings("synthetic-access")
	public static Committer getInstance() {
		return Handler.instance;
	}
	
	public Set<VersionedSet> findCommittedVersionedSets(final StateResult stateResult) {
		Preconditions.checkNotNull(stateResult, "Given statetResult is null!");
		
		// Clear previous results
		this.boundVersionedSetsInNewState.clear();
		this.remainedVersionedSetsInOldState.clear();
		this.committedVersionedSets.clear();
		this.modifiedVersionedSets.clear();
		this.createdVersionedSets.clear();
		this.deletedVersionedSets.clear();
		this.invariableVersionedSets.clear();
		this.actualState.clear();
		
		// Invalidate cache
		ModelCache.getInstance().invalidateObjectValuesCache();
		
		if ((stateResult != null) && !stateResult.isEqual()) {
			final Map<VersionedSetType, VersionedSet> oldVersionedSets = Maps.newHashMapWithExpectedSize(30);
			final Map<VersionedSetType, VersionedSet> newVersionedSets = Maps.newHashMapWithExpectedSize(30);
			final Set<VersionedSet> notCreatedAndModifiedVersionedSets = Sets.newHashSetWithExpectedSize(30);
			ModificationType modificationType = ModificationType.CREATED;
			for (final VersionedSetResult versionedSetResult : stateResult.getResults()) {
				if (versionedSetResult.getFirst() != null) {
					oldVersionedSets.put(versionedSetResult.getFirst().getType(), versionedSetResult.getFirst());
				}
				if (versionedSetResult.getSecond() != null) {
					newVersionedSets.put(versionedSetResult.getSecond().getType(), versionedSetResult.getSecond());
				}
				modificationType = versionedSetResult.getModificationType();
				switch (modificationType) {
					case CREATED: {
						this.createdVersionedSets.add(versionedSetResult.getSecond());
					}
						break;
					case DELETED: {
						this.deletedVersionedSets.add(versionedSetResult.getFirst());
					}
						break;
					case INVARIABLE: {
						this.invariableVersionedSets.add(versionedSetResult.getFirst());
						notCreatedAndModifiedVersionedSets.add(versionedSetResult.getSecond());
					}
						break;
					case MODIFIED: {
						this.modifiedVersionedSets.add(versionedSetResult.getSecond());
					}
						break;
					default:
						break;
				}
			}
			
			// Find bound versioned sets
			if (!notCreatedAndModifiedVersionedSets.isEmpty()) {
				final Stack<VersionedSet> stackOfVersionedSetsWhichHaveToBeChecked = new Stack<VersionedSet>();
				for (final VersionedSet versionedSet : notCreatedAndModifiedVersionedSets) {
					if ((Sets.intersection(versionedSet.getPredecessorsBinding(), this.modifiedVersionedSets).size() > 0)
							|| (Sets.intersection(versionedSet.getPredecessorsBinding(), this.createdVersionedSets).size() > 0)) {
						this.boundVersionedSetsInNewState.add(versionedSet);
						stackOfVersionedSetsWhichHaveToBeChecked.push(versionedSet);
					}
				}
				
				while (!stackOfVersionedSetsWhichHaveToBeChecked.isEmpty()) {
					final VersionedSet versionedSetWhichHasToBeChecked = stackOfVersionedSetsWhichHaveToBeChecked.pop();
					for (final VersionedSet uncheckedVersionedSet : notCreatedAndModifiedVersionedSets) {
						if (uncheckedVersionedSet.getPredecessorsBinding().contains(versionedSetWhichHasToBeChecked)) {
							stackOfVersionedSetsWhichHaveToBeChecked.push(uncheckedVersionedSet);
							this.boundVersionedSetsInNewState.add(uncheckedVersionedSet);
						}
					}
				}
			}
			
			if (!this.boundVersionedSetsInNewState.isEmpty()) {
				this.committedVersionedSets.addAll(this.boundVersionedSetsInNewState);
			}
			if (!this.modifiedVersionedSets.isEmpty()) {
				this.committedVersionedSets.addAll(this.modifiedVersionedSets);
			}
			if (!this.createdVersionedSets.isEmpty()) {
				this.committedVersionedSets.addAll(this.createdVersionedSets);
			}
			if (!this.committedVersionedSets.isEmpty()) {
				this.actualState.addAll(this.committedVersionedSets);
			}
			
			if (!this.invariableVersionedSets.isEmpty()) {
				// Remove from invariable sets which were part of the bound versioned
				// sets
				
				for (final VersionedSet invariableVersionedSet : this.invariableVersionedSets) {
					this.remainedVersionedSetsInOldState.put(invariableVersionedSet.getType(), invariableVersionedSet);
					final VersionedSet invalidVersionedSetInNewState = newVersionedSets.get(invariableVersionedSet.getType());
					for (final VersionedSet committedVersionedSet : this.committedVersionedSets) {
						this.remainedVersionedSetsInOldState.remove(committedVersionedSet.getType());
						if (!this.boundVersionedSetsInNewState.contains(invalidVersionedSetInNewState)) {
							if (committedVersionedSet.getPredecessorsBinding().contains(invalidVersionedSetInNewState)) {
								committedVersionedSet.removePredecessorBinding(invalidVersionedSetInNewState);
								committedVersionedSet.addPredecessorBinding(invariableVersionedSet);
							}
						}
						if (oldVersionedSets.containsKey(committedVersionedSet.getType())) {
							committedVersionedSet.removePredecessorVersioning(committedVersionedSet.getUuid());
							committedVersionedSet.setName(StringUtil.getInstance().getNameWithNewVersion(
								oldVersionedSets.get(committedVersionedSet.getType()).getName()));
							try {
								committedVersionedSet.addPredecessorVersioning(oldVersionedSets.get(committedVersionedSet.getType()));
							}
							catch (final Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				this.actualState.addAll(this.remainedVersionedSetsInOldState.values());
				
			}
			
			return this.committedVersionedSets;
		}
		return null;
	}
	
	public Set<VersionedSet> getBoundVersionedSetsInNewState() {
		return this.boundVersionedSetsInNewState;
	}
	
	public Collection<VersionedSet> getRemainedVersionedSetsInOldState() {
		return this.remainedVersionedSetsInOldState.values();
	}
	
	public Set<VersionedSet> getCommittedVersionedSets() {
		return this.committedVersionedSets;
	}
	
	// public Set<VersionedSet> getIrrelevantVersionedSets() {
	// return this.irrelevantVersionedSets;
	// }
	
	public Set<VersionedSet> getModifiedVersionedSets() {
		return this.modifiedVersionedSets;
	}
	
	public Set<VersionedSet> getCreatedVersionedSets() {
		return this.createdVersionedSets;
	}
	
	public Set<VersionedSet> getDeletedVersionedSets() {
		return this.deletedVersionedSets;
	}
	
	public Set<VersionedSet> getInvariableVersionedSets() {
		return this.invariableVersionedSets;
	}
	
	public Set<VersionedSet> getActualState() {
		return this.actualState;
	}
	
	// public Set<VersionedSet> getIsolatedVersionedSetsInOldState() {
	// return this.isolatedVersionedSetsInOldState;
	// }
}
