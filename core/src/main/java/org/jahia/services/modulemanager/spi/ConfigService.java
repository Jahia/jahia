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
package org.jahia.services.modulemanager.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Service to store and restore OSGi configurations to/from JCR
 */
public interface ConfigService {

    /**
     * Configuration types
     */
    enum ConfigType {
        SYSTEM, MODULE, MODULE_DEFAULT, USER
    }

    /**
     * Get the config for the specified PID
     *
     * If it does not exist yet, create a new config
     *
     * @param pid The config PID
     * @return the Config object
     * @throws IOException exception
     */
    Config getConfig(String pid) throws IOException ;

    /**
     * Get the settings for the specified factory PID and identifer
     *
     * If it does not exist yet, create a new config
     *
     * @param factoryPid The factory PID
     * @param identifier The identifier
     * @return the Config object
     * @throws IOException exception
     */
    Config getConfig(String factoryPid, String identifier) throws IOException ;

    /**
     * Persist the changes on the config into the the Configuration Manager storage
     * @param config The config to store
     * @throws IOException exception
     */
    void storeConfig(Config config) throws IOException;

    /**
     * Delete the associated configuration
     *
     * @param config The config to delete
     * @throws IOException exception
     */
    void deleteConfig(Config config) throws IOException;

    /**
     * Get all configurations currently deployed
     *
     * @return the list of configurations files saved along with their type
     */
    Map<String, ConfigType> getAllConfigurationTypes();

    /**
     * Store configurations into JCR
     *
     * @return the list of configurations files saved
     */
    Collection<String> storeAllConfigurationsToJCR();

    /**
     * Restore configurations from JCR
     *
     * @param types The types to store, null for all configurations
     * @return the list of restored configurations
     */
    public Collection<String> restoreConfigurationsFromJCR(Collection<ConfigType> types);

    /**
     * Auto save configuration on change
     */
    void setAutoSaveToJCR(boolean autoSave);

}
