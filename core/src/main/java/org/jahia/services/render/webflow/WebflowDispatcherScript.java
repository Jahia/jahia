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
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.scripting.RequestDispatcherScript;
import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class WebflowDispatcherScript extends RequestDispatcherScript {
    private static final Logger logger = LoggerFactory.getLogger(WebflowDispatcherScript.class);

    private String flowPath;

    /**
     * Builds the script object
     */
    public WebflowDispatcherScript(View view) {
        super(view);
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     *
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        final View view = getView();
        if (view == null) {
            throw new RenderException("View not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("View '" + view + "' resolved for resource: " + resource);
            }
        }

        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();

        flowPath = view.getPath().replaceFirst("/modules/[^/]*/", "");

        String identifier;
        try {
            identifier = resource.getNode().getIdentifier();
        } catch (RepositoryException e) {
            throw new RenderException(e);
        }

        RequestDispatcher rd = request.getRequestDispatcher("/flow/"+flowPath);

        Object oldModule = request.getAttribute("currentModule");
        request.setAttribute("currentModule", view.getModule());

        if (logger.isDebugEnabled()) {
            dumpRequestAttributes(request);
        }

        StringResponseWrapper responseWrapper = new StringResponseWrapper(response);

        try {
            FlowDefinitionRegistry reg = ((FlowDefinitionRegistry)view.getModule().getContext().getBean("jahiaFlowRegistry"));
            final GenericApplicationContext applicationContext = (GenericApplicationContext) reg.getFlowDefinition(flowPath).getApplicationContext();
            applicationContext.setClassLoader(view.getModule().getClassLoader());
            applicationContext.setResourceLoader(new ResourceLoader() {
                @Override
                public org.springframework.core.io.Resource getResource(String location) {
                    return applicationContext.getParent().getResource("/"+flowPath+"/"+location);
                }

                @Override
                public ClassLoader getClassLoader() {
                    return view.getModule().getClassLoader();
                }
            });

            rd.include(request, responseWrapper);

            while (responseWrapper.getRedirect() != null) {
                String qs = StringUtils.substringAfter(responseWrapper.getRedirect(), "?");
                final Map<String,String[]> params = new HashMap<String,String[]>();
                if (!StringUtils.isEmpty(qs)) {
                    params.put("webflowexecution"+ identifier.replaceAll("-",""),new String[] {StringUtils.substringAfterLast(qs, "webflowexecution" + identifier.replaceAll("-","")+"=")});
                }
                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public Map getParameterMap() {
                        return params;
                    }

                    @Override
                    public String getParameter(String name) {
                        return params.containsKey(name) ? params.get(name) [0] : null;
                    }

                    @Override
                    public Enumeration getParameterNames() {
                        return new Vector(params.keySet()).elements();
                    }

                    @Override
                    public String[] getParameterValues(String name) {
                        return params.get(name);
                    }
                };

                rd = requestWrapper.getRequestDispatcher("/flow/"+flowPath + "?" + qs);
                responseWrapper = new StringResponseWrapper(response);
                rd.include(requestWrapper, responseWrapper);
            }
        } catch (ServletException e) {
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule", oldModule);
        }
        try {
            return responseWrapper.getString();
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    public String getFlowPath() {
        return "/flow/"+flowPath;
    }

    public boolean isMultipartRequest(final HttpServletRequest req) {
        final String contentType = req.getHeader("Content-Type");

        return ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0));
    }
}
