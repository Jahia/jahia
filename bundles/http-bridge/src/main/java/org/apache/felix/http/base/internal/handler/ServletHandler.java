/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.base.internal.handler;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.felix.http.base.internal.context.ExtServletContext;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ServletHandler
    extends AbstractHandler implements Comparable<ServletHandler>
{
    private final String alias;
    private String aliasWithSlash;
    private String jspPathPrefix;
    private final Servlet servlet;

    public ServletHandler(ExtServletContext context, Servlet servlet, String alias)
    {
        super(context);
        this.alias = alias;
        this.servlet = servlet;
        if (alias != null) {
            if (alias.endsWith("*.jsp") && alias.length() > "*.jsp".length()) {
                // special case of a handler for all JSPs of a bundle
                jspPathPrefix = alias.substring(0, alias.lastIndexOf("*.jsp"));
            } else {
                this.aliasWithSlash = alias + "/";
            }
        }
    }

    public String getAlias()
    {
        return this.alias;
    }

    public Servlet getServlet()
    {
        return this.servlet;
    }

    public void init()
        throws ServletException
    {
        String name = "servlet_" + getId();
        ServletConfig config = new ServletConfigImpl(name, getContext(), getInitParams());
        this.servlet.init(config);
    }

    public void destroy()
    {
        this.servlet.destroy();
    }

    public boolean matches(String uri)
    {
        if (uri == null) {
            return this.alias.equals("/");
        } else if (this.alias.equals("/")) {
            return uri.startsWith(this.alias);
        } else {
            return uri.equals(this.alias) || jspPathPrefix == null && uri.startsWith(aliasWithSlash)
                    || jspPathPrefix != null && uri.startsWith(jspPathPrefix) && uri.endsWith(".jsp");
        }
    }

    public boolean handle(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        String includePathInfo = (String) req.getAttribute("javax.servlet.include.path_info");
        // TODO should we handle forward here also?
        final boolean matches = matches(includePathInfo != null ? includePathInfo : req.getPathInfo());
        if (matches) {
            if (includePathInfo != null) {
                handleInclude(req, res);
            } else {
                doHandle(req, res);
            }
        }

        return matches;
    }

    private void doHandle(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        // set a sensible status code in case handleSecurity returns false
        // but fails to send a response
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        if (getContext().handleSecurity(req, res))
        {
            // reset status to OK for further processing
            res.setStatus(HttpServletResponse.SC_OK);

            // in case of a JSP alias (*.jsp) we do not wrap the request
            this.servlet.service(jspPathPrefix == null ? new ServletHandlerRequest(req, this.alias) : req, res);
        }
    }

    /**
     * Handles the RequestDispatcher.include and forward methods to call the servlet.
     */
    public void handleInclude(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        // TODO can we find a proper solution for the class loading issue with JSTL?
        Object oldLocalizationContext = req.getAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request");
        if (oldLocalizationContext != null)  {
            try {
                Locale locale = (Locale) oldLocalizationContext.getClass().getMethod("getLocale").invoke(oldLocalizationContext);
                ResourceBundle oldBundle = (ResourceBundle) oldLocalizationContext.getClass().getMethod("getResourceBundle").invoke(oldLocalizationContext);
                req.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request", new LocalizationContext(oldBundle,locale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.servlet.service(req, res);
        } finally {
            if (oldLocalizationContext!=null) {
                req.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.request", oldLocalizationContext);
            }
        }
    }

    public int compareTo(ServletHandler other)
    {
        return other.alias.length() - this.alias.length();
    }
}
