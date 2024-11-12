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
package org.apache.felix.http.base.internal.dispatch;

import org.apache.felix.http.base.internal.handler.ServletHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ServletPipeline
{
    private final ServletHandler[] handlers;

    public ServletPipeline(ServletHandler[] handlers)
    {
        this.handlers = handlers;
    }

    public boolean handle(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        for (ServletHandler handler : this.handlers) {
            if (handler.handle(req, res)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasServletsMapped()
    {
        return this.handlers.length > 0;
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
        for (ServletHandler handler : this.handlers) {
            if (handler.matches(path)) {
                return new Dispatcher(path, handler);
            }
        }

        return null;
    }

    private final class Dispatcher
        implements RequestDispatcher
    {
        private final String path;
        private final ServletHandler handler;

        public Dispatcher(String path, ServletHandler handler)
        {
            this.path = path;
            this.handler = handler;
        }

        public void forward(ServletRequest req, ServletResponse res)
            throws ServletException, IOException
        {
            if (res.isCommitted())
            {
                throw new ServletException("Response has been committed");
            }

            res.resetBuffer();

            this.handler.handleInclude((HttpServletRequest) req, (HttpServletResponse) res);
        }

        public void include(ServletRequest req, ServletResponse res)
            throws ServletException, IOException
        {
            this.handler.handleInclude((HttpServletRequest) req, (HttpServletResponse) res);
        }
    }
}
