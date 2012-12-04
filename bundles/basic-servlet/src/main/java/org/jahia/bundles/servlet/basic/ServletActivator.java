package org.jahia.bundles.servlet.basic;

import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.Servlet;
import java.util.*;

/**
 * Basic servlet bundle activator
 * User: loom
 * Date: Oct 20, 2010
 * Time: 2:36:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServletActivator implements BundleActivator {

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    private ServiceTracker httpServiceTracker;

    public void start(BundleContext context)
            throws Exception {
        Bundle bundle = context.getBundle();

        if (bundle != null) {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("alias", "/basic");
            BasicServlet basicServlet = new BasicServlet();
            basicServlet.setBundleContext(context);
            serviceRegistrations.add(context.registerService(Servlet.class.getName(), basicServlet, props));
        }

        httpServiceTracker = new ServiceTracker(context, HttpService.class.getName(), null) {
            public Object addingService(ServiceReference reference) {
                HttpService httpService = (HttpService) super.addingService(reference);
                return httpService;
            }

            public void removedService(ServiceReference reference, Object service) {
                super.removedService(reference, service);
            }
        };
        httpServiceTracker.open();
    }

    public void stop(BundleContext context)
            throws Exception {
        httpServiceTracker.close();
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }
}
