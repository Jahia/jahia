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
package org.jahia.admin.status;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.JahiaAdministration;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.admin.AbstractAdministrationModule;


/**
 * <p>Title: Manage Jahia status</p>
 * <p>Description: The purpose of this class is to present an administration
 * tool for viewing and manipulating Jahia's internal status, including
 * cache(s) status, memory consumption, etc... </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Inc.</p>
 * @author Serge Huber, Fulco Houkes
 * @version 3.1
 */

public class ManageStatus extends AbstractAdministrationModule {

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageStatus.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;


    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service(HttpServletRequest request,
                                        HttpServletResponse response)
            throws Exception {
        String operation = request.getParameter ("sub");

        if (operation.equals ("display")) {
            displaySettings (request, response, request.getSession());
        } else if (operation.equals ("process")) {
            processSettings (request, response, request.getSession());
        }
    }


    /**
     * Display the server settings page, using doRedirect().
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displaySettings (HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {
        // retrieve previous form values...
        Long freeMemoryInBytes = new Long (Runtime.getRuntime ().freeMemory ());
        Long totalMemoryInBytes = new Long (Runtime.getRuntime ().totalMemory ());
        Long maxMemoryInBytes = new Long(Runtime.getRuntime().maxMemory());
        Long outputCacheSize = (Long)session.getAttribute (CLASS_NAME + "outputCacheSize");
        Long outputCacheMaxSize = (Long)session.getAttribute (CLASS_NAME + "outputCacheMaxSize");

        // set request attributes...
        request.setAttribute ("freeMemoryInBytes", freeMemoryInBytes);
        request.setAttribute ("totalMemoryInBytes", totalMemoryInBytes);
        request.setAttribute ("maxMemoryInBytes", maxMemoryInBytes);
        request.setAttribute ("outputCacheSize", outputCacheSize);
        request.setAttribute ("outputCacheMaxSize", outputCacheMaxSize);

        JahiaAdministration.doRedirect (request, response, session, JSP_PATH + "status.jsp");
    }


    /**
     * Process and check the validity of the server settings page. If they are
     * not valid, display the server settings page to the user.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processSettings (HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {

        if (request.getParameter ("flushLocks") != null) {
            logger.debug ("Flushing Locks");
            ServicesRegistry.getInstance ().getLockService().purgeLocks();
        }

        if (request.getParameter ("flushAllCaches") != null) {
            logger.debug ("Flushing all caches");
            ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            JahiaBatchingClusterCacheHibernateProvider.flushAllCaches();
        }

        // get the cache factory instance
        CacheService cacheFactory = ServicesRegistry.getInstance().getCacheService();

        // get the registered cache names
        Iterator cacheNameIte = cacheFactory.getNames ().iterator();

        // for each cache..
        while (cacheNameIte.hasNext()) {

            // get the cache name
            String curCacheName = (String)cacheNameIte.next ();

            if (request.getParameter ("flush_" + curCacheName) != null) {
                Cache cache = ServicesRegistry.getInstance().getCacheService().getCache (curCacheName);
                if (cache != null) {
                    logger.debug ("Flushing cache : " + curCacheName);
                    cache.flush();
                }
            }
        }

        displaySettings (request, response, session);
    }

}