/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 2013
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class VersionedSetRepresentationUtil {
	
	private VersionedSetRepresentationUtil() {
	}
	
	private static class Handler {
		
		private static VersionedSetRepresentationUtil instance = new VersionedSetRepresentationUtil();
	}
	
	public static VersionedSetRepresentationUtil getInstance() {
		return Handler.instance;
	}
	
	/*
	 * This method checks - is there a path from the
	 * sourceVersionedSetRepresentation to the targetVersionedSetRepresentation or
	 * in other words is targetVersionedSetRepresentation reachable via binding
	 * relations.
	 * -------------------------------------------------------------------------
	 * Lets consider this versioned set representation of the binding graph:
	 * Shift -> DailyShifts -> WeeklyShifts
	 * -> - means binding relations which is an opposite reference direction.
	 * Then lets say, DailyShifts is invisible and we want to know is there a
	 * valid path between Shift and WeeklyShifts.
	 * We store predecessorBinding in each versioned set object, so our graph
	 * will look like this: Shift <- DailyShifts <- WeeklyShifts, this structure
	 * we have internally in memory.
	 * Based on this, we can start from targetVersionedSetRepresentation and move
	 * forward through the first level predessecors, second, etc. If we reach
	 * during this procedure the sourceVersionedRepresentation set we can return
	 * true, because this means that targetVersionedSetRepresentation is reachable
	 * from the sourceVersionedSetRepresentation.
	 */
	public boolean isReachableViaBindingRelations(final VersionedSetRepresentation sourceVersionedSetRepresentation,
		final VersionedSetRepresentation targetVersionedSetRepresentation,
		final Collection<VersionedSetRepresentation> versionedSetRepresentations) {
		Preconditions.checkNotNull(sourceVersionedSetRepresentation, "Source versioned set representation is null!");
		Preconditions.checkNotNull(targetVersionedSetRepresentation, "Target versioned set representation is null!");
		boolean isReachable = false;
		final List<VersionedSetRepresentation> visitedVersionedSetRepresentations = Lists.newArrayList();
		final Stack<VersionedSetRepresentation> stack = new Stack<VersionedSetRepresentation>();
		stack.add(targetVersionedSetRepresentation);
		while (!stack.isEmpty() && !isReachable) {
			final VersionedSetRepresentation popedVersionedSetRepresentation = stack.pop();
			// Mark popedVersionedSetRepresentation as visited
			if (!visitedVersionedSetRepresentations.contains(popedVersionedSetRepresentation)) {
				visitedVersionedSetRepresentations.add(popedVersionedSetRepresentation);
				// Now we get the uuids of all predecessor versioned sets
				for (final String predecessorVersionedSetUuid : popedVersionedSetRepresentation.getPredecessorsBinding()) {
					// But we need the versioned set object, so we have to
					// iterate through the versionedSetRepresentations to find out
					// which versioned set representation object contains given uuid of
					// the versioned set predecessor
					final boolean isPredecessorVersionedSetRepresentationFound = false;
					final Iterator<VersionedSetRepresentation> versionedSetRepresentationIterator =
						versionedSetRepresentations.iterator();
					while (!isPredecessorVersionedSetRepresentationFound && versionedSetRepresentationIterator.hasNext()) {
						final VersionedSetRepresentation predecessorVersionedSetRepresentation =
							versionedSetRepresentationIterator.next();
						if (predecessorVersionedSetRepresentation.getVersionedSetUuid().equals(predecessorVersionedSetUuid)) {
							// First check if this versioned set representation was visited
							// before or not
							if (!visitedVersionedSetRepresentations.contains(predecessorVersionedSetRepresentation)) {
								// If this versioned set representation equals to
								// sourceVersionedSetRepresentation, we can say that the source
								// can be reached from target
								if (predecessorVersionedSetRepresentation.equals(sourceVersionedSetRepresentation)) {
									isReachable = true;
								}
								// If the predecessorVersionedSetRepresentation is visible we
								// must leave it to avoid this situation:
								// @formatter:off
								// visible_1-->visible_2-->visible_3
								// visible_1 - source
								// visible_3 - target
								// visible_1-->visible_2-->visible_3
								//		 \										/\
								//      \___________________/
								// @formatter:on
								else if (!predecessorVersionedSetRepresentation.isVisible()) {
									stack.add(predecessorVersionedSetRepresentation);
								}
							}
						}
						
					}
				}
			}
		}
		
		return isReachable;
	}
	
}
