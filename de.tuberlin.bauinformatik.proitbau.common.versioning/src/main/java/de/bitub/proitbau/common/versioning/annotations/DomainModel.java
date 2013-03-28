package de.bitub.proitbau.common.versioning.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.TYPE
})
public @interface DomainModel {
	// Marker annotation, shows that the fields of this object will be taken for
	// the reverse conversion
}
