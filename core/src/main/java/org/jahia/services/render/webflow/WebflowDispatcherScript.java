/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
import org.springframework.web.util.WebUtils;
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
import java.util.regex.Pattern;

public class WebflowDispatcherScript extends RequestDispatcherScript {
    private static final Pattern MODULE_PREFIX_PATTERN = Pattern.compile("/modules/[^/]*/");

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

        flowPath = MODULE_PREFIX_PATTERN.matcher(view.getPath()).replaceFirst("");

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

            while (responseWrapper.getRedirect() != null && !responseWrapper.getRedirect().startsWith("http://") && !responseWrapper.getRedirect().startsWith("https://")) {
                final String qs = StringUtils.substringAfter(responseWrapper.getRedirect(), "?");
                final Map<String,String[]> params = new HashMap<String,String[]>();
                if (!StringUtils.isEmpty(qs)) {
                    String identifierNoDashes = StringUtils.remove(identifier, '-');
                    params.put("webflowexecution"+ identifierNoDashes,new String[] {StringUtils.substringAfterLast(qs, "webflowexecution" + identifierNoDashes+"=")});
                }
                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @SuppressWarnings("rawtypes")
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

                    @Override
                    public Object getAttribute(String name) {
                        if (WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE.equals(name)) {
                            return qs;
                        }
                        return super.getAttribute(name);
                    }

                    @Override
                    public String getQueryString() {
                        return qs;
                    }
                };
                rd = requestWrapper.getRequestDispatcher("/flow/"+flowPath + "?" + qs);
                responseWrapper = new StringResponseWrapper(response);
                rd.include(requestWrapper, responseWrapper);
            }
            if(responseWrapper.getRedirect() != null && (responseWrapper.getRedirect().startsWith("http://") || responseWrapper.getRedirect().startsWith("https://"))) {
                context.setRedirect(responseWrapper.getRedirect());
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
