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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.*;

/**
 * Represents a dedicated tab for viewing workflow status and history
 * information.
 *
 * @author Sergiy Shyrkov
 */
public class WorkflowTabItem extends EditEngineTabItem {
    private transient LayoutContainer container;

    private transient WorkflowHistoryPanel activePanel;

    private transient Map<String, WorkflowHistoryPanel> panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
//    private transient LayoutContainer aclPanel;

    private transient GWTJahiaWorkflowType previousType = null;
    private transient GWTJahiaWorkflowDefinition previousSelection = null;
    private transient AclEditor rightsEditor;
    private transient Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> workflowRules;

    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setHandleCreate(false);
        return super.create(engineTab,engine);
    }

    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }
        if (container != null) {
            container.removeFromParent();
            container.removeAll();
        }
        if (container == null) {
            container = new LayoutContainer(new RowLayout());
        }
        tab.add(container);

        tab.setProcessed(true);

        WorkflowHistoryPanel next = getPanel(locale, engine);
        if (activePanel != null) {
            if (activePanel != next) {
                activePanel.removeFromParent();
            }
        }
        container.add(next, new RowData(1, 0.5));

        activePanel = next;

        JahiaContentManagementService.App.getInstance().getWorkflowRules(engine.getNode().getPath(),
                new BaseAsyncCallback<Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>>>() {
                    public void onSuccess(final Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>> result) {
                        HorizontalPanel horizontalPanel = new HorizontalPanel();
                        horizontalPanel.setTableWidth("100%");
                        container.add(horizontalPanel);

                        final ListStore<GWTJahiaWorkflowType> types = new ListStore<GWTJahiaWorkflowType>();
                        final ComboBox<GWTJahiaWorkflowType> typesCombo = new ComboBox<GWTJahiaWorkflowType>();
                        typesCombo.setForceSelection(true);
                        typesCombo.setValueField("name");
                        typesCombo.setDisplayField("displayName");
                        typesCombo.setStore(types);
                        typesCombo.setTypeAhead(true);
                        typesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                        horizontalPanel.add(typesCombo);

                        final ListStore<GWTJahiaWorkflowDefinition> states = new ListStore<GWTJahiaWorkflowDefinition>();
//                        states.add(new LinkedList<GWTJahiaWorkflowDefinition>(result.get(typesCombo.getSelectedText()).keySet()));
                        final ComboBox<GWTJahiaWorkflowDefinition> combo = new ComboBox<GWTJahiaWorkflowDefinition>();

                        workflowRules = result;

                        typesCombo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowType>() {
                            public void selectionChanged(SelectionChangedEvent<GWTJahiaWorkflowType> se) {
                                combo.setValue(null);
                                combo.clearSelections();
                                states.removeAll();
                                final List<GWTJahiaWorkflowDefinition> list = workflowRules.get(se.getSelectedItem());
                                states.add(list);
                                states.sort("displayName", Style.SortDir.ASC);

                                for (GWTJahiaWorkflowDefinition definition : list) {
                                    if (Boolean.TRUE.equals(definition.get("active"))) {
                                        combo.setValue(definition);
                                        break;
                                    }
                                }                                
                            }
                        });
                        combo.setForceSelection(true);
                        combo.setDisplayField("displayName");
                        combo.setWidth(400);
                        combo.setStore(states);
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                        combo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowDefinition>() {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<GWTJahiaWorkflowDefinition> event) {
                                if (event.getSelectedItem() != null) {
                                    if (previousSelection != null) {
                                        previousSelection.set("active", Boolean.FALSE);
                                    }
                                    event.getSelectedItem().set("active", Boolean.TRUE);
                                }
                                previousSelection = event.getSelectedItem();
                                previousType = typesCombo.getValue();
                                tab.layout();
                            }
                        });

                        types.add(new ArrayList<GWTJahiaWorkflowType>(workflowRules.keySet()));
                        types.sort("displayName", Style.SortDir.ASC);
                        typesCombo.setValue(types.getAt(0));
                        horizontalPanel.add(combo);

                        if (!engine.getNode().isWriteable() || engine.getNode().isLocked()) {
                            combo.setReadOnly(true);
                            typesCombo.setReadOnly(true);
                        }

                        tab.layout();
                    }
                });

    }

//    private void displayACLEditor(final GWTJahiaNodeACL gwtJahiaNodeACL, final GWTJahiaNode node,
//                                  final List<GWTJahiaWorkflowDefinition> selection, NodeHolder engine) {
//        rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
//        rightsEditor.setAclGroup("tasks");
//        if (aclPanel != null) {
//            aclPanel.removeAll();
//        }
//        rightsEditor.setCanBreakInheritance(false);
//        rightsEditor.setReadOnly(!engine.getNode().isWriteable() || engine.getNode().isLocked());
//
//        if (aclPanel == null) {
//            aclPanel = new LayoutContainer(new FitLayout());
//        }
//        aclPanel.add(rightsEditor.renderNewAclPanel());
//        container.add(aclPanel, new RowData(1, 0.43));
//    }

    private WorkflowHistoryPanel getPanel(String locale, NodeHolder engine) {
        WorkflowHistoryPanel panel = panelsByLanguage.get(locale);
        if (panel == null) {
            panel = new WorkflowHistoryPanel(engine.getNode().getUUID(), locale);
            panel.setVisible(true);
            panelsByLanguage.put(locale, panel);
        }
        return panel;
    }

    public void doSave(NodeHolder engine) {
        Set<GWTJahiaWorkflowDefinition>  activeWorkflows = new HashSet<GWTJahiaWorkflowDefinition>();
        if (workflowRules == null) {
            return;
        }
        for (List<GWTJahiaWorkflowDefinition> list : workflowRules.values()) {
            for (GWTJahiaWorkflowDefinition definition : list) {
                if (Boolean.TRUE.equals(definition.get("active"))) {
                    activeWorkflows.add(definition);
                }
            }
        }

        engine.getNode().set("activeWorkflows", activeWorkflows);
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            container = null;
            panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
//            aclPanel = null;
            previousType = null;
            previousSelection = null;
            rightsEditor = null;
            workflowRules = null;
        }

        super.setProcessed(processed);
    }


}
