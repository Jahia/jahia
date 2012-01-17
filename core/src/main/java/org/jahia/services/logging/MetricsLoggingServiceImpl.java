/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.slf4j.profiler.ProfilerRegistry;

import java.util.*;


/**
 * Default implementation of the metrics logging service, that logs to a specific Log4J appender using template
 * strings to customize output. The template strings are partially hardcoded in this class, and partially configured
 * in the Spring configuration file.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2009
 */
public class MetricsLoggingServiceImpl implements MetricsLoggingService {
    private transient static Logger metricsLogger = LoggerFactory.getLogger("loggingService");
    private transient static Logger profilerMetricsLogger = LoggerFactory.getLogger("profilerLoggingService");
    private Map<String, String> logTemplatesMap;
    private Set<String> ignoreUsers = new HashSet<String>();
    private static MetricsLoggingServiceImpl instance;
    private final static String headerTemplate = "user {} ip {} session {} identifier {} path {} nodetype {} ";
    private ThreadLocal<Stack<Profiler>> threadLocal = new ThreadLocal<Stack<Profiler>>();

    public MetricsLoggingServiceImpl() {
    }

    public void setLogTemplatesMap(Map<String, String> logTemplatesMap) {
        this.logTemplatesMap = new LinkedHashMap<String, String>(logTemplatesMap.size());
        for (Map.Entry<String, String> entry : logTemplatesMap.entrySet()) {
            this.logTemplatesMap.put(entry.getKey(), headerTemplate + entry.getValue());
        }
    }

    public void setIgnoreUsers(Set<String> ignoreUsers) {
        this.ignoreUsers = ignoreUsers;
    }

    /**
     * Log some metric about a node.
     *
     * @param user           user achieving the operation
     * @param ipAddress      ip address of the user
     * @param sessionID      if available, the identifier of the session, otherwise null or an empty string is fine. Note
     *                       that if you use null it will be outputted verbatim in the log.
     * @param nodeIdentifier if available, the node identifier on which the event took place, otherwise null
     * @param path           the node path on which the operation has been achieved
     * @param nodeType       the type of the node
     * @param logTemplate    the name of the template log you want to use.
     * @param args           variable list of arguments depending of the template you choose
     */
    public void logContentEvent(String user, String ipAddress, String sessionID, String nodeIdentifier, String path, String nodeType,
                                String logTemplate, String... args) {
        if (!isEnabled() || ignoreUsers.contains(user)) {
            return;
        }
        String template = logTemplatesMap.get(logTemplate);
        String[] templateParameters = new String[6 + args.length];
        templateParameters[0] = user;
        templateParameters[1] = ipAddress;
        templateParameters[2] = sessionID;
        templateParameters[3] = nodeIdentifier;
        templateParameters[4] = path;
        templateParameters[5] = nodeType;
        int i = 6;
        for (String arg : args) {
            templateParameters[i++] = arg;
        }
        if (template == null) {
            metricsLogger.trace("Couldn't find template for " + logTemplate + " and args " + templateParameters);
        } else {
            metricsLogger.trace(template, templateParameters);
        }
    }

    /**
     * Start a profiler and start the associated action (if the profilerName is not foudn it will create it)
     *
     * @param profilerName name of the profiler you want to use or create
     * @param action       the action you want to profile
     */
    public void startProfiler(String profilerName, String action) {
        if (!isProfilingEnabled()) {
            return;
        }
        final ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();
        Profiler profiler = profilerRegistry.get(profilerName);
        if (profiler == null) {
            profiler = new Profiler(profilerName);
            profiler.setLogger(profilerMetricsLogger);
            profiler.registerWith(profilerRegistry);
        }
        profiler.start(action);
    }

    /**
     * Stop all profiling for this profiler name
     *
     * @param profilerName the name of the profiler you want to stop
     */
    public void stopProfiler(String profilerName) {
        if (!isProfilingEnabled()) {
            return;
        }
        final ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();
        Profiler profiler = profilerRegistry.get(profilerName);
        if (profiler != null) {
            profiler.stop().log();
        }
        profilerRegistry.clear();
        threadLocal.set(null);
    }

    /**
     * Create a sub profiler of an existing profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     * @return the nested profiler
     */
    public Profiler createNestedProfiler(String parentProfilerName, String nestedProfilerName) {
        if (!isProfilingEnabled()) {
            return null;
        }
        Stack<Profiler> profilers;
        if (threadLocal.get() == null) {
            profilers = new Stack<Profiler>();
            final ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();
            profilers.push(profilerRegistry.get(parentProfilerName));
            threadLocal.set(profilers);
        } else {
            profilers = threadLocal.get();
        }
        Profiler profiler = profilers.peek();
        if (profiler == null) {
            profiler = startProfiler(parentProfilerName);
            profilers.push(profiler);
        }
        Profiler nestedProfiler = profiler.startNested(nestedProfilerName);
        profilers.push(nestedProfiler);
        return nestedProfiler;
    }

    /**
     * Stop a nested profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     */
    public void stopNestedProfiler(String parentProfilerName, String nestedProfilerName) {
        if (!isProfilingEnabled()) {
            return;
        }
        Stack<Profiler> profilers = threadLocal.get();
        final Profiler profiler = profilers.pop();
        if (profiler.getName().equals(nestedProfilerName)) {
            profiler.stop();
        }
    }

    public Profiler startProfiler(String profilerName) {
        if (!isProfilingEnabled()) {
            return null;
        }
        final ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();
        Profiler profiler = profilerRegistry.get(profilerName);
        if (profiler == null) {
            profiler = new Profiler(profilerName);
            profiler.setLogger(profilerMetricsLogger);
            profiler.registerWith(profilerRegistry);
        }
        return profiler;
    }

    /**
     * Returns the singleton instance, and creates it if not existing yet
     *
     * @return the singleton instance, either existing or newly created.
     */
    public static MetricsLoggingServiceImpl getInstance() {
        if (instance == null) {
            instance = new MetricsLoggingServiceImpl();
        }
        return instance;
    }

    public boolean isEnabled() {
        return metricsLogger.isTraceEnabled();
    }
    
    public boolean isProfilingEnabled() {
        return profilerMetricsLogger.isDebugEnabled();
    }
}
