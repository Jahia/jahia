/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.jsp;

import org.osgi.framework.Bundle;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * A wrapper for the JspServlet class from Pax Web, but that will actually allow sharing of the JspServlet and
 * class loader instances.
 */
public class JahiaJspServletWrapper extends org.ops4j.pax.web.jsp.JspServletWrapper {


    public JahiaJspServletWrapper(Bundle bundle) {
        super(bundle);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        Object currentLocaleResolver = req.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
        if (req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH) != null && req.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO) != null) {
            String servletPath = req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH).toString() + req.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO).toString();
            req.setAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH, servletPath);
            req.setAttribute(RequestDispatcher.INCLUDE_PATH_INFO, null);
        }

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
                //
            }
        });


        try {
            super.service(req, res);
        } finally {
            req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, currentLocaleResolver);
        }

    }
}
