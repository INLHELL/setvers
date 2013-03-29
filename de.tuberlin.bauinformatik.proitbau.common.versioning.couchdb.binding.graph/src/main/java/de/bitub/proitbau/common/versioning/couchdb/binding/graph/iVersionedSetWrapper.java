/*******************************************************************************
 * Author: "Vladislav Fedotov"
 * Written: 29.08.2012
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

import de.bitub.proitbau.common.versioning.model.VersionedSetType;

public interface iVersionedSetWrapper {
	
	public String getUuid();
	
	public String getVersionedSetUuid();
	
	public void setVersionedSetUuid(final String versionedSetUuid);
	
	public String getVersionedSetName();
	
	public void setVersionedSetName(final String versionedSetName);
	
	public int getVersionedSetSize();
	
	public void setVersionedSetSize(final int versionedSetSize);
	
	public VersionedSetType getVersionedSetType();
	
	public void setVersionedSetType(final VersionedSetType versionedSetClassType);
	
}
