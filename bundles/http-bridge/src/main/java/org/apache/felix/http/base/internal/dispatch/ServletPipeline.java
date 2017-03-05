/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
