/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * OSGi proxy servlet
 *
 * Delegates to existing OSGI dispatcher
 */
public final class GraphqlProxyServlet extends HttpServlet {
    private ServiceTracker<HttpServlet, HttpServlet> tracker;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            BundleContext bundleContext = this.getBundleContext();
            String str = "(&(" + "objectClass" + "=" +
                    HttpServlet.class.getName() + ")" +
                    "(http.felix.dispatcher=*)" + ")";
            this.tracker = new ServiceTracker<>(bundleContext, bundleContext.createFilter(str), null);
            this.tracker.open();
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpServlet dispatcher = this.tracker.getService();
        HttpServletRequest wrapper = new PathTranslationWrapper(req);
        if (dispatcher != null) {
            dispatcher.service(wrapper, res);
        } else {
            res.sendError(503);
        }

    }

    @Override
    public void destroy() {
        this.tracker.close();
        super.destroy();
    }

    private BundleContext getBundleContext() throws ServletException {
        Object context = this.getServletContext().getAttribute(BundleContext.class.getName());
        if (context instanceof BundleContext) {
            return (BundleContext)context;
        } else {
            throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() + "] not set in servlet context");
        }
    }

    private static class PathTranslationWrapper extends HttpServletRequestWrapper {
        public PathTranslationWrapper(HttpServletRequest req) {
            super(req);
        }

        @Override
        public String getServletPath() {
            return "/modules";
        }

        @Override
        public String getPathInfo() {
            return super.getServletPath().substring("/modules".length()) + Optional.ofNullable(super.getPathInfo()).orElse("");
        }
    }
}
