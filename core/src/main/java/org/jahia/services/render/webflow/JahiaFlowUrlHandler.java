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

package org.jahia.services.render.webflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class JahiaFlowUrlHandler extends DefaultFlowUrlHandler {

    @Override
    public String getFlowExecutionKey(HttpServletRequest request) {
        JCRNodeWrapper n = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (n != null) {
            try {
                return request.getParameter( "webflow-execution-" + n.getIdentifier());
            } catch (RepositoryException e) {
                e.printStackTrace();
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
        JCRNodeWrapper n = (JCRNodeWrapper) request.getAttribute("currentNode");
        if (script != null && n != null) {
            StringBuffer path = new StringBuffer(request.getRequestURI());
            path.append('?');
            Map<String,String> params = new HashMap<String, String>();
            try {
                params.put("webflow-execution-" + n.getIdentifier(), flowExecutionKey);
                appendQueryParameters(path, params, getEncodingScheme(request));
            } catch (RepositoryException e) {
                e.printStackTrace();
                return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
            }
            return path.toString();
        }

        return super.createFlowExecutionUrl(flowId, flowExecutionKey, request);
    }

    @Override
    public String createFlowDefinitionUrl(String flowId, AttributeMap input, HttpServletRequest request) {
        return super.createFlowDefinitionUrl(flowId, input, request);
    }

}
