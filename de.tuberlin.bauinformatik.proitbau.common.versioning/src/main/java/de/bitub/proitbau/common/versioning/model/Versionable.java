/*
 * Package: de.bitub.proitbau.common.versioning.model
 * Project: SetVers
 * File: Versionable.java
 * Date: 25.09.2012
 * Time: 17:41:20
 * Company: TU-Berlin
 * Author: "Vladislav Fedotov"
 * E-mail: <a
 * href="mailto:vladislav.fedotov@tu-berlin.de">vladislav.fedotov@tu-berlin
 * .de</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.model;

public interface Versionable {
	
	public abstract String getUuid();
	
	public abstract void setUuid(final String uuid);
}
