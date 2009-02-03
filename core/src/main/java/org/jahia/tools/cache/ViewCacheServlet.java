/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.tools.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.GroupCacheKey;

/**
 * Helps to precompile JSPs of a WebApp. The Servlet performs 3 actions depending on the passed params: - if jsp_name param is passed, the
 * servlet tries to forward to the JSP with the passed name - if compile_type=all is passed, the servlet tries to forward to all found JSPs
 * and generates a report HTML output - if compile_type=templates is passed, the servlet tries to forward to all found templates JSPs and
 * generates a report HTML output - if compile_type=site is passed, the servlet tries to forward to all found templates JSPs of a site and
 * generates a report HTML output - if no special param is passed, the servlet generates a page with links for the above described purposes
 */
public class ViewCacheServlet extends HttpServlet implements Servlet {

    public void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
            throws ServletException, IOException {
        doWork(aRequest, aResponse);
    }

    public void doPost(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        doWork(aRequest, aResponse);
    }

    /**
     * Performs depending on the passed request params the actions mentioned in the class description.
     */
    private void doWork(HttpServletRequest aRequest,
            HttpServletResponse aResponse) throws ServletException, IOException {
        aRequest.getSession(true);
        String nextJSP = "/jsp/jahia/tools/listCaches.jsp";
        String cacheName = null;
        String cacheKey = null;
        for (Object parameter : aRequest.getParameterMap().keySet()) {
            if (parameter instanceof String) {
                String stringParam = (String) parameter;
                if (stringParam.startsWith("view_")) {
                    cacheName = stringParam.substring(5);
                    cacheKey = aRequest.getParameter("key_"
                            + cacheName);
                    break;
                } else if (stringParam.startsWith("itemview_")) {
                    cacheName = stringParam.substring(9);
                    cacheKey = aRequest.getParameter("key");                    
                    break;
                } else if (stringParam.startsWith("flush_")) {
                    cacheName = stringParam.substring(6);
                    Cache cache = ServicesRegistry.getInstance()
                            .getCacheService().getCache(cacheName);
                    cache.flush();
                    cacheName = null;
                } else if (stringParam.startsWith("remove_")) {
                    cacheName = stringParam.substring(7);
                    cacheKey = aRequest.getParameter("key");                    
                    Cache cache = ServicesRegistry.getInstance()
                            .getCacheService().getCache(cacheName);
                    Object objectKey = cacheKey;
                    Class<?> keyType = null;
                    Object key = cache.getKeys().iterator().next();
                    if (key != null) {
                        keyType = key.getClass();
                        if (!keyType.equals(cacheKey.getClass())) {
                            if (keyType.equals(Integer.class)) {
                                objectKey = new Integer(cacheKey);
                            } else if (keyType.equals(Long.class)) {
                                objectKey = new Long(cacheKey);
                            } else if (keyType.equals(GroupCacheKey.class)) {
                                int keyIndex = cacheKey.indexOf(GroupCacheKey
                                        .getKeyGroupSeparator());
                                String groupKey = keyIndex != -1 ? cacheKey
                                        .substring(0, keyIndex) : null;
                                Set<String> groups = new HashSet<String>();
                                int groupIndex = cacheKey.indexOf(GroupCacheKey
                                        .getGroupSeparator(),
                                        keyIndex != -1 ? keyIndex
                                                + GroupCacheKey
                                                        .getKeyGroupSeparator()
                                                        .length() : 0);
                                while (groupIndex != -1) {
                                    int nextGroupIndex = cacheKey.indexOf(
                                            GroupCacheKey.getGroupSeparator(),
                                            groupIndex
                                                    + GroupCacheKey
                                                            .getGroupSeparator()
                                                            .length());
                                    String group = cacheKey.substring(groupIndex
                                            + GroupCacheKey.getGroupSeparator()
                                                    .length(),
                                            nextGroupIndex != -1 ? nextGroupIndex
                                                    : cacheKey.length());
                                    groups.add(group);
                                    groupIndex = nextGroupIndex;
                                }
                                objectKey = groupKey != null ? new GroupCacheKey(
                                        groupKey, groups) : new GroupCacheKey(
                                        groups);
                            }

                        }
                    }                    
                    cache.remove(objectKey);
                    cacheKey = null;
                }
            }
        }
        if (cacheName != null) {
            nextJSP = "/jsp/jahia/tools/viewCache.jsp";
            aRequest.setAttribute("cacheName", cacheName);
            aRequest.setAttribute("cacheKey", cacheKey);
        }

        RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher(nextJSP);
        dispatcher.forward(aRequest, aResponse);

    }
}