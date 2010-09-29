/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewWorkflowStatusActionItem extends ViewStatusActionItem {

    @Override
    public void viewStatus(List<Module> moduleList) {
        Listener<ComponentEvent> removeListener = createRemoveListener();

        String lastUnpublished = null;
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