/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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