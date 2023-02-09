/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
