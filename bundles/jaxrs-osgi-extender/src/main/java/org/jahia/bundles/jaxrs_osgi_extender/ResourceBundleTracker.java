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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

import javax.ws.rs.core.Application;
import java.util.Dictionary;

/**
 * Track bundles with JAX-RS headers, register endpoint
 */
public class ResourceBundleTracker extends BundleTracker<String> {
    public static final String PROP_JAXRS_ALIAS = "JAX-RS-Alias";
    public static final String PROP_JAXRS_APPLICATION = "JAX-RS-Application";

    private final LogService log;
    private final HttpService httpService;

    /**
     * New ResourceBundleTracker
     * @param context context
     * @param httpService httpService
     * @param log log
     */
    public ResourceBundleTracker(BundleContext context,
                                 HttpService httpService, LogService log) {
        super(context, Bundle.ACTIVE, null);
        this.httpService = httpService;
        this.log = log;
    }

    @Override
    public String addingBundle(Bundle bundle, BundleEvent event) {
        @SuppressWarnings("unchecked")
        Dictionary<String, String> headers = bundle.getHeaders();

        String alias = headers.get(PROP_JAXRS_ALIAS);
        String jaxrsApplication = headers.get(PROP_JAXRS_APPLICATION);

        if (alias == null || jaxrsApplication == null) {
            // ignore this bundle
            return null;
        }

        // modify alias so that we can properly detect in ServletHandler that we are requesting a REST call and
        // therefore the request should not be wrapped
        // record the initial alias since that's what is recorded internally and return that
        String initialAlias = alias;
        alias = alias + ".jaxrs";

        ServletContainer servlet = processBundle(bundle, jaxrsApplication);

        BundleHttpContext bundleContext = new BundleHttpContext(bundle);

        try {
            log.log(LogService.LOG_INFO, "Registering HTTP servlet under alias " + alias +
                    " for JAX-RS resources in bundle " + bundle.getLocation());

            httpService.registerServlet(alias, servlet, null, bundleContext);

            return initialAlias;
        } catch (Exception e) {
            log.log(LogService.LOG_ERROR, "Error registering servlet.", e);
            return null;
        }
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent event, String alias) {
        removedBundle(bundle, event, alias);
        addingBundle(bundle, event);
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, String alias) {
        log.log(LogService.LOG_INFO, "Unregistering HTTP servlet under alias " + alias + " for JAX-RS resources in bundle "
                + bundle.getLocation());

        httpService.unregister(alias);
    }

    private ServletContainer processBundle(Bundle bundle, String applicationName) {
        Application application = null;
        try {
            Class<?> applicationClass = bundle.loadClass(applicationName);
            application = (Application) applicationClass.newInstance();
        } catch (Exception e) {
            log.log(LogService.LOG_ERROR, "Error loading application class " + applicationName + " from bundle "
                    + bundle.getLocation(), e);
        }

        return new ServletContainer(ResourceConfig.forApplication(application));
    }

}
