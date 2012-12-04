package org.jahia.osgi.http.bridge;

import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial startup OSGi provision activator
 *
 * @author loom
 *         Date: Oct 11, 2010
 *         Time: 5:18:48 PM
 */
public final class ProvisionActivator
        implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(ProvisionActivator.class);

    private final ServletContext servletContext;
    private BundleContext bundleContext;
    static private ProvisionActivator instance = null;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();

    public ProvisionActivator(ServletContext servletContext) {
        this.servletContext = servletContext;
        instance = this;
    }

    public static ProvisionActivator getInstance() {
        return instance;
    }

    public void start(BundleContext context)
            throws Exception {
        bundleContext = context;
        servletContext.setAttribute(BundleContext.class.getName(), context);

        ArrayList<Bundle> installed = new ArrayList<Bundle>();
        for (URL url : findBundles()) {
            this.servletContext.log("Installing bundle [" + url + "]");
            Bundle bundle = context.installBundle(url.toExternalForm());
            installed.add(bundle);
        }

        for (Bundle bundle : installed) {
            // we first check if it is a fragment bundle, in which case we will not start it.
            if (bundle.getHeaders().get("Fragment-Host") == null) {
                bundle.start();
            }
        }

        // @todo can we also expose module application context beans ?
        String[] beanNames = SpringContextSingleton.getInstance().getContext().getBeanNamesForType(null, false, false);
        for (String beanName : beanNames) {
            // @todo can we somehow use the name of the service and not just the class name ? Maybe we need a property for this ?
            try {
                Object bean = SpringContextSingleton.getInstance().getContext().getBean(beanName);
                List<String> classNames = new ArrayList<String>();
                classNames.add(bean.getClass().getName());
                for (Class classInterface : bean.getClass().getInterfaces()) {
                    classNames.add(classInterface.getName());
                }
                serviceRegistrations.add(context.registerService(classNames.toArray(new String[classNames.size()]), bean, null));
                logger.debug("Registered bean " + beanName + " as OSGi service under names: " + classNames);
            } catch (Throwable t) {
                logger.warn("Couldn't register bean " + beanName + " since it couldn't be retrieved: " + t.getMessage());
            }
        }
    }

    public void stop(BundleContext context)
            throws Exception {
        bundleContext = null;
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
        servletContext.removeAttribute(BundleContext.class.getName());
        instance = null;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    private List<URL> findBundles()
            throws Exception {
        ArrayList<URL> list = new ArrayList<URL>();
        for (Object o : this.servletContext.getResourcePaths("/WEB-INF/bundles/")) {
            String name = (String) o;
            if (name.endsWith(".jar")) {
                URL url = this.servletContext.getResource(name);
                if (url != null) {
                    list.add(url);
                }
            }
        }

        return list;
    }
}
