/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Collection;
import java.util.Map;

import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleState;
import org.jahia.services.modulemanager.BundleBucketInfo;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.InvalidTargetException;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleNotFoundException;

/**
 * Service for bundle related operations.
 * <p>
 * The value of the <code>target</code> group of cluster nodes could be specified as <code>null</code>, meaning the default group is
 * concerned, which includes all cluster nodes.
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
     * @param target The group of cluster nodes targeted by the install operation (see JavaDoc of the class)
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
     * @param target The group of cluster nodes targeted by the start operation (see JavaDoc of the class)
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
     * @param target The group of cluster nodes targeted by the stop operation (see JavaDoc of the class)
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
     * @param target The group of cluster nodes targeted by the uninstall operation (see JavaDoc of the class)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void uninstall(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

    /**
     * Performs the refresh operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by the refresh operation (see JavaDoc of the class)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void refresh(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

    /**
     * Performs the refresh operation with the provided bundles on the target group of cluster nodes.
     *
     * @param bundleInfos The bundles to perform operation for
     * @param target The group of cluster nodes targeted by the refresh operation (see JavaDoc of the class)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case any if the corresponding bundles cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void refresh(Collection<BundleInfo> bundleInfos, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

    /**
     * Get info about a bundle.
     *
     * @param bundleInfo The bundle to retrieve info about
     * @param target The group of cluster nodes to get info from (see JavaDoc of the class)
     * @return A map of bundle info by cluster node name; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     * @throws InvalidTargetException in case the target is not a valid one
     */
    Map<String, BundleInformation> getInfo(BundleInfo bundleInfo, String target) throws ModuleManagementException, InvalidTargetException;

    /**
     * Get info about multiple bundles.
     *
     * @param bundleInfo The bundle to retrieve info about
     * @param target The group of cluster nodes to get info from (see JavaDoc of the class)
     * @return A map of bundle info by bundle key by cluster node name; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     * @throws InvalidTargetException in case the target is not a valid one
     */
    Map<String, Map<String, BundleInformation>> getInfos(Collection<BundleInfo> bundleInfos, String target) throws ModuleManagementException, InvalidTargetException;

    /**
     * Get info about multiple bundles belonging to a single bundle bucket.
     *
     * @param bundleBucketInfo The bundle bucket
     * @param target The group of cluster nodes to get info from (see JavaDoc of the class)
     * @return A map of bundle info by bundle key by cluster node name; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     */
    Map<String, Map<String, BundleInformation>> getInfos(BundleBucketInfo bundleBucketInfo, String target) throws ModuleManagementException, InvalidTargetException;

    /**
     * Get info about all installed bundles.
     *
     * @param target The group of cluster nodes to get info from (see JavaDoc of the class)
     * @return A map of bundle info by bundle key by cluster node name; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     * @throws InvalidTargetException in case the target is not a valid one
     */
    Map<String, Map<String, BundleInformation>> getAllInfos(String target) throws ModuleManagementException, InvalidTargetException;

    /**
     * Get current local state of a bundle.
     *
     * @param bundleInfo The bundle to retrieve status
     * @return Current local OSGi state of the bundle
     */
    BundleState getLocalState(BundleInfo bundleInfo) throws ModuleManagementException, ModuleNotFoundException;

    /**
     * Get local info about a bundle.
     *
     * @param bundleInfo The bundle to retrieve info about
     * @return Local info about the bundle; represented by either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     */
    BundleService.BundleInformation getLocalInfo(BundleInfo bundleInfo) throws ModuleManagementException, ModuleNotFoundException;

    /**
     * Get local info about multiple bundles belonging to a single bundle bucket.
     *
     * @param bundleBucketInfo The bundle bucket
     * @return A map of bundle info by bundle key; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     */
    Map<String, BundleInformation> getLocalInfos(BundleBucketInfo bundleBucketInfo) throws ModuleManagementException;

    /**
     * Get local info about all installed bundles.
     *
     * @return A map of bundle info by bundle key; each map value is either a LocalModuleInfo instance in case the bundle is a DX module, or a LocalBundleInfo instance otherwise
     */
    Map<String, BundleInformation> getAllLocalInfos() throws ModuleManagementException;

    /**
     * Info about a bundle.
     */
    public interface BundleInformation {

        /**
         * @return State of the bundle in OSGi terms
         * @throws ModuleManagementException in case there was an exception retrieving bundle information that has been suppressed to let the invoker handle it later
         */
        public BundleState getOsgiState() throws ModuleManagementException;
    }

    /**
     * Info about a bundle which is a DX module.
     */
    public interface ModuleInformation extends BundleInformation {

        /**
         * @return State of the module in DX terms
         * @throws ModuleManagementException in case there was an exception retrieving bundle information that has been suppressed to let the invoker handle it later
         */
        public ModuleState.State getModuleState() throws ModuleManagementException;
    }
}
