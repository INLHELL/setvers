/*
 * Package: de.bitub.proitbau.versioning.annotations
 * Project: Setvers
 * File: VersionedEntity.java
 * Date: 25.05.2010
 * Time: 13:26:33
 * Company: TU-Berlin
 * Author: Vladislav Fedotov
 * E-mail: <a href="mailto:vladislav.fedotov@tu-berlin.de">Vladislav Fedotov</a>
 * Version: 1.0
 */
package de.bitub.proitbau.common.versioning.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.TYPE
})
public @interface VersionedEntity {
	
	SetStrategy setStrategy() default SetStrategy.SET_PER_CLASS;
	
	String[] boundBy() default {};
	
	String name() default "Default";
	
	boolean visible() default false;
	
	String[] divisorFieldNames() default {};
	
}
