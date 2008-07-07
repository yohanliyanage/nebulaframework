/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.nebulaframework.util.profiling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StopWatch;

/**
 * Aspect which enables profiling of execution times of methods.
 * <p>
 * <b>AspectJ Aspect</b>
 * 
 * @author Yohan Liyanage
 * @version 1.0
 */
@Aspect
public class ProfilingAspect {
	
	private static Log log = LogFactory.getLog(ProfilingAspect.class);
	
	/**
	 * Advice of Profiling Aspect. Measures and logs the exection
	 * time of advised method.
	 * 
	 * @param pjp {ProceedingJoinPoint}
	 * @return Object result
	 * @throws Throwable if exception occurs
	 */
    @Around("profilingPointcut()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
    	
    	StopWatch sw = new StopWatch();
		
    	Log localLog = null;
		
		try {
			// Get Log
			localLog = LogFactory.getLog(pjp.getTarget().getClass());
			
			// Start StopWatch
			sw.start();
			
			// Proceed Invocation
			return pjp.proceed();
			
		} finally{
			
			// Stop StopWatch
			sw.stop();
			if (localLog ==null) {
				// If local Log not found, use ProfilingAspect's Log
				localLog = log;
			}
			
			//Log Stats
			localLog.debug("[Profiling] " + pjp.getTarget().getClass().getName() + "->" + pjp.getSignature().getName() + "() | " + sw.getLastTaskTimeMillis() + " ms");
		}
    }
    
    /**
     * AspectJ Pointcut Definition for Profiling Aspect
     */
    @Pointcut("@annotation(org.nebulaframework.util.profiling.annotations.ProfileExecution)")
    public void profilingPointcut(){}
}