/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Sep 9, 2010
 * Time: 5:29:14 PM
 * 
 */
public class GWTJahiaWorkflow extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowTask> availableTasks;
    private Map<String, GWTJahiaNodeProperty> variables;
    private CustomWorkflow customWorkflowInfo;

    public GWTJahiaWorkflow() {
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider",provider);
    }

    public GWTJahiaNode getNode() {
        return get("node");
    }

    public void setNode(GWTJahiaNode node) {
        set("node",node);
    }

    public Date getStartTime() {
        return get("startTime");
    }

    public void setStartTime(Date startTime) {
        set("startTime",startTime);
    }

    public GWTJahiaWorkflowDefinition getDefinition() {
        return get("definition");
    }

    public void setDefinition(GWTJahiaWorkflowDefinition definition) {
        set("definition",definition);
    }

    public Date getDuedate() {
        return get("duedate");
    }

    public void setDuedate(Date duedate) {
        set("duedate",duedate);
    }

    public void setVariables(Map<String, GWTJahiaNodeProperty> variables) {
        this.variables = variables;
    }

    public Map<String, GWTJahiaNodeProperty> getVariables() {
        return variables;
    }

    public void setLocale(String locale) {
        set("locale",locale);
    }

    public String getLocale() {
        return get("locale");
    }

    public void setWorkspace(String workspace) {
        set("workspace",workspace);
    }

    public String getWorkspace() {
        return get("workspace");
    }

    public List<GWTJahiaWorkflowTask> getAvailableTasks() {
        return availableTasks;
    }

    public void setAvailableTasks(List<GWTJahiaWorkflowTask> availableTasks) {
        this.availableTasks = availableTasks;
    }

    public CustomWorkflow getCustomWorkflowInfo() {
        return customWorkflowInfo;
    }

    public void setCustomWorkflowInfo(CustomWorkflow customWorkflowInfo) {
        this.customWorkflowInfo = customWorkflowInfo;
    }
}
