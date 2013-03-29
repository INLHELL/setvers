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

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public class VersionedSetUtil {
	
	private VersionedSetUtil() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static VersionedSetUtil instance = new VersionedSetUtil();
	}
	
	@SuppressWarnings("synthetic-access")
	public static VersionedSetUtil getInstance() {
		return Handler.instance;
	}
	
	public List<VersionedSet> getBound(final Collection<VersionedSet> versionedSets, final boolean includingGiven) {
		Preconditions.checkNotNull(versionedSets, "Collection of versioned sets is null");
		Preconditions.checkArgument(versionedSets.size() != 0, "Collection of versioned sets is empty");
		final List<VersionedSet> allVersionedSets = Lists.newArrayList();
		final Stack<VersionedSet> stack = new Stack<VersionedSet>();
		for (final VersionedSet versionedSet : versionedSets) {
			if (!allVersionedSets.contains(versionedSet)) {
				stack.add(versionedSet);
				while (!stack.isEmpty()) {
					final VersionedSet tempVersionedSet = stack.pop();
					if (!allVersionedSets.contains(tempVersionedSet)
							&& (includingGiven || !tempVersionedSet.equals(versionedSet))) {
						allVersionedSets.add(tempVersionedSet);
					}
					for (final VersionedSet predecessorVersionedSet : tempVersionedSet.getPredecessorsBinding()) {
						if (!allVersionedSets.contains(predecessorVersionedSet)) {
							stack.add(predecessorVersionedSet);
						}
					}
				}
			}
		}
		return allVersionedSets;
	}
	
	public List<VersionedSet> getBound(final VersionedSet versionedSet, final boolean includingGiven) {
		Preconditions.checkNotNull(versionedSet, "Versioned set is null");
		final List<VersionedSet> allVersionedSets = Lists.newArrayList();
		final Stack<VersionedSet> stack = new Stack<VersionedSet>();
		stack.add(versionedSet);
		while (!stack.isEmpty()) {
			final VersionedSet tempVersionedSet = stack.pop();
			if (!allVersionedSets.contains(tempVersionedSet) && (includingGiven || !tempVersionedSet.equals(versionedSet))) {
				allVersionedSets.add(tempVersionedSet);
			}
			for (final VersionedSet predecessorVersionedSet : tempVersionedSet.getPredecessorsBinding()) {
				if (!allVersionedSets.contains(predecessorVersionedSet)) {
					stack.add(predecessorVersionedSet);
				}
			}
		}
		return allVersionedSets;
	}
	
	/*
	 * This method checks - is there a path from the sourceVersionedSet to the
	 * targetVersionedSet or in other words is targetVersionedSet reachable via
	 * binding relations.
	 * -------------------------------------------------------------------------
	 * Lets consider this versioned set binding graph:
	 * Shift -> DailyShifts -> WeeklyShifts
	 * -> - means binding relations which is an opposite reference direction.
	 * Then lets say, DailyShifts is invisible and we want to know is there a
	 * valid path between Shift and WeeklyShifts.
	 * We store predecessorBinding in each versioned set object, so our graph
	 * will look like this: Shift <- DailyShifts <- WeeklyShifts, this structure
	 * we have internally in memory. Based on this, we can start from
	 * targetVersionedSet and move forward through the first level predessecors,
	 * second, etc. If we reach during this procedure sourceVersioned set we can
	 * return true, because this means that targetVersionedSet is reachable from
	 * the sourceVersionedSet.
	 */
	public boolean isReachableViaBindingRelations(final VersionedSet sourceVersionedSet,
		final VersionedSet targetVersionedSet) {
		Preconditions.checkNotNull(sourceVersionedSet, "Source versioned set is null!");
		Preconditions.checkNotNull(targetVersionedSet, "Target versioned set is null!");
		boolean isReachable = false;
		final List<VersionedSet> visitedVersionedSets = Lists.newArrayList();
		final Stack<VersionedSet> stack = new Stack<VersionedSet>();
		stack.add(targetVersionedSet);
		while (!stack.isEmpty() && !isReachable) {
			final VersionedSet popedVersionedSet = stack.pop();
			if (!visitedVersionedSets.contains(popedVersionedSet)) {
				visitedVersionedSets.add(popedVersionedSet);
				for (final VersionedSet predecessorVersionedSet : popedVersionedSet.getPredecessorsBinding()) {
					if (!visitedVersionedSets.contains(predecessorVersionedSet)) {
						if (popedVersionedSet.equals(sourceVersionedSet)) {
							isReachable = true;
						}
						else {
							stack.add(predecessorVersionedSet);
						}
					}
				}
			}
		}
		
		return isReachable;
	}
	
}
