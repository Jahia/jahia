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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowActionItem extends BaseActionItem {
    private List<String> processes;
    private boolean bypassWorkflow;
    private ActionItem bypassActionItem;

    public WorkflowActionItem() {
    }

    public WorkflowActionItem(List<String> processes, boolean bypassWorkflow, ActionItem bypassActionItem) {
        this.processes = processes;
        this.bypassWorkflow = bypassWorkflow;
        this.bypassActionItem = bypassActionItem;
    }

    @Override
    public void handleNewLinkerSelection() {
//        Menu menu = new Menu();
//        final GWTJahiaNode node;
//
//        boolean isEnabled = false;
//
//        if (linker.getSelectedNode() != null) {
//            node = linker.getSelectedNode();
//        } else {
//            node = linker.getMainNode();
//        }
//        if (node != null) {
//            menu.removeAll();
//            GWTJahiaWorkflowInfo info = node.getWorkflowInfo();
//            List<GWTJahiaWorkflowDefinition> wfs = info.getPossibleWorkflows();
//            if (!node.isLanguageLocked(node.getLanguageCode()) && node.isWriteable()) {
//                for (final GWTJahiaWorkflowDefinition wf : wfs) {
//                    if (processes == null || processes.contains(wf.getId())) {
//                        isEnabled = true;
//                        MenuItem item = new MenuItem("Start new : " + wf.getName());
//                        item.setIconStyle("gwt-toolbar-icon-workflow-start");
//                        item.addSelectionListener(new SelectionListener<MenuEvent>() {
//                            @Override
//                            public void componentSelected(MenuEvent ce) {
//                                String formResourceName = wf.getFormResourceName();
//                                if (formResourceName!=null && !"".equals(formResourceName)) {
//                                    // Start workflow
//                                    WorkflowActionDialog wad = new WorkflowActionDialog(node, linker);
//                                    wad.initStartWorkflowDialog(wf);
//                                    wad.show();
//                                } else {
//                                    JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
//                                    async.startWorkflow(node.getPath(), wf,new ArrayList<GWTJahiaNodeProperty>(), new BaseAsyncCallback() {
//                                        public void onSuccess(Object result) {
//                                            Info.display("Workflow started", "Workflow started");
//                                            linker.refresh(Linker.REFRESH_ALL);
//                                        }
//
//                                        public void onApplicationFailure(Throwable caught) {
//                                            Info.display("Workflow not started", "Workflow not started");
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                        menu.add(item);
//                        break;
//                    }
//                }
//                if (bypassWorkflow) {
//                    MenuItem item = new MenuItem(Messages.get("label.bypassWorkflow", "Bypass workflow"));
//                    item.setIconStyle("gwt-toolbar-icon-workflow-bypass");
//                    isEnabled = true;
//                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
//                        @Override
//                        public void componentSelected(MenuEvent ce) {
//                            bypassActionItem.onComponentSelection();
//                        }
//                    });
//                    menu.add(item);
//                }
//            }
//        } else {
//            menu.removeAll();
//        }
//
//        if (isEnabled) {
//            setEnabled(true);
//        }
//        if (bypassActionItem != null) {
//            Log.info("handle wf: " + bypassActionItem.getClass().getName());
//            bypassActionItem.handleNewLinkerSelection();
//        }
//        if (!isEnabled) {
//            setEnabled(false);
//        }
//
//        setSubMenu(menu);
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (bypassActionItem != null) {
            bypassActionItem.init(gwtToolbarItem, linker);
        }
    }

    @Override
    public Component getTextToolItem() {
        final Component item = super.getTextToolItem();
        if (bypassActionItem != null) {
            bypassActionItem.setTextToolitem(item);
        }
        return item;
    }

    @Override
    public MenuItem getMenuItem() {
        final MenuItem item = super.getMenuItem();
        if (bypassActionItem != null) {
            bypassActionItem.setMenuItem(item);
        }
        return item;
    }

    @Override
    public MenuItem getContextMenuItem() {
        final MenuItem item = super.getContextMenuItem();
        if (bypassActionItem != null) {
            bypassActionItem.setContextMenuItem(item);
        }
        return item;
    }
}
