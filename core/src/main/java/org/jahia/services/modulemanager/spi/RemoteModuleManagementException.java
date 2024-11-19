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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.services.modulemanager.ModuleManagementException;

/**
 * Indicates errors that happen during remote invocation of another cluster node.
 */
public class RemoteModuleManagementException extends ModuleManagementException {

    private static final long serialVersionUID = -5591954704941402764L;
    private final String nodeId;

    /**
     * Create an exception instance.
     *
     * @param message Error message
     * @param cause Cause if any
     * @param nodeId ID of the cluster node that failed remote invocation
     */
    public RemoteModuleManagementException(String message, Throwable cause, String nodeId) {
        super(message, cause);
        this.nodeId = nodeId;
    }

    /**
     * Create an exception instance.
     *
     * @param cause Cause if any
     * @param nodeId ID of the cluster node that failed remote invocation
     */
    public RemoteModuleManagementException(Throwable cause, String nodeId) {
        this(null, cause, nodeId);
    }

    /**
     * @return ID of the cluster node that failed remote invocation
     */
    public String getNodeId() {
        return nodeId;
    }
}
