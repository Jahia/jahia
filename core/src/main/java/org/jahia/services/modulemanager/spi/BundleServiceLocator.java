/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for looking up the {@link BundleService} instances.
 *
 * @author Sergiy Shyrkov
 */
public class BundleServiceLocator {

    private static final String CLUSTERED_SERVICE_FILTER = "(" + Constants.BUNDLE_SERVICE_PROPERTY_CLUSTERED + "=true)";

    private static final Logger logger = LoggerFactory.getLogger(BundleServiceLocator.class);

    private BundleService defaultBundleService;
    private SettingsBean settingsBean;

    /**
     * Looks up the most suitable instance of the {@link BundleService}.
     *
     * @return the most suitable instance of the {@link BundleService}
     */
    public BundleService lookup() {
        BundleService service = settingsBean.isClusterActivated() ? lookupClusteredService() : null;
        return service != null ? service : defaultBundleService;
    }

    /**
     * Looks up an OSGi service instance for {@link BundleService}. Returns <code>null</code> if no instance is found.
     *
     * @return an OSGi service instance for {@link BundleService}. Returns <code>null</code> if no instance is found
     */
    private BundleService lookupClusteredService() {

        BundleService service = null;
        long startTime = System.currentTimeMillis();
        logger.debug("Looking up suitable BundleService instance using filter " + CLUSTERED_SERVICE_FILTER + "...");

        BundleContext bundleContext = FrameworkService.getBundleContext();
        Collection<ServiceReference<BundleService>> serviceReferences;
        try {
            serviceReferences = bundleContext.getServiceReferences(BundleService.class, CLUSTERED_SERVICE_FILTER);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        if (serviceReferences != null) {
            ServiceReference<BundleService> bestServiceReferefnce = null;
            if (serviceReferences.size() > 1) {
                List<ServiceReference<BundleService>> matchingServices = new ArrayList<>(serviceReferences);
                // sort references by ranking (ascending)
                Collections.sort(matchingServices);
                // get the service with the highest ranking
                bestServiceReferefnce = matchingServices.get(matchingServices.size() - 1);
            } else {
                bestServiceReferefnce = serviceReferences.iterator().next();
            }
            // obtain the service
            service = bundleContext.getService(bestServiceReferefnce);
        }

        if (service == null) {
            throw new ModuleManagementException("Unable to find suitable cluster-aware BundleService instance");
        }

        logger.debug("Found suitable BundleService instance of type {} in {} ms", service.getClass().getName(),
                System.currentTimeMillis() - startTime);

        return service;
    }

    public void setDefaultBundleService(BundleService defaultBundleService) {
        this.defaultBundleService = defaultBundleService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
