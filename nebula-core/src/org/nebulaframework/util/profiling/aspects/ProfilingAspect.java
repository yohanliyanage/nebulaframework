package org.nebulaframework.util.profiling.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StopWatch;


@Aspect
public class ProfilingAspect {
	
	private static Log log = LogFactory.getLog(ProfilingAspect.class);
	
    @Around("profilingPointcut()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
    	StopWatch sw = new StopWatch();
		Log localLog = null;
		try {
			localLog = LogFactory.getLog(pjp.getTarget().getClass());
			sw.start();
			return pjp.proceed();
		} finally{
			sw.stop();
			if (localLog ==null) {
				localLog = log;
			}
			localLog.debug("[Profiling] " + pjp.getTarget().getClass().getName() + "->" + pjp.getSignature().getName() + "() | " + sw.getLastTaskTimeMillis() + " ms");
		}
    }
    
    @Pointcut("@annotation(org.nebulaframework.util.profiling.annotations.ProfileExecution)")
    public void profilingPointcut(){}
}