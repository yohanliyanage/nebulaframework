package org.nebulaframework.core.job.annotations;

import java.io.Serializable;

import org.nebulaframework.core.job.GridJob;

// TODO FixDoc
public interface AnnotationProcessor {

	boolean isValid(Serializable obj);
	GridJob<?,?> adapt(Serializable obj) throws IllegalArgumentException;
}
