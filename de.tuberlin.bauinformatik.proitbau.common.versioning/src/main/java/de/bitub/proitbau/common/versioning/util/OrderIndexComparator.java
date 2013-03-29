/*******************************************************************************
 * Author: "Vladislav Fedotov", "Tilman Reinhardt"
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
package de.bitub.proitbau.common.versioning.util;

import java.io.Serializable;
import java.util.Comparator;

import de.bitub.proitbau.common.versioning.compare_results.FieldResult;

public class OrderIndexComparator implements Comparator<FieldResult>, Serializable {
	
	private static final long serialVersionUID = -437588856495453671L;
	
	@Override
	public int compare(final FieldResult firstFieldResult, final FieldResult secondFieldResult) {
		if (firstFieldResult.getOrderIndex() == secondFieldResult.getOrderIndex()) {
			return 0;
		}
		if (firstFieldResult.getOrderIndex() < secondFieldResult.getOrderIndex()) {
			return -1;
		}
		return 1;
	}
	
}
