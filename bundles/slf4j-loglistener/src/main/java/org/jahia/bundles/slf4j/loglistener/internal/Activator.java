package org.jahia.bundles.slf4j.loglistener.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Created by loom on 21.03.14.
 */
public class Activator implements BundleActivator {

    private ServiceTracker logReaderServiceServiceTracker;
    private final SLF4jLogListener slf4jLogListener = new SLF4jLogListener();

    @Override
    public void start(BundleContext context) throws Exception {
        logReaderServiceServiceTracker = new ServiceTracker(context, LogReaderService.class, new SLF4jLogListenerServiceTrackerCustomizer(context));
        logReaderServiceServiceTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (logReaderServiceServiceTracker != null) {
            logReaderServiceServiceTracker.close();
        }
    }

    class SLF4jLogListenerServiceTrackerCustomizer implements ServiceTrackerCustomizer {

        private BundleContext bundleContext;

        SLF4jLogListenerServiceTrackerCustomizer(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public Object addingService(ServiceReference reference) {
            LogReaderService logReaderService = (LogReaderService) bundleContext.getService(reference);
            logReaderService.addLogListener(slf4jLogListener);
            return logReaderService;
        }

        @Override
        public void modifiedService(ServiceReference reference, Object service) {

        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            LogReaderService logReaderService = (LogReaderService) service;
            logReaderService.removeLogListener(slf4jLogListener);
        }
    }

}
