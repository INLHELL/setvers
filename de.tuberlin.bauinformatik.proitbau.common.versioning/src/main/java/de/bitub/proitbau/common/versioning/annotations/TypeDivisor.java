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
package de.bitub.proitbau.common.versioning.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * New attribute was added to TypeDivisor annotation -
 * basedOnInternalObjectField, by default it's false, if it's true, then type
 * division will be applied to the underlying object, for example, we have this:
 * object1->object2->object3, if basedOnInternalObjectField is false, object1
 * will be divided to versioned sets based on type of object2, if true, the
 * algorithm will look further to the object2 and subdivide versioned sets based
 * on type of object3.
 * Important requirement:
 * - Classes of object1 and object2 must have SET_PER_OBJECT_TYPE strategy and
 * each of them must contain field with annotation @TypeDivisor (object1 has a
 * field of object2 type, this field was annotated with @TypeDivisor annotation
 * with attribute basedOnInternalObjectField - true, object2 has a field of
 * object3 type, this field was also annotated with @TypeDivisor annotation but
 * basedOnInternalObjectField is false).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.FIELD
})
public @interface TypeDivisor {
	
	boolean basedOnInternalObjectField() default false;
}
