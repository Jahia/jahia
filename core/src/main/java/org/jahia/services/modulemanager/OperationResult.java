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

    /**
     * Returns an operation result indicating successful operation for the specified bundles.
     *
     * @param bundleInfos the information about the target bundles
     */
    public static OperationResult success(List<BundleInfo> bundleInfos) {
        return new OperationResult("Operation successful", bundleInfos);
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
     * Initializes an instance of this class.
     *
     * @param message description of the operation result
     * @param bundleInfos the information about the target bundles
     */
    public OperationResult(String message, List<BundleInfo> bundleInfos) {
        this(message);
        setBundleInfos(bundleInfos);
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
