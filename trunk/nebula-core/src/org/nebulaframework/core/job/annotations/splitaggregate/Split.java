package org.nebulaframework.core.job.annotations.splitaggregate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//TODO FixDoc

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Split {
	// Defaults to void, indicating no class was stated.
	// This resolves to the class where this annotation
	// was defined
	Class<?> task() default Void.class;
}
