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
package org.jahia.services.provisioning;

import java.util.Map;

/**
 * An operation that can be done by the provisioning script
 */
public interface Operation {

    /**
     * Can the entry be handled by this operation
     * @param entry the entry to handle
     * @return true if it can be handled
     */
    boolean canHandle(Map<String, Object> entry);

    /**
     * Execute the operation
     *
     * @param entry operation entry params
     * @param executionContext context
     */
    void perform(Map<String, Object> entry, ExecutionContext executionContext);

    /**
     * Initialize the context, before executing any operation
     * @param executionContext context
     */
    default void init(ExecutionContext executionContext) {
    }

    /**
     * Cleanup after script completion
     * @param executionContext context
     */
    default void cleanup(ExecutionContext executionContext) {
    }

    String getType();

    /**
     * Retrieves the list of API names that the operation refers to, which will be used for access control.
     * A {@code null} or empty array indicates no access control, so use with caution.
     * Each API name will be checked using the syntax {@code "provisioning." + apiName}.
     * (only matching APIs is enough to grant access)
     *
     * @return an array of API names for access control checks
     */
    default String[] getAPIsForAccessControl() {
        return new String[] { getType() };
    }
}
