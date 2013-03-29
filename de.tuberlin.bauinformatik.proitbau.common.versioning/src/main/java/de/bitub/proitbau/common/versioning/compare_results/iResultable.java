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
package de.bitub.proitbau.common.versioning.compare_results;

public interface iResultable<T extends Object> {
	
	T getFirst();
	
	void setFirst(T first);
	
	T getSecond();
	
	void setSecond(T second);
	
	String getName();
	
	void setName(final String name);
	
	boolean isEqual();
	
	void setEqual(final boolean equal);
	
}
