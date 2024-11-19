/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.profiler.Profiler;

import javax.servlet.http.HttpSession;

/**
 * MetricsLoggingFilter
 * <p/>
 * Calls the logging service to log the display of a resource.
 * Also initializes profiling information.
 */
public class MetricsLoggingFilter extends AbstractFilter {

    private MetricsLoggingService loggingService;

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        if (!loggingService.isProfilingEnabled()) {
            return null;
        }
        String profilerName = "render module " + resource.getNodePath();
        Profiler profiler = loggingService.createNestedProfiler("MAIN", profilerName);
        profiler.start("render filters for " + resource.getNodePath());
        context.getRequest().setAttribute("profiler", profiler);
        return null;
    }


    @Override
    public String execute(String previousOut, RenderContext context, Resource resource,
                          RenderChain chain) throws Exception {

        if (!loggingService.isProfilingEnabled()) {
            return previousOut;
        }

        String profilerName = "render module " + resource.getNodePath();

        String sessionID = "";
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            sessionID = session.getId();
        }

        if (loggingService.isEnabled()) {
            if (resource.isNodeLoaded()) {
                // The resource has its node loaded from JCR, so node properties are available immediately and can be accessed safely.
                JCRNodeWrapper node = resource.getNode();
                loggingService.logContentEvent(context.getUser().getName(), context.getRequest().getRemoteAddr(), sessionID, node.getIdentifier(), node.getPath(), node.getNodeTypes().get(0), "moduleViewed", resource.getResolvedTemplate(), "the full render chain");
            } else {
                // The resource does not have its node loaded from JCR, so we avoid accessing node properties to avoid loading it from JCR for better performance.
                loggingService.logContentEvent(context.getUser().getName(), context.getRequest().getRemoteAddr(), sessionID, "", resource.getNodePath(), "", "moduleViewed", resource.getResolvedTemplate(), "the cache filter");
            }
        }

        loggingService.stopNestedProfiler("MAIN", profilerName);

        return previousOut;
    }
}
