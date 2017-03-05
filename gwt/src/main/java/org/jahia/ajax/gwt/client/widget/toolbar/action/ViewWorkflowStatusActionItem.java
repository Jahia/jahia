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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * 
 */
public class ViewWorkflowStatusActionItem extends ViewStatusActionItem {

    @Override
    public void viewStatus(List<Module> moduleList) {
        Listener<ComponentEvent> removeListener = createRemoveListener();

        boolean allPublished = true;
        for (Module module : moduleList) {
            if (module.getNode() != null) {
                GWTJahiaWorkflowInfo info = module.getNode().getWorkflowInfo();
                if (info.getActiveWorkflows().size()>0) {
                    GWTJahiaWorkflow workflow = info.getActiveWorkflows().values().iterator().next();
                    String current = workflow.getDefinition().getName();
                    allPublished = false;
                    if(workflow.getDuedate()!=null) {
                        infoLayers.addInfoLayer(module, "Workflow :<br/>"+current+" is waiting on timer.<br/>Will be triggered at : "+ DateTimeFormat.getMediumDateTimeFormat().format(workflow.getDuedate()),
                                 "red", "red", removeListener, true, "0.7");
                    } else {
                        infoLayers.addInfoLayer(module, "Workflow :<br/>started "+current, "red", "red", removeListener, true,
                            "0.7");
                    }
                }
            }
        }

        if (allPublished) {
            infoLayers.addInfoLayer(moduleList.iterator().next(), "No actual worflow(s) started", "black", "white",
                    removeListener, false,
                    "0.7");
        }
    }

}