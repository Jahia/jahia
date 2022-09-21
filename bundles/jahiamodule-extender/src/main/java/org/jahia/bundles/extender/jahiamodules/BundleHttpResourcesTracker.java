/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.utils.collections.MapToDictionary;
import org.apache.jasper.*;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldCache;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.bundles.extender.jahiamodules.jsp.JahiaJspServletWrapper;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.jsp.tagext.TagLibraryInfo;
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
    private final Bundle bundle;
    private String bundleName;
    private String jspServletAlias;
    private Map<String, String> staticResources = Collections.emptyMap();

    BundleHttpResourcesTracker(Bundle bundle) {
        super(bundle.getBundleContext(), HttpService.class.getName(), null);
        this.bundle = bundle;
        this.bundleName = BundleUtils.getDisplayName(bundle);
    }

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
        return resources == null || resources.isEmpty() ? Collections.<String, String>emptyMap() : resources;
    }

    /**
     * Utility method to register a JSP servlet
     *
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
                bundle.getSymbolicName() + "-" + bundle.getBundleId());
        if (!scratchDirFile.exists()) {
            scratchDirFile.mkdirs();
        }
        cfg.put("scratchdir", scratchDirFile.getPath());
        cfg.put("alias", jspServletAlias);
        cfg.put("compilerSourceVM", "1.7");
        cfg.put("compilerTargetVM", "1.7");

        JahiaJspServletWrapper jspServletWrapper = new JahiaJspServletWrapper(bundle);

        try {
            httpService.registerServlet(jspServletAlias, jspServletWrapper, new MapToDictionary(cfg), httpContext);
        } catch (ServletException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        } catch (NamespaceException e) {
            logger.error("Error registering JSPs for bundle " + bundleName, e);
        }
    }

    public static void flushJspCache(Bundle bundle, String jspFile) {
        File scratchDirFile = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-jsps"),
                bundle.getSymbolicName() + "-" + bundle.getBundleId());
        if (jspFile != null && jspFile.length() > 0) {
            JspCompilationContext jspCompilationContext = new JspCompilationContext(jspFile,
                                                                                    new EmptyOptions(),
                                                                                    JahiaContextLoaderListener.getServletContext(),
                                                                                    null,
                                                                                    null);
            String javaPath = jspCompilationContext.getJavaPath();
            String classPath = StringUtils.substringBeforeLast(javaPath, ".java") + ".class";
            FileUtils.deleteQuietly(new File(scratchDirFile, javaPath));
            FileUtils.deleteQuietly(new File(scratchDirFile, classPath));
        }
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
                logger.info("Bundle {} registered {} JSPs and {} static resources in {} ms", new Object[]{bundleName,
                        jspCount, resourceCount, System.currentTimeMillis() - timer});
            }

            return httpService;
        } catch (Exception e) {
            logger.error("Error when adding service", e);
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

    private int registerStaticResources(HttpService httpService, HttpContext httpContext) {
        int count = 0;
        // Looking for static resources
        staticResources = getStaticResources(bundle);
        List<URL> jsps = getJsps(bundle);
        List<String> excludes = new ArrayList<String>();
        for (URL jsp : jsps) {
            if (jsp.getFile().indexOf("/", 1) > 0) {
                excludes.add(jsp.getFile().substring(0, jsp.getFile().indexOf("/", 1)));
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

    private static class EmptyOptions implements Options {
        @Override
        public boolean getErrorOnUseBeanInvalidClassAttribute() {
            return false;
        }

        @Override
        public boolean getKeepGenerated() {
            return false;
        }

        @Override
        public boolean isPoolingEnabled() {
            return false;
        }

        @Override
        public boolean getMappedFile() {
            return false;
        }

        @Override
        public boolean getClassDebugInfo() {
            return false;
        }

        @Override
        public int getCheckInterval() {
            return 0;
        }

        @Override
        public boolean getDevelopment() {
            return false;
        }

        @Override
        public boolean getDisplaySourceFragment() {
            return false;
        }

        @Override
        public boolean isSmapSuppressed() {
            return false;
        }

        @Override
        public boolean isSmapDumped() {
            return false;
        }

        @Override
        public TrimSpacesOption getTrimSpaces() {
            return null;
        }

        @Override
        public String getIeClassId() {
            return null;
        }

        @Override
        public File getScratchDir() {
            return null;
        }

        @Override
        public String getClassPath() {
            return null;
        }

        @Override
        public String getCompiler() {
            return null;
        }

        @Override
        public String getCompilerTargetVM() {
            return null;
        }

        @Override
        public String getCompilerSourceVM() {
            return null;
        }

        @Override
        public String getCompilerClassName() {
            return null;
        }

        @Override
        public TldCache getTldCache() {
            return null;
        }

        @Override
        public String getJavaEncoding() {
            return null;
        }

        @Override
        public boolean getFork() {
            return false;
        }

        @Override
        public JspConfig getJspConfig() {
            return null;
        }

        @Override
        public boolean isXpoweredBy() {
            return false;
        }

        @Override
        public TagPluginManager getTagPluginManager() {
            return null;
        }

        @Override
        public boolean genStringAsCharArray() {
            return false;
        }

        @Override
        public int getModificationTestInterval() {
            return 0;
        }

        @Override
        public boolean getRecompileOnFail() {
            return false;
        }

        @Override
        public boolean isCaching() {
            return false;
        }

        @Override
        public Map<String, TagLibraryInfo> getCache() {
            return null;
        }

        @Override
        public int getMaxLoadedJsps() {
            return 0;
        }

        @Override
        public int getJspIdleTimeout() {
            return 0;
        }

        @Override
        public boolean getStrictQuoteEscaping() {
            return false;
        }

        @Override
        public boolean getQuoteAttributeEL() {
            return false;
        }
    }
}
