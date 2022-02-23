/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
