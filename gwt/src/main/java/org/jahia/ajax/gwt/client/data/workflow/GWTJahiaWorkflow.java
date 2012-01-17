/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
