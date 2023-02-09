/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.monitoring;

import javax.servlet.http.HttpServletRequest;

import org.jahia.exceptions.RenderTimeLimitExceededException;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderTimeMonitor;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.ConditionalExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aimed to check the current request rendering time and, if it exceeds the maximum configured one, throws the
 * {@link RenderTimeLimitExceededException} to stop the current request processing chain. <br>
 * We extends the {@link AbstractFilter} here to utilize the code for conditional execution.
 *
 * @author Sergiy Shyrkov
 */
public class DefaultRenderTimeMonitor extends ConditionalExecution implements RenderTimeMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRenderTimeMonitor.class);

    private static final String REQUEST_ATTR_NAME = RenderTimeMonitor.class.getName() + ".start";

    private long maxRequestRenderTime;

    private long getExecutionTime(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute(REQUEST_ATTR_NAME);
        if (startTime == null) {
            throw new IllegalStateException("Render chain monitoring has not been started for current HTTP request");
        }
        return System.currentTimeMillis() - startTime.longValue();
    }

    public long getMaxRequestRenderTime() {
        return maxRequestRenderTime;
    }

    @Override
    public void monitor(Resource resource, RenderContext renderContext) throws RenderTimeLimitExceededException {
        if (maxRequestRenderTime <= 0 || !areConditionsMatched(renderContext, resource)) {
            return;
        }

        long time = getExecutionTime(renderContext.getRequest());
        if (time > maxRequestRenderTime) {
            String msg = new StringBuffer(512).append("Request rendering time (").append(time)
                    .append(" ms) exceeded the maximum configured one (").append(maxRequestRenderTime)
                    .append(" ms)." + " The request processing will be stopped." + " Last rendered resource: ")
                    .append(resource).append(". Main resource: ").append(renderContext.getMainResource())
                    .append(". Request URL: ").append(renderContext.getRequest().getRequestURL()).toString();

            logger.warn(msg);

            throw new RenderTimeLimitExceededException(msg, time, maxRequestRenderTime);
        }
    }

    public void setMaxRequestRenderTime(long maxRequestRenderTime) {
        this.maxRequestRenderTime = maxRequestRenderTime;
    }

    @Override
    public void track(HttpServletRequest request) {
        if (maxRequestRenderTime > 0 && request.getAttribute(REQUEST_ATTR_NAME) == null) {
            request.setAttribute(REQUEST_ATTR_NAME, Long.valueOf(System.currentTimeMillis()));
        }
    }
}
