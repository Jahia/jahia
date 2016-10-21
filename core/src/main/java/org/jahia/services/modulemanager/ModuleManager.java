/*
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
package org.jahia.services.modulemanager;

import org.springframework.core.io.Resource;

/**
 * Entry point interface for the module management service, providing functionality for module deployment, undeployment, start and stop
 * operations, which are performed in a seamless way on a standalone installation as well as across the platform cluster.
 * <p>
 * The following bundle key format is supported in the most of operations of this service:
 * <p>
 * <code>
 * <pre>&lt;groupId&gt;/&lt;symbolicName&gt;/&lt;version&gt;</pre>
 * </code>
 * <p>
 * For example:
 * <p>
 * <code>
 * <pre>org.jahia.modules/article/2.0.2</pre>
 * </code>
 * <p>
 * Note, please, the version here is the <code>Bundle-Version</code>. In case of SNAPSHOT versions it can differ in the format from the
 * module version.
 * <p>
 * In some cases, when the bundle can be unambiguously identified by the symbolic name and version, the group ID could me omitted, i.e.:
 * <p>
 * <code>
 * <pre>article/2.0.2</pre>
 * </code>
 * <p>
 * The value of the <code>target</code> group of cluster nodes could be specified as <code>null</code>, meaning the default group is
 * concerned, which includes all cluster nodes.
 *
 * @author Sergiy Shyrkov
 */
public interface ModuleManager {

    /**
     * Install the specified bundle on the target group of cluster nodes.
     *
     * @param bundleResource The resource representing a bundle to install
     * @param target The group of cluster nodes targeted by the install operation (see class JavaDoc for the supported values)
     * @return The result of the install operation
     * @throws ModuleManagementException Is case of problems
     */
    OperationResult install(Resource bundleResource, String target) throws ModuleManagementException;

    /**
     * Install the specified bundle on the target group of cluster nodes, optionally starting it right after.
     *
     * @param bundleResource The resource, representing a bundle to install
     * @param target The group of cluster nodes targeted by the install operation (see class JavaDoc for the supported values)
     * @param start Whether the installed bundle should be started right away
     * @return The result of the install operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult install(Resource bundleResource, String target, boolean start) throws ModuleManagementException;

    /**
     * Start the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to start (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the start operation (see class JavaDoc for the supported values)
     * @return The result of the start operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult start(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Stop the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to stop (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the stop operation (see class JavaDoc for the supported values)
     * @return The result of the stop operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult stop(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Uninstall the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to uninstall (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the uninstall operation (see class JavaDoc for the supported values)
     * @return The result of the uninstall operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult uninstall(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Refresh the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to refresh (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the refresh operation (see class JavaDoc for the supported values)
     * @return The result of the refresh operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult refresh(String bundleKey, String target) throws ModuleManagementException;
}
