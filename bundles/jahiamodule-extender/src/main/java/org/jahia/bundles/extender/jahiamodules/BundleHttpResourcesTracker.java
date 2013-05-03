/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bundles.extender.jahiamodules;

import java.io.File;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletException;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.framework.util.MapToDictionary;
import org.apache.jasper.servlet.JspServlet;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.ops4j.pax.web.jsp.JasperClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service tracker instance to listen for {@link HttpService} service add/remove events in order to register/unregister servlets for module
 * JSPs and static resources.
 * 
 * @author Sergiy Shyrkov
 */
class BundleHttpResourcesTracker extends ServiceTracker {

    private static Logger logger = LoggerFactory.getLogger(BundleHttpResourcesTracker.class);

    @SuppressWarnings("unchecked")
    static List<URL> getJsps(Bundle bundle) {
        Enumeration<?> jsps = bundle.findEntries("/", "*.jsp", true);
        if (jsps != null && jsps.hasMoreElements()) {
            return EnumerationUtils.toList(jsps);
        }
        return Collections.emptyList();
    }

    static Map<String, String> getStaticResources(Bundle bundle) {
        Map<String, String> resources = null;
        String manifestHeader = (String) bundle.getHeaders().get("Jahia-Static-Resources");
        if (manifestHeader != null) {
            resources = new HashMap<String, String>();
            for (String clause : StringUtils.split(manifestHeader, ", ")) {
                if (clause.contains("=")) {
                    String[] mapping = StringUtils.split(clause, "=");
                    resources.put(mapping[0], mapping[1]);
                } else {
                    resources.put("/" + bundle.getSymbolicName() + clause, clause);
                }
            }
        }
        return resources == null || resources.isEmpty() ? Collections.<String, String> emptyMap() : resources;
    }

    private final Bundle bundle;

    private String bundleName;

    private String jspServletAlias;

    private Map<String, String> staticResources = Collections.emptyMap();

    BundleHttpResourcesTracker(Bundle bundle) {
        super(bundle.getBundleContext(), HttpService.class.getName(), null);
        this.bundle = bundle;
        this.bundleName = BundleUtils.getDisplayName(bundle);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) super.addingService(reference);

        long timer = System.currentTimeMillis();

        HttpContext httpContext = new FileHttpContext(FileHttpContext.getSourceURLs(bundle),
                httpService.createDefaultHttpContext());

        int resourceCount = registerStaticResources(httpService, httpContext);

        // register servlets for JSPs
        int jspCount = registerJsps(httpService, httpContext);

        if (resourceCount > 0 || jspCount > 0) {
            logger.info("Bundle {} registered {} JSPs and {} static resources in {} ms", new Object[] { bundleName,
                    jspCount, resourceCount, System.currentTimeMillis() - timer });
        }

        return httpService;
    }

    private int registerJsps(HttpService httpService, HttpContext httpContext) {
        List<URL> jsps = getJsps(bundle);
        if (jsps.isEmpty()) {
            return 0;
        }
        String bundleJspPathPrefix = "/" + bundle.getSymbolicName();
        jspServletAlias = bundleJspPathPrefix + "/*.jsp";

        @SuppressWarnings("unchecked")
        Map<String, String> jspConfig = (Map<String, String>) SpringContextSingleton.getBean("jspConfig");
        Map<String, String> cfg = new HashMap<String, String>(jspConfig.size() + 2);
        cfg.putAll(jspConfig);
        File scratchDirFile = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-jsps"),
                bundle.getSymbolicName());
        if (!scratchDirFile.exists()) {
            scratchDirFile.mkdirs();
        }
        cfg.put("scratchdir", scratchDirFile.getPath());
        cfg.put("alias", jspServletAlias);

        JspServletWrapper jspServletWrapper = new JspServletWrapper(new JspServlet(), new JasperClassLoader(bundle,
                JasperClassLoader.class.getClassLoader()), null, bundleJspPathPrefix, true);
        if (logger.isDebugEnabled()) {
            for (URL jsp : jsps) {
                logger.debug("Found JSP {} in bundle {}", jsp.getPath(), bundleName);
            }
        }
        try {
            httpService.registerServlet(jspServletAlias, jspServletWrapper, new MapToDictionary(cfg), httpContext);
        } catch (ServletException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        } catch (NamespaceException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        }

        return jsps.size();
    }

    private int registerStaticResources(HttpService httpService, HttpContext httpContext) {
        int count = 0;
        // Looking for static resources
        staticResources = getStaticResources(bundle);
        List<URL> jsps = getJsps(bundle);
        List<String> excludes = new ArrayList<String>();
        for (URL jsp : jsps) {
            if (jsp.getFile().indexOf("/",1) > 0) {
                excludes.add(jsp.getFile().substring(0, jsp.getFile().indexOf("/",1)));
            }
        }

        if (staticResources.size() > 0) {
            for (Map.Entry<String, String> res : staticResources.entrySet()) {
                try {
                    logger.debug("Registering static resource {}", res.getKey());
                    count++;
                    if (!excludes.contains(res.getValue())) {
                        httpService.registerResources(res.getKey(), res.getValue(), httpContext);
                    }
                } catch (NamespaceException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return count;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        HttpService httpService = (HttpService) service;
        int count = 0;
        for (Map.Entry<String, String> curEntry : staticResources.entrySet()) {
            logger.debug("Unregistering static resource {}", curEntry.getKey());
            count++;
            httpService.unregister(curEntry.getKey());
        }
        logger.info("Unregistered {} static resources for bundle {}", count, bundleName);

        // unregister servlets for JSPs
        if (jspServletAlias != null) {
            httpService.unregister(jspServletAlias);
            logger.info("Unregistered JSPs for bundle {}", bundleName);
        }

        super.removedService(reference, service);
    }
}