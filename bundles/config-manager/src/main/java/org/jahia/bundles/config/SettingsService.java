/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.config;

import org.osgi.service.cm.Configuration;

import java.io.IOException;

/**
 * Service that helps manipulation of OSGi configuration
 *
 * Gets service configuration based on pid or service factory configurations based on a pid and additional id
 * Provides proper method to create/update OSGi configurations and reflect that in cfg files
 */
public interface SettingsService {

    /**
     * Get the settings for the specified PID
     *
     * If it does not exist yet, create a new settings and configuration
     *
     * @param pid
     * @return the Settings object
     * @throws IOException
     */
    Settings getSettings(String pid) throws IOException ;

    /**
     * Get the settings for the specified factory PID and identifer
     *
     * If it does not exist yet, create a new settings and configuration
     *
     * @param factoryPid
     * @param identifier
     * @return the Settings object
     * @throws IOException
     */
    Settings getSettings(String factoryPid, String identifier) throws IOException ;

    /**
     * Get the settings object for an existing Configuration object
     *
     * @param configuration
     * @return the Settings object
     */
    Settings getSettings(Configuration configuration);

    /**
     * Persist the changes on the settings into the the Configuration Manager storage
     * @param settings
     * @throws IOException
     */
    void storeSettings(Settings settings) throws IOException;

    /**
     * Delete the associated configuration
     *
     * @param settings
     * @throws IOException
     */
    void deleteSettings(Settings settings) throws IOException;

}
