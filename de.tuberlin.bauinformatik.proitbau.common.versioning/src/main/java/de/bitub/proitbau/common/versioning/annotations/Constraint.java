package de.bitub.proitbau.common.versioning.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.bitub.proitbau.common.versioning.model.Resolverable;

@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.TYPE
})
public @interface Constraint {
	
	Class<? extends Resolverable> resolver();
	
	String description() default "";
}
