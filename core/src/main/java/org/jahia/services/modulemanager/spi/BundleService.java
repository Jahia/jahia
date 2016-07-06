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

import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.InvalidTargetException;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.ModuleNotFoundException;

/**
 * Service for bundle related operations.
 *
 * @author Sergiy Shyrkov
 */
public interface BundleService {

    /**
     * Install the specified bundle on the target group of cluster nodes, optionally starting it right after if the <code>start</code>
     * parameter is <code>true</code>.
     *
     * In case the same version of the bundle is already installed, update it with the snapshot referenced by the uri.
     *
     * @param uri The bundle location
     * @param target The group of cluster nodes targeted by the install operation (see JavaDoc of {@link ModuleManager} class for the
     *            supported values)
     * @param start Whether the installed bundle should be started right away
     * @return The result of the install operation
     * @throws ModuleManagementException in case of operation failure
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void install(String uri, String target, boolean start) throws ModuleManagementException, InvalidTargetException;

    /**
     * Performs the start operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by this operation (see JavaDoc of {@link ModuleManager} class for the supported
     *            values)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void start(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

    /**
     * Performs the stop operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by this operation (see JavaDoc of {@link ModuleManager} class for the supported
     *            values)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void stop(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

    /**
     * Performs the uninstall operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by this operation (see JavaDoc of {@link ModuleManager} class for the supported
     *            values)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void uninstall(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;
}
