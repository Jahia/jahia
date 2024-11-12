/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
                String token = (String) request.getAttribute(WebflowTokenListener.WEBFLOW_TOKEN);
                params.put(WebflowTokenListener.WEBFLOW_TOKEN, token);
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
