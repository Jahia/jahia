package org.jahia.bundles.extender.jahiamodules;

import org.apache.jasper.servlet.JspServlet;
import org.jahia.bundles.extender.jahiamodules.render.BundleDispatcherServlet;
import org.jahia.bundles.extender.jahiamodules.render.BundleScriptResolver;
import org.jahia.services.SpringContextSingleton;
import org.ops4j.pax.web.jsp.JasperClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A bundle observer for JSP script files
 */
public class JspBundleObserver extends ScriptBundleObserver {

    private static Logger logger = LoggerFactory.getLogger(JspBundleObserver.class);

    private BundleDispatcherServlet bundleDispatcherServlet = null;
    private Map<Bundle, HttpService> bundleHttpServices = new HashMap<Bundle,HttpService>();
    private Map<Bundle, List<JspRegistration>> pendingRegistrations = new HashMap<Bundle, List<JspRegistration>>();
    private Map<Bundle, Set<String>> registeredAliases = new HashMap<Bundle, Set<String>>();
    private Map<Bundle, JspServlet> bundleJspServlets = new HashMap<Bundle, JspServlet>();
    private Map<Bundle, URLClassLoader> bundleURLClassLoaders = new HashMap<Bundle, URLClassLoader>();

    class JspRegistration {
        private boolean registration;
        private List<URL> urls;

        public JspRegistration(boolean registration, List<URL> urls) {
            this.registration = registration;
            this.urls = urls;
        }

        public boolean isRegistration() {
            return registration;
        }

        public List<URL> getUrls() {
            return urls;
        }
    }

    public JspBundleObserver(BundleScriptResolver bundleScriptResolver, BundleDispatcherServlet bundleDispatcherServlet) {
        super(bundleScriptResolver);
        this.bundleDispatcherServlet = bundleDispatcherServlet;
    }

    public void setBundleHttpService(Bundle bundle, HttpService httpService) {
        if (httpService == null) {
            HttpService bundleHttpService = bundleHttpServices.get(bundle);
            if ((bundleHttpService != null) && (registeredAliases.get(bundle) != null)) {
                for (String registeredAlias : registeredAliases.get(bundle)) {
                    bundleHttpService.unregister(registeredAlias);
                }
                registeredAliases.remove(bundle);
            }
            bundleHttpServices.remove(bundle);
            JspServlet bundleJspServlet = bundleJspServlets.remove(bundle);
            if (bundleJspServlet != null) {
                bundleJspServlet.destroy();
            }
            bundleURLClassLoaders.remove(bundle);
        } else {
            bundleHttpServices.put(bundle, httpService);
            if (pendingRegistrations.containsKey(bundle)) {
                List<JspRegistration> bundleJspRegistrations = pendingRegistrations.get(bundle);
                for (JspRegistration jspRegistration : bundleJspRegistrations) {
                    if (jspRegistration.isRegistration()) {
                        registerJSPs(bundle, jspRegistration.getUrls(), httpService);
                    } else {
                        unregisterJSPs(bundle, jspRegistration.getUrls(), httpService);
                    }
                }
                pendingRegistrations.remove(bundle);
            }
        }
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        if (!bundleHttpServices.containsKey(bundle)) {
            List<JspRegistration> pendingBundleRegistrations = pendingRegistrations.get(bundle);
            if (pendingBundleRegistrations == null) {
                pendingBundleRegistrations = new ArrayList<JspRegistration>();
            }
            pendingBundleRegistrations.add(new JspRegistration(true, urls));
            pendingRegistrations.put(bundle, pendingBundleRegistrations);
        } else {
            HttpService bundleHttpService = bundleHttpServices.get(bundle);
            registerJSPs(bundle, urls, bundleHttpService);
        }
        super.addingEntries(bundle, urls);
    }

    private void registerJSPs(Bundle bundle, List<URL> urls, HttpService bundleHttpService) {
        if (urls.size() == 0) {
            return;
        }
        String bundleName = bundle.getSymbolicName() + " v"
                + (String) bundle.getHeaders().get("Implementation-Version");
        int registered = 0;

        URL[] sourceURLs = FileHttpContext.getSourceURLs(bundle);
        JspServlet jspServlet = bundleJspServlets.get(bundle);
        URLClassLoader urlClassLoader = bundleURLClassLoaders.get(bundle);

        @SuppressWarnings("unchecked")
        Map<String, String> jspConfig = new HashMap<String, String>(
                (Map<String, String>) SpringContextSingleton.getBean("jspConfig"));
        File scratchDirFile = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-jsps"),
                bundle.getSymbolicName());
        if (!scratchDirFile.exists()) {
            scratchDirFile.mkdirs();
        }
        jspConfig.put("scratchdir", scratchDirFile.getPath());

        for (URL url : urls) {
            String urlAlias = url.getPath();
            boolean firstInstance = false;
            if (jspServlet == null) {
                jspServlet = new JspServlet();
                firstInstance = true;
                bundleJspServlets.put(bundle, jspServlet);
            }
            if (urlClassLoader == null) {
                urlClassLoader = new JasperClassLoader(bundle, JasperClassLoader.class.getClassLoader());
                bundleURLClassLoaders.put(bundle, urlClassLoader);
            }
            JspServletWrapper jspServletWrapper = new JspServletWrapper(jspServlet, urlClassLoader, urlAlias, firstInstance);
            Hashtable<String, String> props = new Hashtable<String, String>(jspConfig.size() + 1);
            props.putAll(jspConfig);
            props.put("alias", urlAlias);

            HttpContext httpContext = new FileHttpContext(sourceURLs,bundleHttpService.createDefaultHttpContext());
            try {
                Set<String> registeredBundleAliases = registeredAliases.get(bundle);
                if (registeredBundleAliases == null) {
                    registeredBundleAliases = new HashSet<String>();
                    registeredAliases.put(bundle,registeredBundleAliases);
                }
                if (registeredBundleAliases.contains(urlAlias)) {
                    logger.warn("URL Alias {} already registered, unregistering old servlet...", urlAlias);
                    bundleHttpService.unregister("/"+bundle.getSymbolicName() + urlAlias);
                    registeredBundleAliases.remove(urlAlias);
                }
                registered++;
                logger.debug("Registering JSP {} for bundle {}", urlAlias, bundleName);

                bundleHttpService.registerServlet("/"+bundle.getSymbolicName() + urlAlias, jspServletWrapper, props, httpContext);
                registeredBundleAliases.add(urlAlias);

                bundleDispatcherServlet.getJspMappings().put("/"+bundle.getSymbolicName() + urlAlias, jspServletWrapper);
            } catch (ServletException e) {
                logger.error("Error registering JSP " + urlAlias, e);
            } catch (NamespaceException e) {
                logger.error("Error registering JSP " + urlAlias, e);
            }
        }
        logger.info("Registered {} JSPs for bundle {}", registered, bundleName);
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        if (!bundleHttpServices.containsKey(bundle)) {
            List<JspRegistration> pendingBundleRegistrations = pendingRegistrations.get(bundle);
            if (pendingBundleRegistrations == null) {
                pendingBundleRegistrations = new ArrayList<JspRegistration>();
            }
            pendingBundleRegistrations.add(new JspRegistration(false, urls));
            pendingRegistrations.put(bundle, pendingBundleRegistrations);
        } else {
            HttpService bundleHttpService = bundleHttpServices.get(bundle);
            unregisterJSPs(bundle, urls, bundleHttpService);
        }
        super.removingEntries(bundle, urls);
    }

    private void unregisterJSPs(Bundle bundle, List<URL> urls, HttpService bundleHttpService) {
        if (urls.size() == 0) {
            return;
        }
        String bundleName = bundle.getSymbolicName() + " v" + (String) bundle.getHeaders().get("Implementation-Version");
        int count = 0;
        for (URL url : urls) {
            String urlAlias = url.getPath();
            logger.debug("Unregistering JSP {} for bundle {}", urlAlias, bundleName);
            Set<String> registeredBundleAliases = registeredAliases.get(bundle);
            if (registeredBundleAliases != null && registeredBundleAliases.contains(urlAlias)) {
                bundleHttpService.unregister("/"+bundle.getSymbolicName() +urlAlias);
                registeredBundleAliases.remove(urlAlias);
            }
            bundleDispatcherServlet.getJspMappings().remove("/"+bundle.getSymbolicName() +urlAlias);
            count++;
        }
        
        logger.info("Unregistered {} JSPs for bundle {}", count, bundleName);
    }
}
