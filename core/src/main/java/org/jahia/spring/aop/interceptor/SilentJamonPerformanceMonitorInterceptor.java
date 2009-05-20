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
 package org.jahia.spring.aop.interceptor;

import org.springframework.aop.interceptor.JamonPerformanceMonitorInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Same as Spring's JamonPerfomanceMonitorInterceptor, except that it doesn't
 * output to the logger, only gathers statistics.
 * User: Serge Huber
 * Date: 27 juin 2006
 * Time: 14:31:53
 * Copyright (C) Jahia Inc.
 */
public class SilentJamonPerformanceMonitorInterceptor extends JamonPerformanceMonitorInterceptor {

    public SilentJamonPerformanceMonitorInterceptor () {

    }

    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
        Monitor monitor = MonitorFactory.start(name);
        try {
            return invocation.proceed();
        } finally {
            monitor.stop();
        }
    }

}