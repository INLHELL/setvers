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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Preconditions;

import de.bitub.proitbau.common.versioning.model.VersionedSet;

public class BindingChecker {
	
	private BindingChecker() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static BindingChecker instance = new BindingChecker();
	}
	
	@SuppressWarnings("synthetic-access")
	public static BindingChecker getInstance() {
		return Handler.instance;
	}
	
	// @formatter:off
	 //  ___________________________      Binding       _________________________________________
	 // |                           |       \/         |                                         |
	 // | VersionedSet_Of_Locations |----------------->|      VersionedSet_Of_Components         |
	 // |                           |                  |                                         |
	 // |      Location_1<----------|------------------|---------Component_1(Location 1)         |
	 // |                           |       /\         |                                         |
	 // |___________________________|   Reference      |_________________________________________|
	 //
	 // Component_1 refers to the Location_1
	 // Versioned set of Locations binds Versioned set of Components
	 // Versioned set of Components is bound by Versioned set of Locations
	 //
	 // @formatter:on
	
	/*
	 * Checks, if the first versioned set is bound by the second one
	 * @param boundVersionedSet
	 * @param bindVersionedSet
	 */
	public boolean isBoundBy(final VersionedSet boundVersionedSet, final VersionedSet bindVersionedSet) {
		Preconditions.checkNotNull(boundVersionedSet, "Bound versioned set is null!");
		Preconditions.checkArgument(!boundVersionedSet.getVersionedObjects().isEmpty(),
			"Bound versioned set  has no versioned objects!");
		Preconditions.checkNotNull(boundVersionedSet, "Bind versioned set is null!");
		Preconditions.checkArgument(!bindVersionedSet.getVersionedObjects().isEmpty(),
			"Bind versioned set has no versioned objects!");
		Preconditions.checkArgument(!boundVersionedSet.getType().equals(bindVersionedSet.getType()),
			"The class type of the Bound versioned set  is coincides with class type of the bind versioned set!");
		
		boolean isBound = false;
		// 1) First case, the versioned sets can be bound by a property of a
		// @VersionedEntity annotation 'boundBy', the problem is, that this property
		// and annotation itself can be placed somewhere in the superclass.
		// First we need to obtain the list of classes which are mentioned in any
		// class or a superclass, of versioned objects of the boundByVersionedSet,
		// at
		// the boundBy property.
		
		final Class<?> boundVersionedSetClassType = boundVersionedSet.getType().getMainType();
		final Class<?> bindVersionedSetClassType = bindVersionedSet.getType().getMainType();
		isBound = ReflectionUtil.getInstance().isBoundByClass(boundVersionedSetClassType, bindVersionedSetClassType);
		
		// @formatter:off
		// 2) Second case, standard procedure, we are going to start analyze the
		// content of the bound class. 
		// The bound versioned set can be bound by the bind versioned set, if 
		//		1) The field of the object inside the bound versioned set refers to the object of the bind versioned set
		//		2) The element of the collection of the object inside the bound versioned set refers to the object of the bind versioned set
		//		3) The value of the map of the object inside the bound versioned set refers to the object of the bind versioned set
		//		4) The element of the array of the object inside the bound versioned set refers to the object of the bind versioned set
		// We have four different type of relations:
		// 1) object.field 						  ---> object
		// 2) object.collection.element ---> object
		// 3) object.map.value 				  ---> object
		// 4) object.array.element 		  ---> object
		// @formatter:on
		if (!isBound) {
			// Go through all objects in the bound versioned set
			final Iterator<Object> versionedObjectsIterator = boundVersionedSet.getVersionedObjects().iterator();
			while (versionedObjectsIterator.hasNext() && !isBound) {
				final Object versionedObject = versionedObjectsIterator.next();
				
				final Collection<Object> versionedValues =
					ReflectionUtil.getInstance().getVersionedValuesOfVersionedObject(versionedObject);
				
				// Go through all values of the specific object
				final Iterator<Object> versionedValuesIterator = versionedValues.iterator();
				while (versionedValuesIterator.hasNext() && !isBound) {
					final Object versionedValue = versionedValuesIterator.next();
					
					// Case 1) object.field ---> object
					final boolean isGivenVersionedSetContainsObject =
						bindVersionedSet.getVersionedObjects().contains(versionedValue);
					if (isGivenVersionedSetContainsObject) {
						isBound = true;
					}
					
					// Case 2) object.collection.element ---> object
					else if (versionedValue instanceof Collection<?>) {
						final boolean doColelctionsHaveCommonElements =
							!Collections.disjoint((Collection<?>) versionedValue, boundVersionedSet.getVersionedObjects());
						if (doColelctionsHaveCommonElements) {
							isBound = true;
						}
					}
					
					// Case 3) object.map.value ---> object
					else if (versionedValue instanceof Map) {
						final boolean doColelctionsHaveCommonElements =
							!Collections.disjoint(((Map<?, ?>) versionedValue).values(), boundVersionedSet.getVersionedObjects());
						if (doColelctionsHaveCommonElements) {
							isBound = true;
						}
					}
					
					// Case 4) object.array.element ---> object
					else if (versionedValue instanceof Object[]) {
						final boolean doColelctionsHaveCommonElements =
							!Collections
								.disjoint(Arrays.asList(((Object[]) versionedValue)), boundVersionedSet.getVersionedObjects());
						if (doColelctionsHaveCommonElements) {
							isBound = true;
						}
					}
				} // Loop through the fields of the current VersionedSet
			} // Loop through objects in a dependent VersionedSet
		}
		return isBound;
	}
	
}
