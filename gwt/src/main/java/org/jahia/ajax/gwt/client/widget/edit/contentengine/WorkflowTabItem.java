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
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
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
    private GWTJahiaLanguage locale;
    private ContentPanel aclPanel;
    private final JahiaContentManagementServiceAsync service;

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
        this.locale = locale;
        if (engine.getNode() == null) {
            return;
        }
        if(container!= null) {
            container.removeFromParent();
            container.removeAll();
        }
        if (container == null) {
            container = new LayoutContainer(new RowLayout());
        }
        generateUI();
        WorkflowHistoryPanel next = getPanel(locale.getLanguage());
        if (activePanel != null) {
            if (activePanel != next) {
                activePanel.removeFromParent();
            }
        }
        container.add(next, new RowData(1, 0.5));

        activePanel = next;

        layout();
    }

    private void generateUI() {
        service.getWorkflowRules(engine.getNode().getPath(),
                                 new BaseAsyncCallback<Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL>>() {
                                     public void onSuccess(
                                             final Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL> result) {
                                         HorizontalPanel horizontalPanel = new HorizontalPanel();
                                         horizontalPanel.setTableWidth("100%");
                                         ListStore<GWTJahiaWorkflowDefinition> states = new ListStore<GWTJahiaWorkflowDefinition>();
                                         states.add(new LinkedList<GWTJahiaWorkflowDefinition>(result.keySet()));

                                         final ComboBox<GWTJahiaWorkflowDefinition> combo = new ComboBox<GWTJahiaWorkflowDefinition>();
                                         combo.setDisplayField("displayName");
                                         combo.setWidth(400);
                                         combo.setStore(states);
                                         combo.setTypeAhead(true);
                                         combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                                         combo.addSelectionChangedListener(
                                                 new SelectionChangedListener<GWTJahiaWorkflowDefinition>() {
                                                     @Override
                                                     public void selectionChanged(
                                                             SelectionChangedEvent<GWTJahiaWorkflowDefinition> gwtJahiaWorkflowDefinitionSelectionChangedEvent) {
                                                         final GWTJahiaNodeACL gwtJahiaNodeACL = result.get(
                                                                 gwtJahiaWorkflowDefinitionSelectionChangedEvent.getSelectedItem());
                                                         displayACLEditor(gwtJahiaNodeACL, engine.getNode(),combo.getSelection());
                                                         layout();
                                                     }
                                                 });
                                         combo.setValue(result.keySet().iterator().next());
                                         TableData tableData = new TableData(Style.HorizontalAlignment.LEFT,
                                                                             Style.VerticalAlignment.TOP);
                                         tableData.setWidth("80%");
                                         tableData.setPadding(2);
                                         horizontalPanel.add(combo, tableData);
                                         final Button button = new Button("Manage Workflows");
                                         button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                             @Override
                                             public void componentSelected(ButtonEvent buttonEvent) {
                                                 button.disable();
                                                 final WorkflowManager workflowManager = new WorkflowManager(
                                                         engine.getNode(), result);
                                                 workflowManager.addWindowListener(new WindowListener() {
                                                     @Override
                                                     public void windowHide(WindowEvent we) {
                                                         super.windowHide(we);
                                                         button.enable();
                                                     }
                                                 });
                                                 workflowManager.show();
                                             }
                                         });
                                         tableData = new TableData(Style.HorizontalAlignment.RIGHT,
                                                                   Style.VerticalAlignment.TOP);
                                         tableData.setWidth("20%");
                                         tableData.setPadding(2);
                                         horizontalPanel.add(button, tableData);
                                         WorkflowTabItem.this.container.add(horizontalPanel, new RowData(1, 0.07));
                                         final GWTJahiaNodeACL gwtJahiaNodeACL = result.values().iterator().next();
                                         displayACLEditor(gwtJahiaNodeACL, engine.getNode(),combo.getSelection());
                                         layout();
                                     }
                                 });

        add(this.container);
    }

    private void displayACLEditor(final GWTJahiaNodeACL gwtJahiaNodeACL, final GWTJahiaNode node,
                                  final List<GWTJahiaWorkflowDefinition> selection) {
        final AclEditor rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
        rightsEditor.setAclGroup("tasks");
        if (aclPanel != null) {
            aclPanel.removeAll();
        }
        rightsEditor.setCanBreakInheritance(false);
        rightsEditor.setReadOnly(false);
        Button saveButton = rightsEditor.getSaveButton();
        saveButton.setVisible(true);
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                GWTJahiaNodeACL nodeACL = rightsEditor.getAcl();
                service.updateWorkflowRulesACL(node.getPath(),selection.get(0),nodeACL,new BaseAsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
            }
        });

        if (aclPanel == null) {
            aclPanel = new ContentPanel(new FitLayout());
        }
        aclPanel.add(rightsEditor.renderNewAclPanel());
        WorkflowTabItem.this.container.add(aclPanel, new RowData(1, 0.43));
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

    private class WorkflowManager extends Window {
        public WorkflowManager(final GWTJahiaNode node,
                               final Map<GWTJahiaWorkflowDefinition, GWTJahiaNodeACL> currentWorkflows) {
            setSize(600, 400);
            setTitle("Workflow Manager");
            setModal(true);
            setClosable(false);
            service.getWorkflows(new BaseAsyncCallback<List<GWTJahiaWorkflowDefinition>>() {
                public void onSuccess(List<GWTJahiaWorkflowDefinition> result) {
                    List<GWTJahiaWorkflowDefinition> definitions = new LinkedList<GWTJahiaWorkflowDefinition>();
                    final Set<GWTJahiaWorkflowDefinition> set = currentWorkflows.keySet();
                    for (GWTJahiaWorkflowDefinition definition : result) {
                        for (GWTJahiaWorkflowDefinition workflowDefinition : set) {
                            final List<GWTJahiaNodeACE> jahiaNodeACEList = currentWorkflows.get(
                                    workflowDefinition).getAce();
                            if (workflowDefinition.getName().equals(definition.getName()) && (jahiaNodeACEList==null || jahiaNodeACEList.isEmpty() || !jahiaNodeACEList.get(0).isInherited())) {
                                definition.set("active", true);
                                break;
                            } else {
                                definition.set("active", false);
                            }
                        }
                        definitions.add(definition);
                    }
                    final ListStore<GWTJahiaWorkflowDefinition> all = new ListStore<GWTJahiaWorkflowDefinition>();
                    all.add(definitions);
                    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

                    ColumnConfig column = new ColumnConfig();
                    column.setId("displayName");
                    column.setSortable(false);
                    column.setHeader("Name");
                    column.setWidth(200);
                    configs.add(column);

                    CheckColumnConfig checkColumn = new CheckColumnConfig("active", "Active?", 55);
                    checkColumn.setSortable(false);
                    CellEditor checkBoxEditor = new CellEditor(new CheckBox());
                    checkColumn.setEditor(checkBoxEditor);
                    configs.add(checkColumn);

                    Grid<GWTJahiaWorkflowDefinition> grid = new Grid<GWTJahiaWorkflowDefinition>(all, new ColumnModel(
                            configs));
                    ContentPanel cp = new ContentPanel();
                    cp.setBodyBorder(false);
                    cp.setHeading("Workflows declared on this node (not inherited)");
                    cp.setButtonAlign(Style.HorizontalAlignment.CENTER);
                    cp.setLayout(new FitLayout());
                    cp.setSize(600, 350);

                    grid.setStyleAttribute("borderTop", "none");
                    grid.setAutoExpandColumn("displayName");
                    grid.setBorders(true);
                    grid.setStripeRows(true);
                    grid.addPlugin(checkColumn);
                    cp.add(grid);

                    cp.addButton(new Button("Reset", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            all.rejectChanges();
                            hide();
                        }
                    }));

                    cp.addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            all.commitChanges();
                            final List<GWTJahiaWorkflowDefinition> definitions = all.getModels();
                            List<GWTJahiaWorkflowDefinition> actives = new LinkedList<GWTJahiaWorkflowDefinition>();
                            List<GWTJahiaWorkflowDefinition> unactives = new LinkedList<GWTJahiaWorkflowDefinition>();
                            for (GWTJahiaWorkflowDefinition definition : definitions) {
                                if ((Boolean) definition.get("active")) {
                                    actives.add(definition);
                                } else {
                                    unactives.add(definition);
                                }
                            }
                            service.updateWorkflowRules(node.getPath(), actives, unactives,
                                                        new BaseAsyncCallback<Void>() {
                                                            public void onSuccess(Void result) {
                                                                container.removeAll();
                                                                create(locale);
                                                                hide();
                                                            }
                                                        });
                        }
                    }));

                    new GridDragSource(grid);

                    GridDropTarget target = new GridDropTarget(grid);
                    target.setAllowSelfAsSource(true);
                    target.setFeedback(DND.Feedback.INSERT);

                    add(cp);
                    layout();
                    show();
                }
            });
        }


    }
}
