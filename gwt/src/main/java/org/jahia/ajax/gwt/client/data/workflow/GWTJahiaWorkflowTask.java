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
package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:38 PM
 *
 */
public class GWTJahiaWorkflowTask extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowOutcome> outcomes;
    private List<GWTJahiaWorkflowComment> comments;
    private Map<String, GWTJahiaNodeProperty> variables;

    public GWTJahiaWorkflowTask() {
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name",name);
    }

    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider",provider);
    }

    public List<GWTJahiaWorkflowOutcome> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<GWTJahiaWorkflowOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public void setTaskComments(List<GWTJahiaWorkflowComment> comments) {
        this.comments = comments;
    }

    public List<GWTJahiaWorkflowComment> getTaskComments() {
        return comments;
    }

    public void setFormResourceName(String formResourceName) {
        set("formResourceName",formResourceName);
    }

    public String getFormResourceName() {
        return get("formResourceName");
    }

    public void setCreateTime(Date createTime) {
        set("createTime",createTime);
    }

    public Date getCreateTime() {
        return get("createTime");
    }

    public void setProcessId(String processId) {
        set("processId",processId);
    }

    public String getProcessId() {
        return get("processId");
    }

    public void setDisplayName(String displayName) {
        set("displayName",displayName);
    }

    public String getDisplayName(){
        return get("displayName");
    }

    public Map<String, GWTJahiaNodeProperty> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, GWTJahiaNodeProperty> variables) {
        this.variables = variables;
    }
}
