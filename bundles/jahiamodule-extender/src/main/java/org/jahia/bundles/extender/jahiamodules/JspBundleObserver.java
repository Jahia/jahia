package org.jahia.bundles.extender.jahiamodules;

import org.jahia.bundles.extender.jahiamodules.render.BundleDispatcherServlet;
import org.jahia.bundles.extender.jahiamodules.render.BundleScriptResolver;
import org.ops4j.pax.web.jsp.JspServletWrapper;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.net.URL;
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
        String bundleName = bundle.getSymbolicName() + " v" + (String) bundle.getHeaders().get("Implementation-Version");
        int registered = 0;
        for (URL url : urls) {
            URL[] sourceURLs = FileHttpContext.getSourceURLs(bundle);
            final JspServletWrapper jspServlet = new JspServletWrapper(bundle, null);
            String urlAlias = url.getPath();
            Hashtable<String, String> props = new Hashtable<String, String>();
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
                    bundleHttpService.unregister(urlAlias);
                    registeredBundleAliases.remove(urlAlias);
                }
                registered++;
                logger.debug("Registering JSP {} for bundle {}", urlAlias, bundleName);
                
                bundleHttpService.registerServlet(urlAlias, jspServlet, props, httpContext);
                registeredBundleAliases.add(urlAlias);

                bundleDispatcherServlet.getJspMappings().put(urlAlias, jspServlet);
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
        for (URL url : urls) {
            String urlAlias = url.getPath();
            logger.info("Unregistering JSP " + urlAlias + " for bundle " + bundle.getSymbolicName() + " v" + (String) bundle.getHeaders().get("Implementation-Version"));
            Set<String> registeredBundleAliases = registeredAliases.get(bundle);
            if (registeredBundleAliases != null && registeredBundleAliases.contains(urlAlias)) {
                bundleHttpService.unregister(urlAlias);
                registeredBundleAliases.remove(urlAlias);
            }
            bundleDispatcherServlet.getJspMappings().remove(urlAlias);
        }
    }
}
