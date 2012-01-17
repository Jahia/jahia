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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
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

    private transient GWTJahiaWorkflowDefinition previousSelection = null;
    private transient Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> workflowRules;

    public WorkflowTabItem() {
        setHandleCreate(false);
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
            container = new LayoutContainer(new BorderLayout());
        }
        tab.add(container);

        tab.setProcessed(true);

        final LayoutContainer layoutContainer = new LayoutContainer(new BorderLayout());
        container.add(layoutContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 150));

        WorkflowHistoryPanel next = getPanel(locale, engine);
        if (activePanel != null) {
            if (activePanel != next) {
                activePanel.removeFromParent();
            }
        }
        container.add(next, new BorderLayoutData(Style.LayoutRegion.CENTER));

        activePanel = next;

        JahiaContentManagementService.App.getInstance().getWorkflowRules(engine.getNode().getPath(),
                new BaseAsyncCallback<Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>>>() {
                    public void onSuccess(final Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>> result) {
                        workflowRules = result;
                        for (List<GWTJahiaWorkflowDefinition> list : workflowRules.values()) {
                            for (GWTJahiaWorkflowDefinition definition : list) {
                                if (Boolean.TRUE.equals(definition.get("active")) && engine.getNode().getPath().equals(definition.get("definitionPath"))) {
                                    definition.set("set", Boolean.TRUE);
                                }
                            }
                        }

                        final ListStore<GWTJahiaWorkflowType> types = new ListStore<GWTJahiaWorkflowType>();
                        types.add(new ArrayList<GWTJahiaWorkflowType>(workflowRules.keySet()));
                        types.sort("displayName", Style.SortDir.ASC);

                        ColumnModel header = new ColumnModel(Arrays.asList(new ColumnConfig("displayName", "displayName", 300)));

                        final Grid<GWTJahiaWorkflowType> grid = new Grid<GWTJahiaWorkflowType>(types, header);
                        grid.setWidth(250);
                        grid.setHideHeaders(true);
                        grid.setHeight(150);
                        grid.setAutoExpandColumn("displayName");
                        grid.setAutoExpandMax(1200);
                        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 250);
                        layoutContainer.add(grid, data);

                        FormPanel form = new FormPanel();
                        form.setHeaderVisible(false);
                        form.setLabelWidth(200);
                        form.setFieldWidth(300);
                        final CheckBox box = new CheckBox();
                        box.setFieldLabel(Messages.get("label.workflow.inherited","Same workflow as parent"));
                        form.add(box);

                        final ListStore<GWTJahiaWorkflowDefinition> states = new ListStore<GWTJahiaWorkflowDefinition>();
                        final ComboBox<GWTJahiaWorkflowDefinition> combo = new ComboBox<GWTJahiaWorkflowDefinition>();
                        combo.setFieldLabel(Messages.get("label.workflow","Workflow"));
                        combo.setForceSelection(true);
                        combo.setDisplayField("displayName");
                        combo.setWidth(400);
                        combo.setStore(states);
                        combo.setTypeAhead(true);
                        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                        form.add(combo);

                        data = new BorderLayoutData(Style.LayoutRegion.CENTER);
                        layoutContainer.add(form, data);


                        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowType>() {
                            @Override
                            public void selectionChanged(SelectionChangedEvent<GWTJahiaWorkflowType> se) {
                                previousSelection = null;
                                combo.setValue(null);
                                combo.clearSelections();
                                states.removeAll();
                                final List<GWTJahiaWorkflowDefinition> list = workflowRules.get(se.getSelectedItem());
                                states.add(list);
                                states.sort("displayName", Style.SortDir.ASC);
                                for (GWTJahiaWorkflowDefinition definition : list) {
                                    if (Boolean.TRUE.equals(definition.get("active"))) {
                                        box.setValue(!Boolean.TRUE.equals(definition.get("set")));
                                        combo.setValue(definition);
                                        return;
                                    }
                                }
                                box.setValue(true);
                            }
                        });

                        box.addListener(Events.Change, new Listener<FieldEvent>() {
                            public void handleEvent(FieldEvent be) {
                                combo.setEnabled(!box.getValue());
                                if (combo.getSelection().size() == 1) {
                                    combo.getSelection().get(0).set("set", !box.getValue());
                                    if (!box.getValue()) {
                                        combo.getSelection().get(0).set("active", Boolean.TRUE);
                                    }
                                }
                            }
                        });

                        combo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowDefinition>() {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<GWTJahiaWorkflowDefinition> event) {
                                if (previousSelection != null) {
                                    previousSelection.set("set", Boolean.FALSE);
                                    previousSelection.set("active", Boolean.FALSE);
                                }
                                if (event.getSelectedItem() != null) {
                                    event.getSelectedItem().set("set", !box.getValue());
                                    if (!box.getValue()) {
                                        event.getSelectedItem().set("active", Boolean.TRUE);
                                    }
                                }
                                previousSelection = event.getSelectedItem();
                                tab.layout();
                            }
                        });

                        // todo : use specific permission to manage workflows ?
                        if (!PermissionsUtils.isPermitted("jcr:write", engine.getNode()) || engine.getNode().isLocked()) {
                            box.setReadOnly(true);
                            combo.setReadOnly(true);
                        }

                        grid.getSelectionModel().select(0, false);

                        tab.layout();
                    }
                });

    }

    private WorkflowHistoryPanel getPanel(String locale, NodeHolder engine) {
        WorkflowHistoryPanel panel = panelsByLanguage.get(locale);
        if (panel == null) {
            panel = new WorkflowHistoryPanel(engine.getNode().getUUID(), locale);
            panel.setVisible(true);
            panelsByLanguage.put(locale, panel);
        }
        return panel;
    }

    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        Set<GWTJahiaWorkflowDefinition>  activeWorkflows = new HashSet<GWTJahiaWorkflowDefinition>();
        if (workflowRules == null) {
            return;
        }
        for (List<GWTJahiaWorkflowDefinition> list : workflowRules.values()) {
            for (GWTJahiaWorkflowDefinition definition : list) {
                if (Boolean.TRUE.equals(definition.get("set"))) {
                    activeWorkflows.add(definition);
                }
            }
        }

        node.set("activeWorkflows", activeWorkflows);
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            container = null;
            panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
            previousSelection = null;
            workflowRules = null;
        }

        super.setProcessed(processed);
    }


}
