/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Sep 16, 2010
 * Time: 5:37:18 PM
 *
 */
public class WorkflowRule implements Serializable {
    private String definitionPath;
    private String providerKey;
    private String workflowDefinitionKey;
    private String workflowRootPath;
    private Map<String,String> permissions;

    public WorkflowRule(String definitionPath, String workflowRootPath, String providerKey, String workflowDefinitionKey, Map<String,String> permissions) {
        this.definitionPath = definitionPath;
        this.providerKey = providerKey;
        this.workflowDefinitionKey = workflowDefinitionKey;
        this.permissions = permissions;
        this.workflowRootPath = workflowRootPath;
    }

    public String getDefinitionPath() {
        return definitionPath;
    }

    public String getWorkflowRootPath() {
        return workflowRootPath;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public String getWorkflowDefinitionKey() {
        return workflowDefinitionKey;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkflowRule that = (WorkflowRule) o;

        if (definitionPath != null ? !definitionPath.equals(that.definitionPath) : that.definitionPath != null)
            return false;
        if (providerKey != null ? !providerKey.equals(that.providerKey) : that.providerKey != null) return false;
        if (workflowDefinitionKey != null ? !workflowDefinitionKey.equals(that.workflowDefinitionKey) : that.workflowDefinitionKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = definitionPath != null ? definitionPath.hashCode() : 0;
        result = 31 * result + (providerKey != null ? providerKey.hashCode() : 0);
        result = 31 * result + (workflowDefinitionKey != null ? workflowDefinitionKey.hashCode() : 0);
        return result;
    }
}
