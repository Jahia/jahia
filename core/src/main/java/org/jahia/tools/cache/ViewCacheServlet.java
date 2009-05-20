/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
        String nextJSP = "/tools/listCaches.jsp";
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
            nextJSP = "/tools/viewCache.jsp";
            aRequest.setAttribute("cacheName", cacheName);
            aRequest.setAttribute("cacheKey", cacheKey);
        }

        RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher(nextJSP);
        dispatcher.forward(aRequest, aResponse);

    }
}