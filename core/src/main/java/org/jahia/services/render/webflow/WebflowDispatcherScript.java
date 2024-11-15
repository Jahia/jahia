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
package org.jahia.services.render.webflow;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.scripting.RequestDispatcherScript;
import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

import javax.annotation.Nullable;
import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

public class WebflowDispatcherScript extends RequestDispatcherScript {
    private static final Pattern MODULE_PREFIX_PATTERN = Pattern.compile("/modules/[^/]*/");

    private static final Logger logger = LoggerFactory.getLogger(WebflowDispatcherScript.class);

    private String flowPath;

    private boolean xssFilteringEnabled = true;

    /**
     * Builds the script object
     *
     * @param view
     *            the view
     */
    public WebflowDispatcherScript(View view) {
        super(view);
    }

    /**
     * Builds the script object
     *
     * @param view
     *            the view
     * @param xssFilteringEnabled
     *            if the XSS filtering of request parameters should be enabled
     */
    public WebflowDispatcherScript(View view, boolean xssFilteringEnabled) {
        this(view);
        this.xssFilteringEnabled = xssFilteringEnabled;
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

        String identifier;
        try {
            identifier = resource.getNode().getIdentifier();
        } catch (RepositoryException e) {
            throw new RenderException(e);
        }
        String identifierNoDashes = StringUtils.replace(identifier, "-", "_");
        if (!view.getKey().equals("default")) {
            identifierNoDashes += "__" + view.getKey();
        }

        HttpServletRequest request;
        HttpServletResponse response = context.getResponse();

        @SuppressWarnings("unchecked")
        final Map<String, List<String>> parameters = (Map<String, List<String>>) context.getRequest().getAttribute("actionParameters");

        if (xssFilteringEnabled && parameters != null) {
            final Map<String, String[]> m = Maps.transformEntries(parameters, new Maps.EntryTransformer<String, List<String>, String[]>() {
                @Override
                public String[] transformEntry(@Nullable String key, @Nullable List<String> value) {
                    return value != null ? value.toArray(new String[value.size()]) : null;
                }
            });
            request = new WebflowHttpServletRequestWrapper(context.getRequest(), m, identifierNoDashes);
        } else {
            request = new WebflowHttpServletRequestWrapper(context.getRequest(), new HashMap<>(context.getRequest().getParameterMap()), identifierNoDashes);
        }

        String s = (String) request.getSession().getAttribute("webflowResponse"+identifierNoDashes);
        if (s != null ) {
            request.getSession().removeAttribute("webflowResponse"+identifierNoDashes);
            return s;
        }

        // skip aggregation for potentials fragments under the webflow
        boolean aggregationSkippedForWebflow = false;
        if(!AggregateFilter.skipAggregation(context.getRequest())) {
            aggregationSkippedForWebflow = true;
            context.getRequest().setAttribute(AggregateFilter.SKIP_AGGREGATION, true);
        }

        flowPath = MODULE_PREFIX_PATTERN.matcher(view.getPath()).replaceFirst("");

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

            String redirect = responseWrapper.getRedirect();
            if(redirect != null && context.getRedirect() == null) {
                // if we have an absolute redirect, set it and move on
                if(redirect.startsWith("http:/") || redirect.startsWith("https://")) {
                    context.setRedirect(responseWrapper.getRedirect());
                } else {
                    while (redirect != null) {
                        final String qs = StringUtils.substringAfter(responseWrapper.getRedirect(), "?");
                        final Map<String, String[]> params = new HashMap<String, String[]>();
                        if (!StringUtils.isEmpty(qs)) {
                            String[] pairs = qs.split("&");
                            for (String pair : pairs) {
                                int idx = pair.indexOf("=");
                                params.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), new String[]{URLDecoder.decode(pair.substring(idx + 1), "UTF-8")});
                            }
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
                                return params.containsKey(name) ? params.get(name)[0] : null;
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
                        rd = requestWrapper.getRequestDispatcher("/flow/" + flowPath + "?" + qs);
                        responseWrapper = new StringResponseWrapper(response);
                        rd.include(requestWrapper, responseWrapper);

                        String oldRedirect = redirect;
                        redirect = responseWrapper.getRedirect();
                        if(redirect != null) {
                            // if we have an absolute redirect, exit the loop
                            if(redirect.startsWith("http://") || redirect.startsWith("https://")) {
                                context.setRedirect(redirect);
                                break;
                            }
                        } else if (request.getMethod().equals("POST")) {
                            // set the redirect to the last non-null one
                            request.getSession().setAttribute("webflowResponse"+identifierNoDashes, responseWrapper.getString());
                            context.setRedirect(oldRedirect);
                        }
                    }
                }
            }
        } catch (ServletException e) {
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule", oldModule);
        }
        try {
            if (aggregationSkippedForWebflow) {
                request.removeAttribute(AggregateFilter.SKIP_AGGREGATION);
            }
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

    private static class WebflowHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> m;
        private final String identifierNoDashes;

        public WebflowHttpServletRequestWrapper(HttpServletRequest request, Map<String, String[]> m, String identifierNoDashes) {
            super(request);
            this.m = m;
            this.identifierNoDashes = identifierNoDashes;
            m.remove("webflowexecution" + identifierNoDashes);
        }

        @Override
        public String[] getParameterValues(String name) {
            return m.get(name);
        }

        @Override
        public String getParameter(String name) {
            if (name.equals("webflowexecution" + identifierNoDashes)) {
                return super.getParameter(name);
            }
            return (m.get(name) != null && m.get(name).length > 0) ? m.get(name)[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
           return m;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return new Vector(m.keySet()).elements();
        }
    }
}
