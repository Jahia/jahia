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

package org.jahia.osgi;

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
public final class ProvisionActivator implements BundleActivator {

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

        //exposeBeansAsServices(context);
    }

    private void exposeBeansAsServices(BundleContext context) {
        long timer = System.currentTimeMillis();
        int registered = 0;
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
                    serviceProperties.put("org.jahia.spring.bean.name", beanName);
                    serviceRegistrations.add(context.registerService(classNames.toArray(new String[classNames.size()]), bean, serviceProperties));
                    registered++;
                    logger.debug("Registered bean {} as OSGi service under names: {}", beanName, classNames);
                }
            } catch (Exception t) {
                logger.warn("Couldn't register bean " + beanName + " since it couldn't be retrieved: " + t.getMessage());
            }
        }
        logger.info("Registered {} Spring beans as OSGi services in {} ms", registered,
                (System.currentTimeMillis() - timer));
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
