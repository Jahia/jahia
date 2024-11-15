/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jaxrs_osgi_extender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Http tracker, starts the ResourceBundleTracker
 */
public class HttpServiceTracker extends ServiceTracker<HttpService, ResourceBundleTracker> {

    private final LogService log;

    /**
     * New HttpServiceTracker
     * @param context context
     * @param log log
     */
    public HttpServiceTracker(BundleContext context, LogService log) {
        super(context, HttpService.class.getName(), null);
        this.log = log;
    }

    @Override
    public ResourceBundleTracker addingService(ServiceReference<HttpService> reference) {
        HttpService httpService = context.getService(reference);

        log.log(LogService.LOG_INFO, "Starting to track JAX-RS bundles");

        ResourceBundleTracker bundleTracker = new ResourceBundleTracker(context, httpService, log);
        bundleTracker.open();

        return bundleTracker;
    }

    @Override
    public void removedService(ServiceReference<HttpService> reference, ResourceBundleTracker bundleTracker) {
        log.log(LogService.LOG_INFO, "Stopping tracking JAX-RS bundles");
        bundleTracker.close();
        context.ungetService(reference);
    }
}
