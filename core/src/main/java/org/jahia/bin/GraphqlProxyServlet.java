/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
