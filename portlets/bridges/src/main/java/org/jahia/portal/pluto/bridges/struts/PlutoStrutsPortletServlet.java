/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.portal.pluto.bridges.struts;

import org.apache.pluto.container.driver.PortletServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * This class is a remplacement for the default portlet servlet dispatcher because we need to make the servlet
 * request and response objects available to the Struts bridge. We do this using ThreadLocal's as we have no way to
 * pass the data another way.
 *
 * @author loom
 *         Date: Jun 19, 2009
 *         Time: 4:53:57 PM
 */
public class PlutoStrutsPortletServlet extends PortletServlet {

    private static ThreadLocal<HttpServletRequest> tlServletRequest = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> tlServletResponse = new ThreadLocal<HttpServletResponse>();
    private static ThreadLocal<ServletContext> tlServletContext = new ThreadLocal<ServletContext>();

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doGet(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doPost(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    @Override
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        tlServletRequest.set(httpServletRequest);
        tlServletResponse.set(httpServletResponse);
        tlServletContext.set(getServletContext());
        super.doPut(httpServletRequest, httpServletResponse);
        tlServletRequest.set(null);
        tlServletResponse.set(null);
        tlServletContext.set(null);
    }

    public static HttpServletRequest getHttpServletRequest() {
        return tlServletRequest.get();
    }

    public static HttpServletResponse getHttpServletResponse() {
        return tlServletResponse.get();
    }

    public static ServletContext getStaticServletContext() {
        return tlServletContext.get();
    }
}
