/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
/*
 * Created on Aug 12, 2004
 */
package org.jahia.spring.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class ProfilingInterceptor implements MethodInterceptor {

    private long executionTimeMinimum;
    private Logger logger = Logger.getLogger(ProfilingInterceptor.class);
    public void setExecutionTimeMinimum(long executionTimeMinimum) {
        this.executionTimeMinimum = executionTimeMinimum;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        long start = System.currentTimeMillis();

        Object returnValue = invocation.proceed();

        dumpInfo(invocation, System.currentTimeMillis()-start);
        return returnValue;
    }

    private void dumpInfo(MethodInvocation invocation, long ms) {
        if (logger.isDebugEnabled() && executionTimeMinimum <= ms) {
            Method m = invocation.getMethod();
            Object target = invocation.getThis();
            Object[] args = invocation.getArguments();

            logger.debug("Executed method: " + m.getName());
            logger.debug("On object of type: " + target.getClass().getName());

            logger.debug("With arguments:");
            for (int x = 0; x < args.length; x++) {
                logger.debug("    > " + args[x]);
            }

            logger.debug("Took: " + ms + " ms");
        }
    }

}