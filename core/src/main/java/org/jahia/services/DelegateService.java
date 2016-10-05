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
package org.jahia.services;

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleUtils;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate class that is dispatching the calls to an appropriate service implementation of the {@link T}.
 * It's used when a service need two distinct implementation:
 * Clustered implementation, respond to cluster behaviors and expectations
 * Non clustered implementation, default implementation
 *
 * The clustered service will be search in the OSGI context, to be able to register the clustered version of a service
 * the service implementation should have the property: {@value BUNDLE_SERVICE_PROPERTY_CLUSTERED} set to "true".
 *
 * @author Kevan
 */
public abstract class DelegateService<T> {

    public static final String BUNDLE_SERVICE_PROPERTY_CLUSTERED = "clustered";
    private static final String CLUSTERED_SERVICE_FILTER = "(" + BUNDLE_SERVICE_PROPERTY_CLUSTERED + "=true)";

    private static final Logger logger = LoggerFactory.getLogger(DelegateService.class);

    private T defaultService;
    private SettingsBean settingsBean;

    private T lookupClusteredService(Class<T> clazz) {

        long startTime = System.currentTimeMillis();
        logger.debug("Looking up suitable {} instance using filter {} ...", clazz.getSimpleName(), CLUSTERED_SERVICE_FILTER);

        T service = BundleUtils.getOsgiService(clazz, CLUSTERED_SERVICE_FILTER);

        if (service == null) {
            throw new JahiaRuntimeException("Unable to find suitable cluster-aware AreaService instance");
        }

        logger.debug("Found suitable {} instance of type {} in {} ms", new Object[] {
                clazz.getSimpleName(),
                service.getClass().getName(),
                System.currentTimeMillis() - startTime
        });

        return service;
    }

    /**
     * Lookup appropriate service depending on cluster environment or not
     * @param clazz the service class
     * @return the service instance
     */
    protected T lookupService(Class<T> clazz) {
        return (settingsBean.isClusterActivated() ? lookupClusteredService(clazz) : defaultService);
    }

    public void setDefaultService(T defaultService) {
        this.defaultService = defaultService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
