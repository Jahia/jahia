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
package org.jahia.services.workflow;

import org.jahia.services.usermanager.JahiaUser;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents task action.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class WorkflowTask extends WorkflowAction {

    private static final long serialVersionUID = 2178724997919059897L;

    private Date dueDate;
    private String description;
    private Date createTime;
    private JahiaUser assignee;
    private String id;
    private Set<String> outcomes;
    private Map<String, String> outcomesPermissions;
    private List<WorkflowParticipation> participations;
    private String formResourceName;
    private String processId;
    private List<String> displayOutcomes;
    private Map<String, Object> variables;
    private List<String> outcomeIcons;

    public WorkflowTask(String name, String provider) {
        super(name, provider);
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setAssignee(JahiaUser assignee) {
        this.assignee = assignee;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JahiaUser getAssignee() {
        return assignee;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getId() {
        return id;
    }

    public void setOutcomes(Set<String> outcome) {
        this.outcomes = outcome;
    }

    public Set<String> getOutcomes() {
        return outcomes;
    }

    public Map<String, String> getOutcomesPermissions() {
        return outcomesPermissions;
    }

    public void setOutcomesPermissions(Map<String, String> outcomesPermissions) {
        this.outcomesPermissions = outcomesPermissions;
    }

    public List<WorkflowParticipation> getParticipations() {
        return participations;
    }

    public void setParticipations(List<WorkflowParticipation> participations) {
        this.participations = participations;
    }

    public void setFormResourceName(String formResourceName) {
        this.formResourceName = formResourceName;
    }

    public String getFormResourceName() {
        return formResourceName;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setDisplayOutcomes(List<String> displayOutcomes) {
        this.displayOutcomes = displayOutcomes;
    }

    public List<String> getDisplayOutcomes() {
        return displayOutcomes;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public List<String> getOutcomeIcons() {
        return outcomeIcons;
    }

    public void setOutcomeIcons(List<String> outcomeIcons) {
        this.outcomeIcons = outcomeIcons;
    }
}
