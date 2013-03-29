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
package de.bitub.proitbau.common.versioning.model.logic;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Preconditions;

import de.bitub.proitbau.common.versioning.annotations.DomainModel;
import de.bitub.proitbau.common.versioning.model.Versionable;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.util.ReflectionUtil;

public class ReverseConverter {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(ReverseConverter.class);
	{
		ReverseConverter.logger.setLevel(Level.INFO);
	}
	
	private ReverseConverter() {
	}
	
	private static class Handler {
		
		@SuppressWarnings("synthetic-access")
		private static ReverseConverter instance = new ReverseConverter();
	}
	
	@SuppressWarnings("synthetic-access")
	public static ReverseConverter getInstance() {
		return Handler.instance;
	}
	
	/*
	 * Returns a domain model object with initialized fields,
	 * the fields will be initialized with the values which
	 * come from the versioned sets
	 * @param domainModel a domain model object, the class of this object must
	 * contain @DomainModel annotation
	 * @param versionedSets the set of versioned sets
	 * @return domain model with initialized fields which were initialized with
	 * the objects from the given versioned sets
	 */
	@SuppressWarnings("unchecked")
	public void revert(final Object domainModel, final Set<VersionedSet> versionedSets) {
		Preconditions.checkNotNull(domainModel, "Given object is null!");
		Preconditions.checkArgument(domainModel.getClass().isAnnotationPresent(DomainModel.class),
			"@DomainModel annotation isn't presented in the class of the given object!");
		Preconditions.checkNotNull(versionedSets, "The set of versioned sets object is null!");
		Preconditions.checkArgument(!versionedSets.isEmpty(), "The set of versioned sets object is empty!");
		
		// Algorithm:
		// 1) Find a specific versioned set which contains information about a
		// specific field of the domain model
		// 2) Obtain objects from the versioned set based on their uuids
		// 3) Specify the obtained objects as value of the specific field of the
		// domain model
		
		// 1)
		final Collection<Field> domainModelFields = ReflectionUtil.getInstance().getFields(domainModel);
		for (final VersionedSet versionedSet : versionedSets) {
			if (!versionedSet.getFieldUuidsPairs().isEmpty()) {
				for (final Field domainModelField : domainModelFields) {
					if (versionedSet.getFieldUuidsPairs().keySet().contains(domainModelField.toGenericString())) {
						// 2)
						final Set<String> uuids = versionedSet.getFieldUuidsPairs().get(domainModelField.toGenericString());
						for (final String uuid : uuids) {
							for (final Object versionedObject : versionedSet.getVersionedObjects()) {
								if (((Versionable) versionedObject).getUuid().equals(uuid)) {
									// 3)
									try {
										Object valueOfDomainModelField = domainModelField.get(domainModel);
										
										if (valueOfDomainModelField == null) {
											// Initialize a field if it is null
											final Object newValueOfDomainModelField = domainModelField.getType().newInstance();
											valueOfDomainModelField = newValueOfDomainModelField;
										}
										
										// Required conditions
										final boolean isCollection = valueOfDomainModelField instanceof Collection;
										// @formatter:off
									// TODO for Maps and Arrays ReverseConvertor doesn't work
									// final boolean isMap = valueOfDomainModelField instanceof Map;
									// final boolean isArray = domainModelField.getType().isArray();
									// @formatter:on
										
										if (isCollection) {
											((Collection<Object>) valueOfDomainModelField).add(versionedObject);
										}
										else {
											domainModelField.set(domainModel, versionedObject);
										}
										
									}
									catch (final IllegalArgumentException e) {
										ReverseConverter.logger.error(e.getMessage());
									}
									catch (final IllegalAccessException e) {
										ReverseConverter.logger.error(e.getMessage());
									}
									catch (final InstantiationException e) {
										ReverseConverter.logger.error(e.getMessage());
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	
}
