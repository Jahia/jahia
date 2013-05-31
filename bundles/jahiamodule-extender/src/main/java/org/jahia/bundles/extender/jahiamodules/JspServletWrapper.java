/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * A wrapper for the JspServlet class from Pax Web, but that will actually allow sharing of the JspServlet and
 * class loader instances.
 */
public class JspServletWrapper implements Servlet {

    private final JspServlet jspServlet;
    private final URLClassLoader urlClassLoader;
    private final String jspFile;
    private boolean performInit;
    private String jspFilePrefix;

    public JspServletWrapper(JspServlet jspServlet, URLClassLoader urlClassLoader, String jspFile, String jspFilePrefix, boolean performInit) {
        this.jspServlet = jspServlet;
        this.urlClassLoader = urlClassLoader;
        this.jspFile = jspFile;
        this.jspFilePrefix = jspFilePrefix;
        this.performInit = performInit;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        if (performInit) {
            try {
                ContextClassLoaderUtils.doWithClassLoader(urlClassLoader,
                        new Callable<Void>() {
                            public Void call()
                                    throws Exception {
                                jspServlet.init(config);
                                return null;
                            }
                        }
                );
            } catch (Exception e) {
                throw new ServletException("Error during servlet init", e);
            }
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return jspServlet.getServletConfig();
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
        String jspPath = getJspFilePath(req);
        if (jspPath != null) {
            req.setAttribute(Constants.JSP_FILE, jspPath);
        }

        Object currentLocaleResolver = req.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
        req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new LocaleResolver() {

            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                HttpSession session = request.getSession();
                if (session != null) {
                    Locale currentLocale = (Locale) session.getAttribute(org.jahia.api.Constants.SESSION_LOCALE);
                    if (currentLocale != null) {
                        return currentLocale;
                    }
                }
                return request.getLocale();
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
            }
        });

        // Prepare attributes for subsequent includes
        String p = (String)req.getAttribute("javax.servlet.include.servlet_path") + (String)req.getAttribute("javax.servlet.include.path_info");
        req.setAttribute("javax.servlet.include.path_info", null);
        req.setAttribute("javax.servlet.include.servlet_path", p);

        try {
            ContextClassLoaderUtils.doWithClassLoader(urlClassLoader,
                    new Callable<Void>() {
                        public Void call()
                                throws Exception {
                            jspServlet.service(req, res);
                            return null;
                        }
                    }
            );
        } catch (Exception e) {
            throw new ServletException("Error during servlet servicing", e);
        }
        req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, currentLocaleResolver);
    }

    private String getJspFilePath(final ServletRequest req) {
        String jspPath = jspFile;
        if (jspPath == null && jspFilePrefix != null && req instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            String servletPath = StringUtils.defaultIfEmpty(
                    (String) httpRequest.getAttribute("javax.servlet.include.servlet_path"),
                    httpRequest.getServletPath());
            String pathInfo = StringUtils.defaultIfEmpty(
                    (String) httpRequest.getAttribute("javax.servlet.include.path_info"), httpRequest.getPathInfo());
            if (pathInfo != null && servletPath != null && "/modules".equals(servletPath)
                    && pathInfo.startsWith(jspFilePrefix) && pathInfo.length() > jspFilePrefix.length()) {
                jspPath = pathInfo.substring(jspFilePrefix.length());
            }
        }
        return jspPath;
    }

    @Override
    public String getServletInfo() {
        return jspServlet.getServletInfo();
    }

    @Override
    public void destroy() {
        // we never destroy the JSP servlet here, we do it at a higher level.
    }
}
