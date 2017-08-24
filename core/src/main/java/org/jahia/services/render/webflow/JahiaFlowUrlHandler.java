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
package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.filter.TemplateAttributesFilter;
import org.jahia.services.render.scripting.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class JahiaFlowUrlHandler extends DefaultFlowUrlHandler {

    private static final Logger logger = LoggerFactory.getLogger(JahiaFlowUrlHandler.class);

    @Override
    public String getFlowExecutionKey(HttpServletRequest request) {
        JCRNodeWrapper n = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (n != null) {
            try {
                String name = getExecutionName(request, n);
                return request.getParameter(name);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return super.getFlowExecutionKey(request);
            }
        }
        return super.getFlowExecutionKey(request);
    }

    @Override
    public String getFlowId(HttpServletRequest request) {
        WebflowDispatcherScript script = (WebflowDispatcherScript) request.getAttribute("script");
        if (script != null) {
            String path = script.getFlowPath();
            return StringUtils.substringAfter(path, "/flow/");
        }

        return super.getFlowId(request);
    }

    @Override
    public String createFlowExecutionUrl(String flowId, String flowExecutionKey, HttpServletRequest request) {
        WebflowDispatcherScript script = (WebflowDispatcherScript) request.getAttribute("script");
        JCRNodeWrapper currentNode = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (script != null && currentNode != null) {
            StringBuilder path = new StringBuilder(request.getRequestURI());
            path.append('?');
            Map<String, String> params = new HashMap<String, String>();
            try {
                String name = getExecutionName(request, currentNode);
                params.put(name, flowExecutionKey);
                if (request.getParameter("jsite") != null) {
                    params.put("jsite", request.getParameter("jsite"));
                }
                Object forcedLocale = request.getAttribute(TemplateAttributesFilter.FORCED_LOCALE_ATTRIBUTE);
                if (forcedLocale != null) {
                    params.put(WebflowAction.WEBFLOW_LOCALE_PARAMETER, forcedLocale.toString());
                }
                appendQueryParameters(path, params, getEncodingScheme(request));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
            }
            return path.toString();
        }

        return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
    }

    private String getExecutionName(HttpServletRequest request, JCRNodeWrapper n) throws RepositoryException {
        String name = "webflowexecution" + StringUtils.replace(n.getIdentifier(), "-", "_");
        Script r = (Script) request.getAttribute("script");
        if (r != null && r.getView() != null && !r.getView().getKey().equals("default")) {
            name += "__" + r.getView().getKey();
        }
        return name;
    }

    @Override
    public String createFlowDefinitionUrl(String flowId, AttributeMap<?> input, HttpServletRequest request) {
        return super.createFlowDefinitionUrl(flowId, input, request);
    }

}
