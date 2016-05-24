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
package org.jahia.services.modulemanager.persistence;

import java.io.IOException;
import java.io.InputStream;

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
     * @param bundleKey
     *            the key of the bundle to delete the info for
     * @return the result of the operation: <code>true</code> in case the info was deleted successfully; <code>false</code> - if no info for
     *         the specified key was found
     * @throws ModuleManagementException
     *             in case of unexpected error during the delete process
     */
    boolean delete(String bundleKey) throws ModuleManagementException;

    /**
     * Returns the input stream for the persisted bundle considering the requested key.
     * 
     * @param bundleKey
     *            the key of the bundle to be looked up
     * @return the input stream for the persisted bundle considering the requested key or <code>null</code> if the corresponding bundle
     *         cannot be found for the provided key
     * @throws ModuleManagementException
     *             in case of unexpected error during the download process
     */
    InputStream download(String bundleKey) throws ModuleManagementException;

    /**
     * Extracts the information from the specified bundle resource.
     * 
     * @param resource
     *            the bundle resource as a source
     * @return the information, extracted from the provided resource
     * @throws IOException
     *             in case of a resource read issue
     */
    PersistedBundle extract(Resource resource) throws IOException;

    /**
     * Returns the info for the persisted bundle considering the requested key.
     * 
     * @param bundleKey
     *            the key of the bundle to be looked up
     * @return the info for the persisted bundle considering the requested key or <code>null</code> if the corresponding info cannot be
     *         found for the provided key
     */
    PersistedBundle find(String bundleKey);

    /**
     * Persists the specified bundle resource either creating a new info in the storage or updating the existing entry with the supplied
     * data.
     * 
     * @param bundleInfo
     *            the bundle info as a source
     * @throws IOException
     *             in case of a resource read issue
     * @throws ModuleManagementException
     *             in case of issues during persistence operation
     */
    void store(PersistedBundle bundleInfo) throws IOException, ModuleManagementException;

    /**
     * Persists the info for the specified resources either creating a new info in the storage or updating the existing entry with the
     * supplied data.
     * 
     * @param resource
     *            the bundle bundle resource as a source
     * @return the bundle information, extracted from the provided resource
     * @throws IOException
     *             in case of a resource read issue
     * @throws ModuleManagementException
     *             in case of issues during persistence operation
     */
    PersistedBundle store(Resource resource) throws IOException, ModuleManagementException;

}
