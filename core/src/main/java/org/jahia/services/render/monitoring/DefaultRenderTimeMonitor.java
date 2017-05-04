/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
