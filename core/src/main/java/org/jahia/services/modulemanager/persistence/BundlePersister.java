/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.persistence;

import org.jahia.services.modulemanager.ModuleManagementException;
import org.springframework.core.io.Resource;

/**
 * Defines service, responsible for module bundle persistence.
 *
 * @author Sergiy Shyrkov
 */
public interface BundlePersister {

    /**
     * Deletes the stored info of the specified bundle.
     *
     * @param bundleKey The key of the bundle to delete the info for
     * @return The result of the operation: <code>true</code> in case the info was deleted successfully; <code>false</code> - if no info for
     *         the specified key was found
     * @throws ModuleManagementException In case of unexpected error during the delete process
     */
    boolean delete(String bundleKey) throws ModuleManagementException;

    /**
     * Returns the info for the persisted bundle considering the requested key.
     *
     * @param bundleKey The key of the bundle to be looked up
     * @return The info for the persisted bundle considering the requested key or <code>null</code> if the corresponding info cannot be
     *         found for the provided key
     */
    PersistentBundle find(String bundleKey);

    /**
     * Persists the specified bundle resource either creating a new info in the storage or updating the existing entry with the supplied
     * data.
     *
     * @param bundleInfo The bundle info as a source
     * @throws ModuleManagementException In case of issues during persistence operation
     */
    void store(PersistentBundle bundleInfo) throws ModuleManagementException;

    /**
     * Persists the info for the specified resources either creating a new info in the storage or updating the existing entry with the
     * supplied data.
     *
     * @param resource The bundle bundle resource as a source
     * @return The bundle information, extracted from the provided resource
     * @throws ModuleManagementException In case of issues during persistence operation
     */
    PersistentBundle store(Resource resource) throws ModuleManagementException;
}
