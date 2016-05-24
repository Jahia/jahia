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

import org.jahia.osgi.FrameworkService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for looking up the {@link BundleService} instances.
 *
 * @author Sergiy Shyrkov
 */
public class BundleServiceLocator {

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
        logger.debug("Looking up suitable BundleService instance...");

        BundleContext bundleContext = FrameworkService.getBundleContext();
        ServiceReference<BundleService> serviceReference = bundleContext.getServiceReference(BundleService.class);
        if (serviceReference != null) {
            service = bundleContext.getService(serviceReference);
        }

        if (service != null) {
            logger.debug("Found suitable BundleService instance of type {} in {} ms", service.getClass().getName(), System.currentTimeMillis() - startTime);
        } else {
            logger.warn("Unable to find suitable cluster-aware BundleService instance. Fallback to the default one.");
        }

        return service;
    }

    public void setDefaultBundleService(BundleService defaultBundleService) {
        this.defaultBundleService = defaultBundleService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
