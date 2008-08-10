package org.nebulaframework.core.job.annotations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nebulaframework.core.job.GridJob;
import org.springframework.util.Assert;

// TODO FixDoc
public class AnnotatedJobSupport {

	private static final Log log = LogFactory.getLog(AnnotatedJobSupport.class);
	private static final AnnotatedJobSupport instance = new AnnotatedJobSupport();
	
	private List<AnnotationProcessor> annotationProcessors = new ArrayList<AnnotationProcessor>();
	
	private AnnotatedJobSupport() {
		// No-external-instantiation
	}

	public static AnnotatedJobSupport getInstance() {
		return instance;
	}

	public void setAnnotationProcessors(
			List<AnnotationProcessor> annotationProcessors) {
		this.annotationProcessors = annotationProcessors;
	}


	public void addAnnotationProcessor(AnnotationProcessor processor) {
		annotationProcessors.add(processor);
	}
	
	private GridJob<?,?> adapt(Serializable obj) throws IllegalArgumentException {

		Assert.notNull(obj);
		
		log.debug("[Annotations Processor] Processing " + obj.getClass().getSimpleName());
		
		for (AnnotationProcessor processor : annotationProcessors) {
			if (processor.isValid(obj)) {
				return processor.adapt(obj);
			}
		}
		
		throw new IllegalArgumentException("Unable to detect Grid Job");
	}
	
	public static GridJob<?,?> adaptAsGridJob(Serializable obj) throws IllegalArgumentException{
		return instance.adapt(obj);
	}
}
