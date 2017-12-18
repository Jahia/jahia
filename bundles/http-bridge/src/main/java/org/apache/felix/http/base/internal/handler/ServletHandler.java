/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.felix.http.base.internal.handler;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import org.apache.felix.http.base.internal.context.ExtServletContext;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class ServletHandler
    extends AbstractHandler implements Comparable<ServletHandler>
{
    private static final Logger LOGGER = Logger.getLogger(ServletHandler.class.getName());
    private final String alias;
    private String aliasWithSlash;
    private String jspPathPrefix;
    private final boolean isRESTCall;
    private final Servlet servlet;

    public ServletHandler(ExtServletContext context, Servlet servlet, String alias)
    {
        super(context);
        this.servlet = servlet;

        // process alias before recording it if needed
        if (alias != null) {
            if (alias.endsWith("*.jsp") && alias.length() > "*.jsp".length()) {
                // special case of a handler for all JSPs of a bundle
                jspPathPrefix = alias.substring(0, alias.lastIndexOf("*.jsp"));

                isRESTCall = false;
            } else {

                // check whether we have a JAX-RS servlet.
                final int jaxrsIndex = alias.indexOf(".jaxrs");
                if (jaxrsIndex > 0) {
                    alias = alias.substring(0, jaxrsIndex);
                    isRESTCall = true;
                } else {
                    isRESTCall = false;
                }
                this.aliasWithSlash = alias + "/";
            }
        } else {
            isRESTCall = false;
        }

        this.alias = alias;
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
            return (isRESTCall && uri.startsWith(this.alias))
                    || uri.equals(this.alias)
                    || (jspPathPrefix == null && uri.startsWith(aliasWithSlash))
                    || (jspPathPrefix != null && uri.startsWith(jspPathPrefix) && uri.endsWith(".jsp"));
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
        if (getContext().handleSecurity(req, res))
        {
            // in case of a JSP alias (*.jsp) or if we are trying to access a JAX-RS servlet we do not wrap the request
            this.servlet.service(jspPathPrefix == null && !isRESTCall ? new ServletHandlerRequest(req, this.alias) : req, res);
        }
        else
        {
            // FELIX-3988: If the response is not yet committed and still has the default 
            // status, we're going to override this and send an error instead.
            if (!res.isCommitted() && res.getStatus() == SC_OK)
            {
                res.sendError(SC_FORBIDDEN);
            }
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
                LOGGER.info("Couldn't call method on " + oldLocalizationContext + " object. Error was: " + e.getLocalizedMessage());
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
