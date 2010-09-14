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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

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
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;

import java.util.*;

/**
 * Represents a dedicated tab for viewing workflow status and history
 * information.
 *
 * @author Sergiy Shyrkov
 */
public class WorkflowTabItem extends EditEngineTabItem {
    private LayoutContainer container;

    private WorkflowHistoryPanel activePanel;

    private Map<String, WorkflowHistoryPanel> panelsByLanguage;
    private LayoutContainer aclPanel;
    private final JahiaContentManagementServiceAsync service;

    private GWTJahiaWorkflowType previousType = null;
    private GWTJahiaWorkflowDefinition previousSelection = null;
    private AclEditor rightsEditor;
    private Map<GWTJahiaWorkflowType, Map<GWTJahiaWorkflowDefinition,GWTJahiaNodeACL>> workflowRules;


    /**
     * Initializes an instance of this class.
     *
     * @param engine reference to the owner
     */
    public WorkflowTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.workflow", "Workflow"), engine);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.workflow());
        panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
        service = JahiaContentManagementService.App.getInstance();
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
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
        add(container);

        setProcessed(true);

        WorkflowHistoryPanel next = getPanel(locale.getLanguage());
        if (activePanel != null) {
            if (activePanel != next) {
                activePanel.removeFromParent();
            }
        }
        container.add(next, new RowData(1, 0.5));

        activePanel = next;

        service.getWorkflowRules(engine.getNode().getPath(),
                new BaseAsyncCallback<Map<GWTJahiaWorkflowType,Map<GWTJahiaWorkflowDefinition,GWTJahiaNodeACL>>>() {
                    public void onSuccess(final Map<GWTJahiaWorkflowType,Map<GWTJahiaWorkflowDefinition,GWTJahiaNodeACL>> result) {
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
                                final Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL> map =
                                        workflowRules.get(se.getSelectedItem());
                                states.add(new LinkedList<GWTJahiaWorkflowDefinition>(map.keySet()));
                                states.sort("displayName", Style.SortDir.ASC);

                                for (GWTJahiaWorkflowDefinition definition : map.keySet()) {
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
                                if (previousSelection != null && rightsEditor != null) {
                                    workflowRules.get(previousType).remove(previousSelection);
                                    workflowRules.get(previousType).put(previousSelection, rightsEditor.getAcl());
                                }
                                if (event.getSelectedItem() != null) {
                                    if (previousSelection != null) {
                                        previousSelection.set("active", Boolean.FALSE);
                                    }
                                    event.getSelectedItem().set("active", Boolean.TRUE);
                                    GWTJahiaNodeACL gwtJahiaNodeACL =
                                            workflowRules.get(typesCombo.getValue()).get(event.getSelectedItem());
                                    displayACLEditor(gwtJahiaNodeACL, engine.getNode(), combo.getSelection());
                                } else {
                                    aclPanel.removeAll();
                                }
                                previousSelection = event.getSelectedItem();
                                previousType = typesCombo.getValue();
                                layout();
                            }
                        });

                        types.add(new ArrayList<GWTJahiaWorkflowType>(workflowRules.keySet()));
                        types.sort("displayName", Style.SortDir.ASC);
                        typesCombo.setValue(types.getAt(0));
                        horizontalPanel.add(combo);

                        layout();
                    }
                });

    }

    private void displayACLEditor(final GWTJahiaNodeACL gwtJahiaNodeACL, final GWTJahiaNode node,
                                  final List<GWTJahiaWorkflowDefinition> selection) {
        rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
        rightsEditor.setAclGroup("tasks");
        if (aclPanel != null) {
            aclPanel.removeAll();
        }
        rightsEditor.setCanBreakInheritance(false);
        rightsEditor.setReadOnly(false);

        if (aclPanel == null) {
            aclPanel = new LayoutContainer(new FitLayout());
        }
        aclPanel.add(rightsEditor.renderNewAclPanel());
        container.add(aclPanel, new RowData(1, 0.43));
    }

    private WorkflowHistoryPanel getPanel(String locale) {
        WorkflowHistoryPanel panel = panelsByLanguage.get(locale);
        if (panel == null) {
            panel = new WorkflowHistoryPanel(engine.getNode().getUUID(), locale);
            panel.setVisible(true);
            panelsByLanguage.put(locale, panel);
        }
        return panel;
    }

    public void doSave() {
        Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL>  activeWorkflows = new HashMap<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL>();
        if (workflowRules == null) {
            return;
        }
        for (Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL> map : workflowRules.values()) {
            for (GWTJahiaWorkflowDefinition definition : map.keySet()) {
                if (Boolean.TRUE.equals(definition.get("active"))) {
                    activeWorkflows.put(definition, map.get(definition));
                }
            }
        }
        final Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL> aclMap = activeWorkflows;

        if (previousSelection != null && rightsEditor != null) {
            aclMap.remove(previousSelection);
            aclMap.put(previousSelection, rightsEditor.getAcl());
        }


        if (aclMap.isEmpty()) {
            engine.getNode().getNodeTypes().remove("jmix:worklfowRulesable");
        } else {
            if (!engine.getNode().getNodeTypes().contains("jmix:worklfowRulesable")) {
                engine.getNode().getNodeTypes().add("jmix:worklfowRulesable");
            }
        }
        engine.getNode().set("activeWorkflows", aclMap);
    }

}
