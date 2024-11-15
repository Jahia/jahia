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
package org.jahia.services.workflow;

import java.util.Set;

/**
 * Workflow process definition.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 4 févr. 2010
 */
public class WorkflowDefinition extends WorkflowBase {

    private static final long serialVersionUID = 3356236148908996978L;

    private final String key;
    private String packageName;
    private String formResourceName;
    private Set<String> tasks;

    public WorkflowDefinition(String name, String key, String provider) {
        super(name, provider);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkflowDefinition that = (WorkflowDefinition) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getProvider() != null ? !getProvider().equals(that.getProvider()) : that.getProvider() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (getProvider() != null ? getProvider().hashCode() : 0);
        return result;
    }

    public String getFormResourceName() {
        return formResourceName;
    }

    public void setFormResourceName(String formResourceName) {
        this.formResourceName = formResourceName;
    }

    /**
     * Returns the name of this item.
     *
     * @return the name of this item
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Returns the name of the workflow item provider.
     *
     * @return the name of the workflow item provider
     */
    @Override
    public String getProvider() {
        return super.getProvider();
    }

    public void setTasks(Set<String> tasks) {
        this.tasks = tasks;
    }

    public Set<String> getTasks() {
        return tasks;
    }

    public String getWorkflowType() {
        return WorkflowService.getInstance().getWorkflowType(this);
    }
}
