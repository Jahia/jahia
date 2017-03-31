/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Collection;
import java.util.Map;

import org.jahia.osgi.BundleState;
import org.jahia.services.modulemanager.spi.BundleService;
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
 * In cases, when the bundle can be unambiguously identified either by the symbolic name and version or by the symbolic name alone, the
 * group ID or the group ID and version correspondingly could me omitted, i.e.:
 * <p>
 * <code>
 * <pre>article/2.0.2</pre>
 * </code>
 * <p>
 * or
 * <p>
 * <code>
 * <pre>article</pre>
 * </code>
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
     * @param target The group of cluster nodes targeted by the install operation (see BundleService class JavaDoc)
     * @param start Whether the installed bundle should be started right away
     * @return The result of the install operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult install(Resource bundleResource, String target, boolean start) throws ModuleManagementException;

    /**
     * Start the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to start (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the start operation (see BundleService class JavaDoc)
     * @return The result of the start operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult start(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Stop the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to stop (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the stop operation (see BundleService class JavaDoc)
     * @return The result of the stop operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult stop(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Uninstall the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to uninstall (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the uninstall operation (see BundleService class JavaDoc)
     * @return The result of the uninstall operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult uninstall(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Refresh the specified bundle on the target group of cluster nodes.
     *
     * @param bundleKey Bundle key to refresh (see class JavaDoc for the supported key format)
     * @param target The group of cluster nodes targeted by the refresh operation (see BundleService class JavaDoc)
     * @return The result of the refresh operation
     * @throws ModuleManagementException In case of problems
     */
    OperationResult refresh(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Get info about a bundle.
     *
     * @param bundleKey Bundle key (see class JavaDoc for the supported key format; note that bundle version is required)
     * @param target The group of cluster nodes to get info from (see BundleService class JavaDoc)
     * @return A map of bundle info by cluster node name; each map value is either a BundleService.ModuleInfo instance in case the bundle is a DX module, or a BundleService.BundleInfo instance otherwise
     */
    Map<String, BundleService.BundleInformation> getInfo(String bundleKey, String target) throws ModuleManagementException;

    /**
     * Get info about multiple bundles.
     *
     * @param bundleKeys Bundle keys (see class JavaDoc for the supported key format; note that bundle version is required)
     * @param target The group of cluster nodes to get info from (see BundleService class JavaDoc)
     * @return A map of bundle info by bundle key by cluster node name; each map value is either a BundleService.ModuleInfo instance in case the bundle is a DX module, or a BundleService.BundleInfo instance otherwise
     */
    Map<String, Map<String, BundleService.BundleInformation>> getInfos(Collection<String> bundleKeys, String target) throws ModuleManagementException;

    /**
     * Get current local state of a bundle.
     *
     * @param bundleKey Bundle key (see class JavaDoc for the supported key format; note that bundle version is required)
     * @return Current local OSGi state of the bundle
     */
    BundleState getLocalState(String bundleKey) throws ModuleManagementException;

    /**
     * Get local info about a bundle.
     *
     * @param bundleKey Bundle key (see class JavaDoc for the supported key format; note that bundle version is required)
     * @return Local info about the bundle; represented by either a BundleService.ModuleInfo instance in case the bundle is a DX module, or a BundleService.BundleInfo instance otherwise
     */
    BundleService.BundleInformation getLocalInfo(String bundleKey) throws ModuleManagementException;
}
