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
     * Install the specified bundle on the target group of cluster nodes, optionally starting it right after if the <code>start</code>
     * parameter is <code>true</code>.
     *
     * In case the same version of the bundle is already installed, update it with the snapshot referenced by the uri.
     *
     * @param uri The bundle location
     * @param target The group of cluster nodes targeted by the install operation (see JavaDoc of the class)
     * @param start Whether the installed bundle should be started right away
     * @param startLevel The start level to apply to the installed bundle
     * @return The result of the install operation
     * @throws ModuleManagementException in case of operation failure
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void install(String uri, String target, boolean start, int startLevel) throws ModuleManagementException, InvalidTargetException;

    /**
     * Performs the resolve operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by the resolve operation (see JavaDoc of the class)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void resolve(BundleInfo bundleInfo, String target)
            throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException;

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
     * Performs the update operation with the provided bundle on the target group of cluster nodes.
     *
     * @param bundleInfo The bundle to perform operation for
     * @param target The group of cluster nodes targeted by the update operation (see JavaDoc of the class)
     * @throws ModuleManagementException in case of operation failure
     * @throws ModuleNotFoundException in case the corresponding bundle cannot be found
     * @throws InvalidTargetException in case target is not a valid target for module operation
     */
    void update(BundleInfo bundleInfo, String target)
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
     * @param bundleInfos The bundles to retrieve info about
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
     * Info about a bundle fragment.
     */
    public interface FragmentInformation extends BundleInformation {
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
