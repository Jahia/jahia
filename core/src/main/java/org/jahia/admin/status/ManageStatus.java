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

package org.jahia.admin.status;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.CacheManager;

import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.JahiaAdministration;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.ehcache.EhCacheProvider;


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
    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (ManageStatus.class);

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

        CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider")).getCacheManager();
        
        if (request.getParameter ("flushAllCaches") != null) {
            CacheHelper.flushAllCaches();
        } else if (request.getParameter ("flushOutputCaches") != null) {
            CacheHelper.flushOutputCaches();
        } else if (request.getParameter ("flushHibernateCaches") != null) {
            CacheHelper.flushHibernateCaches();
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
                    logger.info("Flushing cache: " + curCacheName);
                    cache.flush(false);
                }
            }
        }
        
        for (String cacheName : ehcacheManager.getCacheNames()) {
            if (request.getParameter ("flush_ehcache_" + cacheName) != null) {
                net.sf.ehcache.Cache cache = ehcacheManager.getCache(cacheName);
                if (cache != null) {
                    logger.info("Flushing cache: " + cacheName);
                    // flush without notifying the other cluster nodes
                    cache.removeAll(true);
                    // reset statistics
                    cache.clearStatistics();
                }
            }
        }

        displaySettings (request, response, session);
    }

}