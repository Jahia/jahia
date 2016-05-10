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
import org.jahia.services.modulemanager.ModuleManager;
import org.osgi.framework.BundleException;

/**
 * Service for bundle related operations.
 * 
 * @author Sergiy Shyrkov
 */
public interface BundleService {

    /**
     * The set of available bundle operations.
     * 
     * @author Sergiy Shyrkov
     */
    public static enum Operation {
        START, STOP, UNINSTALL;
    }

    /**
     * Install the specified bundle on the target group of cluster nodes, optionally starting it right after if the <code>start</code>
     * parameter is <code>true</code>.
     * 
     * @param uri
     *            the bundle location
     * @param target
     *            the group of cluster nodes targeted by the install operation (see JavaDoc of {@link ModuleManager} class for the supported
     *            values)
     * @param start
     *            <code>true</code> if the installed bundle should be started right away; <code>false</code> to keep it in the installed
     *            stated
     * @return the result of the install operation
     * @throws BundleException
     *             thrown exception is case of problems
     */
    void install(String uri, String target, boolean start) throws BundleException;

    /**
     * Performs the specified operation with the provided bundle on the target group of cluster nodes.
     * 
     * @param bundleInfo
     *            the bundle to perform operation for (see JavaDoc of {@link ModuleManager} class for the supported key format)
     * @param target
     *            the group of cluster nodes targeted by this operation (see JavaDoc of {@link ModuleManager} class for the supported
     *            values)
     * @throws BundleException
     *             thrown exception is case of problems
     */
    void performOperation(BundleInfo bundleInfo, Operation operation, String target) throws BundleException;

}
