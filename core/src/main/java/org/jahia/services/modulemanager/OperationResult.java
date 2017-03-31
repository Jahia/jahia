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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Represents the result of the bundle operation, conducted by the {@link ModuleManager} service.
 *
 * @author bdjiba
 * @author Sergiy Shyrkov
 */
public class OperationResult implements Serializable {

    private static final long serialVersionUID = 5330025844927356487L;

    /**
     * Returns an operation result indicating successful operation for the specified bundle.
     *
     * @param bundleInfo the information about the target bundle
     */
    public static OperationResult success(BundleInfo bundleInfo) {
        return new OperationResult("Operation successful", bundleInfo);
    }

    private List<BundleInfo> bundleInfos = new LinkedList<>();
    private String message;

    /**
     * Initializes an instance of this class.
     *
     * @param message description of the operation result
     */
    public OperationResult(String message) {
        this.message = message;
    }

    /**
     * Initializes an instance of this class.
     *
     * @param message description of the operation result
     * @param bundleInfo the information about the target bundle
     */
    public OperationResult(String message, BundleInfo bundleInfo) {
        this(message);
        if (bundleInfo != null) {
            this.bundleInfos.add(bundleInfo);
        }
    }

    /**
     * Get the bundle info list
     *
     * @return the bundleInfoList the list of info
     */
    public List<BundleInfo> getBundleInfos() {
        return bundleInfos;
    }

    /**
     * Get the operation result flag
     *
     * @return true if the operation is successfully performed otherwise false
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the bundle info list
     *
     * @param bundleInfoList the bundleInfoList to set
     */
    public void setBundleInfos(List<BundleInfo> bundleInfoList) {
        this.bundleInfos = bundleInfoList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
