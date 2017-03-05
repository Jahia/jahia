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

    public void setOutcome(Set<String> outcome) {
        this.outcomes = outcome;
    }

    public Set<String> getOutcomes() {
        return outcomes;
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
