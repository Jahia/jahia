package org.jahia.osgi.http.bridge;

import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
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
            logger.info("Installing bundle [{}]", url);
            Bundle bundle = context.installBundle(url.toExternalForm());
            installed.add(bundle);
        }

        ServiceTracker st = new ServiceTracker(context, StartLevel.class.getName(), null);
        st.open();
        StartLevel sl = ((StartLevel)st.getService());

        for (Bundle bundle : installed) {
            if (bundle.getSymbolicName().equals("org.apache.felix.fileinstall")) {
                // Start fileInstall only on level 2
                sl.setBundleStartLevel(bundle,2);
            }

            // we first check if it is a fragment bundle, in which case we will not start it.
            if (bundle.getHeaders().get("Fragment-Host") == null) {
                bundle.start();
            }
        }

        String[] beanNames = SpringContextSingleton.getInstance().getContext().getBeanNamesForType(null, false, false);
        for (String beanName : beanNames) {
            try {
                Object bean = SpringContextSingleton.getInstance().getContext().getBean(beanName);
                List<String> classNames = new ArrayList<String>();
                if (classNameAccessible(bean.getClass().getName())) {
                    classNames.add(bean.getClass().getName());
                    for (Class<?> classInterface : bean.getClass().getInterfaces()) {
                        if (classNameAccessible(classInterface.getName())) {
                            classNames.add(classInterface.getName());
                        }
                    }
                    Hashtable<String, String> serviceProperties = new Hashtable<String, String>(1);
                    serviceProperties.put("jahiaSpringBeanName", beanName);
                    serviceRegistrations.add(context.registerService(classNames.toArray(new String[classNames.size()]), bean, serviceProperties));
                    logger.debug("Registered bean {} as OSGi service under names: {}", beanName, classNames);
                }
            } catch (Exception t) {
                logger.warn("Couldn't register bean " + beanName + " since it couldn't be retrieved: " + t.getMessage());
            }
        }
    }

    private boolean classNameAccessible(String classOrInterfaceName) {
        if (classOrInterfaceName.startsWith("java.")) {
            // we ignore all Java classes for the moment.
            return false;
        }
        if (classOrInterfaceName.startsWith("org.apache.felix.framework.")) {
            // we ignore all Felix framework classes for the moment.
            return false;
        }
        // @todo implement import/export checks for accessibility here.
        return true;
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
