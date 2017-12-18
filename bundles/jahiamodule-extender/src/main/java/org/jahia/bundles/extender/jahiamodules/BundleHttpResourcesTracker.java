/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.utils.collections.MapToDictionary;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.servlet.JspServlet;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.ops4j.pax.web.jsp.JasperClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Service tracker instance to listen for {@link HttpService} service add/remove events in order to register/unregister servlets for module
 * JSPs and static resources.
 * 
 * @author Sergiy Shyrkov
 */
public class BundleHttpResourcesTracker extends ServiceTracker<HttpService, HttpService> {

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
                    if (bundle.findEntries(clause, "*", false) != null) {
                        resources.put("/" + bundle.getSymbolicName() + clause, clause);
                    }
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
    public HttpService addingService(ServiceReference<HttpService> reference) {
        try {
            HttpService httpService = super.addingService(reference);

            long timer = System.currentTimeMillis();

            HttpContext httpContext = new FileHttpContext(bundle, httpService.createDefaultHttpContext());

            int resourceCount = registerStaticResources(httpService, httpContext);

            // register servlets for JSPs
            int jspCount = registerJsps(httpService, httpContext);

            if (resourceCount > 0 || jspCount > 0) {
                logger.info("Bundle {} registered {} JSPs and {} static resources in {} ms", new Object[] { bundleName,
                        jspCount, resourceCount, System.currentTimeMillis() - timer });
            }

            return httpService;
        } catch (Exception e) {
            logger.error("Error when adding service",e);
            return super.addingService(reference);
        }
    }

    protected int registerJsps(HttpService httpService, HttpContext httpContext) {
        List<URL> jsps = getJsps(bundle);
        if (jsps.isEmpty()) {
            return 0;
        }
        String bundleJspPathPrefix = "/" + bundle.getSymbolicName();
        jspServletAlias = bundleJspPathPrefix + "/*.jsp";
        registerJspServlet(httpService, httpContext, bundle, bundleName, jspServletAlias, null, bundleJspPathPrefix);
        if (logger.isDebugEnabled()) {
            for (URL jsp : jsps) {
                logger.debug("Found JSP {} in bundle {}", jsp.getPath(), bundleName);
            }
        }
        return jsps.size();
    }


    /**
     * Utility method to register a JSP servlet
     * @param httpService
     * @param httpContext
     * @param bundle
     * @param bundleName
     * @param jspServletAlias
     * @param jspFile
     * @param jspFilePrefix
     */
    public static void registerJspServlet(HttpService httpService, HttpContext httpContext, Bundle bundle, String bundleName,
                                      String jspServletAlias, String jspFile, String jspFilePrefix) {
        @SuppressWarnings("unchecked")
        Map<String, String> jspConfig = (Map<String, String>) SpringContextSingleton.getBean("jspConfig");
        Map<String, String> cfg = new HashMap<String, String>(jspConfig.size() + 2);
        cfg.putAll(jspConfig);
        if (StringUtils.equals(cfg.get("development"), "auto")) {
            cfg.put("development", SettingsBean.getInstance().isDevelopmentMode() ? "true" : "false");
        }
        File scratchDirFile = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-jsps"),
                bundle.getSymbolicName()+"-"+bundle.getBundleId());
        if (!scratchDirFile.exists()) {
            scratchDirFile.mkdirs();
        }
        cfg.put("scratchdir", scratchDirFile.getPath());
        cfg.put("alias", jspServletAlias);

        JspServletWrapper jspServletWrapper = new JspServletWrapper(new JspServlet(), new JasperClassLoader(bundle,
                JasperClassLoader.class.getClassLoader()), jspFile, jspFilePrefix, true);
        try {
            httpService.registerServlet(jspServletAlias, jspServletWrapper, new MapToDictionary(cfg), httpContext);
        } catch (ServletException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        } catch (NamespaceException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        }
    }

    public static void flushJspCache(Bundle bundle, String jspFile) {
        try {
            File scratchDirFile = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-jsps"),
                    bundle.getSymbolicName()+"-"+bundle.getBundleId());
            if (jspFile != null && jspFile.length() > 0) {
                JspCompilationContext jspCompilationContext = new JspCompilationContext(jspFile, false, null, JahiaContextLoaderListener.getServletContext(), null, null);
                String javaPath = jspCompilationContext.getJavaPath();
                String classPath = StringUtils.substringBeforeLast(javaPath,".java") + ".class";
                FileUtils.deleteQuietly(new File(scratchDirFile, javaPath));
                FileUtils.deleteQuietly(new File(scratchDirFile, classPath));
            }
        } catch (JasperException e) {
            logger.error(e.getMessage(),e);
        }
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
    public void removedService(ServiceReference<HttpService> reference, HttpService service) {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        int count = 0;
        for (Map.Entry<String, String> curEntry : staticResources.entrySet()) {
            logger.debug("Unregistering static resource {}", curEntry.getKey());
            count++;
            service.unregister(curEntry.getKey());
        }
        logger.info("Unregistered {} static resources for bundle {}", count, bundleName);

        // unregister servlets for JSPs
        if (jspServletAlias != null) {
            service.unregister(jspServletAlias);
            logger.info("Unregistered JSPs for bundle {}", bundleName);
        }

        super.removedService(reference, service);
    }
}