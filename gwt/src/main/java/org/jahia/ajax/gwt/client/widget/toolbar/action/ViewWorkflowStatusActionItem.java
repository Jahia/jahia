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
