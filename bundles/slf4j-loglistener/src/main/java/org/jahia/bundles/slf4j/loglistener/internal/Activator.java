/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
