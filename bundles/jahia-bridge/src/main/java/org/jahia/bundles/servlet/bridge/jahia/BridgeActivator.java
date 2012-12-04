package org.jahia.bundles.servlet.bridge.jahia;

import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Jahia Bridge bundle activator
 * User: loom
 * Date: Oct 21, 2010
 * Time: 9:07:49 AM
 */
public class BridgeActivator implements BundleActivator {
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
    private ServiceTracker httpServiceTracker;

    public void start(BundleContext context)
            throws Exception {
        Bundle bundle = context.getBundle();

        if (bundle != null) {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("alias", "/render");
            RenderServlet renderServlet = new RenderServlet();
            serviceRegistrations.add(context.registerService(Servlet.class.getName(), renderServlet, props));
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
